package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;
import com.pedropathing.utils.Angle;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.List;

/** Controls autonomous state transitions using the latest pose, Cell tags, and mechanism state. */
public class AutonomousController {

    private static final double AUTONOMOUS_PERIOD_SECONDS = 30.0;

    // TODO: TUNE every constant below against real testing - these are
    // starting placeholders, not measured values.
    private static final double SCAN_DURATION_SECONDS = 0.75;
    private static final double ESTIMATED_COLLECT_SECONDS_PER_BALL = 3.0;
    private static final double ESTIMATED_SCORE_SECONDS = 4.0;
    private static final double ESTIMATED_PARK_SECONDS = 3.0;
    private static final double SAFE_RESERVE_SECONDS = 1.5;
    private static final double DRIVE_TIMEOUT_SECONDS = 5.0;
    private static final double CELL_DRIVE_TIMEOUT_SECONDS = 6.0;
    private static final double AIM_TOLERANCE_RADIANS = Math.toRadians(3);
    private static final double AIM_TIMEOUT_SECONDS = 2.0;
    private static final double SCORE_TIMEOUT_SECONDS = 4.0;
    private static final double ARRIVAL_DISTANCE_INCHES = 4.0;

    private final Robot robot;
    private final AutonomousPlanner planner = new AutonomousPlanner();
    private final StartPosition startPosition;

    private AutoState state = AutoState.INITIALIZE;
    private double stateEnteredAtSeconds = 0;
    private double matchStartSeconds = -1;

    private AutonomousPlanner.Route currentRoute = AutonomousPlanner.Route.EMPTY;
    private int currentIndex = 0;
    private Ball currentTargetBall;
    private Cell committedCell;
    private CellTarget currentCellTarget;
    private boolean atScoringPose;

    private ReplanReason lastReplanReason = ReplanReason.NONE;
    private WorldState lastWorldState;
    private boolean parkingToZone;
    private boolean retriedParkingRoute;
    private List<Pose> parkingRoute = java.util.Collections.emptyList();
    private int parkingWaypointIndex;

    public AutonomousController(Robot robot, StartPosition startPosition) {
        this.robot = robot;
        this.startPosition = startPosition;
    }

    /** Call exactly once per OpMode loop() iteration. */
    public void update(double nowSeconds) {
        if (matchStartSeconds < 0) {
            matchStartSeconds = nowSeconds;
        }

        robot.updateWorldModel(nowSeconds);

        if (nowSeconds - matchStartSeconds >= BioBuzzAutoConfig.AUTO_SAFETY_CUTOFF_SECONDS
                && state != AutoState.FINISHED && state != AutoState.EMERGENCY_STOP) {
            robot.follower.stop();
            robot.mechanism.stopAll();
            enter(AutoState.FINISHED, nowSeconds);
            return;
        }

        double timeRemaining = AUTONOMOUS_PERIOD_SECONDS - (nowSeconds - matchStartSeconds);
        WorldState world = robot.worldState(timeRemaining, nowSeconds);
        lastWorldState = world;

        try {
            // 2. DECIDE + 3. ACT.
            runState(world, nowSeconds);
        } catch (RuntimeException e) {
            // Defensive net (project brief section 25): a bug in decision
            // logic must never leave motors running uncontrolled for the
            // rest of autonomous.
            state = AutoState.EMERGENCY_STOP;
            stateEnteredAtSeconds = nowSeconds;
            robot.follower.stop();
            robot.mechanism.stopAll();
        }
    }

    private void runState(WorldState world, double now) {
        switch (state) {

            case INITIALIZE: {
                committedCell = planner.resolveScoringCell(world);
                lastReplanReason = ReplanReason.INITIAL_PLAN;
                enter(AutoState.SCAN, now);
                break;
            }

            case SCAN: {
                if (now - stateEnteredAtSeconds >= SCAN_DURATION_SECONDS) {
                    enter(AutoState.PLAN, now);
                }
                break;
            }

            case PLAN: {
                if (!enoughTimeToAttemptAnotherBall(world) && world.inventory.totalCount() == 0) {
                    beginPark(world, now);
                    break;
                }

                currentRoute = planner.planRoute(world);
                currentIndex = 0;

                if (!currentRoute.isEmpty()) {
                    currentTargetBall = currentRoute.first();
                    currentIndex = 1;
                    driveToTarget(currentTargetBall);
                    enter(AutoState.DRIVE_TO_BALL, now);
                } else if (world.inventory.totalCount() > 0) {
                    beginDriveToCell(world, now);
                } else {
                    beginPark(world, now);
                }
                break;
            }

            case DRIVE_TO_BALL: {
                if (checkCollectionReplan(world, now)) break;

                boolean arrived = !robot.busy() || distanceTo(world, currentTargetBall.fieldPose()) <= ARRIVAL_DISTANCE_INCHES;
                boolean timedOut = now - stateEnteredAtSeconds >= DRIVE_TIMEOUT_SECONDS;
                if (arrived || timedOut) {
                    robot.mechanism.startIntake(currentTargetBall.type, now);
                    enter(AutoState.ACQUIRE_BALL, now);
                }
                break;
            }

            case ACQUIRE_BALL: {
                if (robot.mechanism.justReachedReadyToScore()
                        || robot.mechanism.justFailedIntake()
                        || robot.mechanism.justFaulted()) {
                    enter(AutoState.VERIFY_ACQUISITION, now);
                }
                break;
            }

            case VERIFY_ACQUISITION: {
                if (robot.mechanism.state() == ScoringState.READY_TO_SCORE) {
                    // Robot.updateWorldModel() already applied
                    // inventory.acquire(...) the tick this became true -
                    // here we finish the loop by updating BallMap and
                    // deciding what's next.
                    robot.ballMap.markCollected(currentTargetBall);
                    advanceAfterAcquisition(world, now);
                } else {
                    robot.ballMap.markMissing(currentTargetBall, now);
                    lastReplanReason = robot.mechanism.state() == ScoringState.FAULT
                            ? ReplanReason.INTAKE_FAILED : ReplanReason.TARGET_MISSING;
                    if (robot.mechanism.state() == ScoringState.FAULT) {
                        robot.mechanism.clearFault(now);
                    }
                    enter(AutoState.PLAN, now);
                }
                break;
            }

            case DRIVE_TO_CELL: {
                ReplanReason reason = planner.detectReplanReason(world, null, committedCell);
                if (reason != ReplanReason.NONE) {
                    lastReplanReason = reason;
                    enter(AutoState.PLAN, now);
                    break;
                }

                boolean arrived = !robot.busy();
                boolean timedOut = now - stateEnteredAtSeconds >= CELL_DRIVE_TIMEOUT_SECONDS;
                if (arrived || timedOut) {
                    enter(AutoState.AIM_AT_CELL, now);
                }
                break;
            }

            case AIM_AT_CELL: {
                if (currentCellTarget == null) {
                    enter(AutoState.PLAN, now);
                    break;
                }

                Cell activeCell = planner.resolveScoringCell(world);
                if (activeCell == null || activeCell != currentCellTarget.cell) {
                    lastReplanReason = ReplanReason.ACTIVE_CELL_CHANGED;
                    enter(AutoState.PLAN, now);
                    break;
                }
                if (activeCell.state() == CellState.UNKNOWN || activeCell.state() == CellState.STALE) {
                    stopForUntrustedCell(now);
                    break;
                }

                Pose pose = world.robotPose();
                robot.follower.hold(new Pose(pose.x(), pose.y(), currentCellTarget.aimHeadingRadians));

                double headingError = Angle.error(pose.heading(), currentCellTarget.aimHeadingRadians);
                boolean aimed = Math.abs(headingError) <= AIM_TOLERANCE_RADIANS;
                boolean timedOut = now - stateEnteredAtSeconds >= AIM_TIMEOUT_SECONDS;
                if (aimed && !atScoringPose) {
                    currentCellTarget = activeCell.computeTarget(world.robotPose());
                    robot.followTo(currentCellTarget.scoringPose);
                    atScoringPose = true;
                    enter(AutoState.DRIVE_TO_SCORE, now);
                } else if (aimed) {
                    enter(AutoState.SCORE, now);
                } else if (timedOut) {
                    stopForUntrustedCell(now);
                }
                break;
            }

            case DRIVE_TO_SCORE: {
                ReplanReason reason = planner.detectReplanReason(world, null, committedCell);
                if (reason != ReplanReason.NONE) {
                    lastReplanReason = reason;
                    atScoringPose = false;
                    enter(AutoState.PLAN, now);
                    break;
                }

                Cell activeCell = planner.resolveScoringCell(world);
                if (activeCell == null || activeCell != committedCell
                        || activeCell.state() == CellState.UNKNOWN || activeCell.state() == CellState.STALE) {
                    stopForUntrustedCell(now);
                    break;
                }

                if (!robot.busy()) {
                    enter(AutoState.AIM_AT_CELL, now);
                } else if (now - stateEnteredAtSeconds >= CELL_DRIVE_TIMEOUT_SECONDS) {
                    robot.follower.stop();
                    beginPark(world, now);
                }
                break;
            }

            case SCORE: {
                if (robot.mechanism.state() == ScoringState.READY_TO_SCORE) {
                    Cell activeCell = planner.resolveScoringCell(world);
                    if (activeCell == null || activeCell != committedCell
                            || activeCell.state() == CellState.UNKNOWN || activeCell.state() == CellState.STALE) {
                        stopForUntrustedCell(now);
                        break;
                    }
                    robot.mechanism.triggerOuttake(now);
                }
                if (robot.mechanism.justCompletedScoreCycle()) {
                    enter(AutoState.VERIFY_SCORE, now);
                } else if (now - stateEnteredAtSeconds >= SCORE_TIMEOUT_SECONDS) {
                    lastReplanReason = ReplanReason.SCORING_FAILED;
                    enter(AutoState.PLAN, now);
                }
                break;
            }

            case VERIFY_SCORE: {
                // Robot.updateWorldModel() already called
                // inventory.scoreAll() the tick justCompletedScoreCycle()
                // fired. This robot has no sensor that can confirm a ball
                // actually landed in the cell (vs. bouncing out) - being
                // honest about that belongs in telemetry (see telemetry()
                // below: scoring is reported as "assumed," never as a
                // verified fact we don't actually have).
                enter(AutoState.DECIDE_NEXT_CYCLE, now);
                break;
            }

            case DECIDE_NEXT_CYCLE: {
                if (worthAnotherCycle(world)) {
                    lastReplanReason = ReplanReason.TIME_THRESHOLD_REACHED;
                    enter(AutoState.PLAN, now);
                } else {
                    beginPark(world, now);
                }
                break;
            }

            case PARK: {
                double timeout = parkingToZone
                        ? BioBuzzAutoConfig.PARK_TIMEOUT_SECONDS
                        : BioBuzzAutoConfig.PARK_LANE_TIMEOUT_SECONDS;

                if (!robot.busy()) {
                    if (parkingWaypointIndex < parkingRoute.size()) {
                        robot.followTo(parkingRoute.get(parkingWaypointIndex++));
                        enter(AutoState.PARK, now);
                    } else if (!parkingToZone) {
                        robot.followTo(BioBuzzAutoConfig.loadingZonePose(robot.redAlliance));
                        parkingToZone = true;
                        enter(AutoState.PARK, now);
                    } else {
                        robot.mechanism.stopAll();
                        enter(AutoState.FINISHED, now);
                    }
                } else if (now - stateEnteredAtSeconds >= timeout) {
                    robot.follower.stop();
                    if (!parkingToZone && !retriedParkingRoute) {
                        retriedParkingRoute = true;
                        parkingRoute = BioBuzzAutoConfig.parkingLaneRoute(robot.redAlliance, world.robotPose());
                        parkingWaypointIndex = 0;
                        startNextParkLeg(now);
                    } else {
                        robot.mechanism.stopAll();
                        enter(AutoState.FINISHED, now);
                    }
                }
                break;
            }

            case FINISHED: {
                // Nothing left to do - hold position, mechanisms off.
                break;
            }

            case EMERGENCY_STOP: {
                // Nothing left to do - already stopped on entry.
                break;
            }
        }
    }

    // ---- One-shot transition actions (each issues its command exactly
    // once, at the moment of transition, not repeatedly every tick) -----

    private void driveToTarget(Ball ball) {
        robot.follower.follow(robot.pathTo(ball.fieldPose()));
    }

    private void beginDriveToCell(WorldState world, double now) {
        Cell cell = planner.resolveScoringCell(world);
        committedCell = cell;
        if (cell == null) {
            // No known cell at all - shouldn't happen once alliance is
            // selected at init, but a real failsafe (project brief section
            // 25) beats driving somewhere made up.
            beginPark(world, now);
            return;
        }
        currentCellTarget = cell.computeTarget(world.robotPose());
        atScoringPose = false;
        robot.follower.follow(robot.pathTo(currentCellTarget.approachPose));
        enter(AutoState.DRIVE_TO_CELL, now);
    }

    private void advanceAfterAcquisition(WorldState world, double now) {
        if (world.inventory.isFull() || currentIndex >= currentRoute.order.size()) {
            beginDriveToCell(world, now);
        } else {
            currentTargetBall = currentRoute.order.get(currentIndex);
            currentIndex++;
            driveToTarget(currentTargetBall);
            enter(AutoState.DRIVE_TO_BALL, now);
        }
    }

    private void beginPark(WorldState world, double now) {
        parkingRoute = BioBuzzAutoConfig.parkingLaneRoute(robot.redAlliance, world.robotPose());
        parkingWaypointIndex = 0;
        parkingToZone = false;
        retriedParkingRoute = false;
        startNextParkLeg(now);
    }

    private void startNextParkLeg(double now) {
        if (parkingWaypointIndex < parkingRoute.size()) {
            robot.followTo(parkingRoute.get(parkingWaypointIndex++));
        } else {
            robot.followTo(BioBuzzAutoConfig.loadingZonePose(robot.redAlliance));
            parkingToZone = true;
        }
        enter(AutoState.PARK, now);
    }

    // ---- Checks ---------------------------------------------------------

    private boolean checkCollectionReplan(WorldState world, double now) {
        ReplanReason reason = planner.detectReplanReason(world, currentTargetBall, committedCell);
        if (reason != ReplanReason.NONE) {
            lastReplanReason = reason;
            enter(AutoState.PLAN, now);
            return true;
        }
        return false;
    }

    private boolean enoughTimeToAttemptAnotherBall(WorldState world) {
        double minimum = ESTIMATED_COLLECT_SECONDS_PER_BALL + ESTIMATED_SCORE_SECONDS
                + ESTIMATED_PARK_SECONDS + SAFE_RESERVE_SECONDS;
        return world.timeRemainingSeconds >= minimum;
    }

    private boolean worthAnotherCycle(WorldState world) {
        return planner.worthAnotherCycle(world,
                ESTIMATED_COLLECT_SECONDS_PER_BALL * Math.max(1, world.inventory.remainingCapacity()),
                ESTIMATED_SCORE_SECONDS, ESTIMATED_PARK_SECONDS, SAFE_RESERVE_SECONDS);
    }

    // ---- Small helpers ----------------------------------------------------

    private void stopForUntrustedCell(double now) {
        lastReplanReason = ReplanReason.ACTIVE_CELL_CHANGED;
        robot.follower.stop();
        robot.mechanism.stopAll();
        enter(AutoState.EMERGENCY_STOP, now);
    }

    private void enter(AutoState next, double now) {
        state = next;
        stateEnteredAtSeconds = now;
    }

    private double distanceTo(WorldState world, Pose target) {
        Pose pose = world.robotPose();
        return Math.hypot(target.x() - pose.x(), target.y() - pose.y());
    }

    public AutoState state() {
        return state;
    }

    public boolean isFinished() {
        return state == AutoState.FINISHED || state == AutoState.EMERGENCY_STOP;
    }

    /** The WorldState built during the most recent update() call - for
     *  telemetry callers that want to report on it without re-touching
     *  hardware (see OpModes.AutoOpMode). */
    public WorldState lastWorldState() {
        return lastWorldState;
    }

    // ---- Telemetry (project brief section 33) ------------------------------

    public void telemetry(Telemetry telemetry) {
        WorldState world = lastWorldState;
        if (world == null) return;

        telemetry.addData("STATE", state);

        Cell active = planner.resolveScoringCell(world);
        telemetry.addData("ACTIVE CELL", active == null ? "none"
                : active.alliance + " " + active.fieldSide + " " + active.state() + String.format(" (conf %.2f)", active.confidence()));

        telemetry.addData("ROBOT POSE", world.robotPose());
        telemetry.addData("LOCALIZATION CONFIDENCE", String.format("%.2f", world.localization.confidence()));
        telemetry.addData("BALLS VISIBLE", world.ballMap.visibleCount());
        telemetry.addData("BALLS REMEMBERED", world.ballMap.rememberedCount());
        telemetry.addData("TARGET BALL", currentTargetBall == null ? "none" : currentTargetBall.toString());
        telemetry.addData("TARGET COUNT", currentRoute.isEmpty() ? "0" : currentRoute.order.size());
        telemetry.addData("CURRENT PATH", currentRoute.isEmpty() ? "none" : (currentIndex + " / " + currentRoute.order.size()));
        telemetry.addData("CURRENT INVENTORY", world.inventory);
        telemetry.addData("CAPACITY REMAINING", world.inventory.remainingCapacity());
        telemetry.addData("MECHANISM", robot.mechanism.state()
                + (robot.mechanism.lastAcquisitionWasAssumed() ? " (last acquisition ASSUMED, not sensor-verified)" : ""));
        telemetry.addData("REPLAN REASON", lastReplanReason);
        telemetry.addData("APRILTAG", world.cellCameraAvailable ? "camera OK" : "camera UNAVAILABLE");
        telemetry.addData("TIME REMAINING", String.format("%.1f", world.timeRemainingSeconds));
        telemetry.update();
    }
}
