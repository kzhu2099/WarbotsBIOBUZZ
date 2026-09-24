package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * The real dynamic autonomous system. This is where the closed loop the
 * project is built around actually lives, once per call to update():
 *
 *   1. PERCEIVE - robot.updateWorldModel() folds in fresh camera/sensor
 *      data (cameras observe -> world model updates).
 *   2. DECIDE - runState() asks AutonomousPlanner what to do given the
 *      now-current WorldState, and checks whether anything that
 *      invalidates the current plan happened (planner makes a decision /
 *      updates the route).
 *   3. ACT - the chosen state commands the Follower and/or ScoringMechanism
 *      (PedroPathing executes; mechanisms change the world).
 *
 * Then the OpMode calls update() again next loop, step 1 happens again,
 * and it picks up whatever step 3 actually caused - including things NOT
 * going according to plan (a ball wasn't where expected, intake failed,
 * the hive tag reappeared somewhere slightly different). That is what
 * makes this "dynamic" rather than a fixed script: nothing about the
 * sequence of states below is precomputed before the match starts.
 *
 * See AutoState for why the state list here is a condensed version of the
 * project brief's suggested state list, and why that's a deliberate,
 * documented decision rather than a missed requirement.
 */
public class AutonomousController {

    // TODO: VERIFY against the real autonomous period length for the event.
    private static final double AUTONOMOUS_PERIOD_SECONDS = 30.0;

    // TODO: TUNE every constant below against real testing - these are
    // starting placeholders, not measured values.
    private static final double SCAN_DURATION_SECONDS = 0.75;
    private static final double ESTIMATED_COLLECT_SECONDS_PER_BALL = 3.0;
    private static final double ESTIMATED_SCORE_SECONDS = 4.0;
    private static final double ESTIMATED_PARK_SECONDS = 3.0;
    private static final double SAFE_RESERVE_SECONDS = 1.5;
    private static final double DRIVE_TIMEOUT_SECONDS = 5.0;
    private static final double HIVE_DRIVE_TIMEOUT_SECONDS = 6.0;
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
    private Hive committedHive;
    private HiveTarget currentHiveTarget;

    private ReplanReason lastReplanReason = ReplanReason.NONE;
    private WorldState lastWorldState;

    public AutonomousController(Robot robot, StartPosition startPosition) {
        this.robot = robot;
        this.startPosition = startPosition;
    }

    /** Call exactly once per OpMode loop() iteration. */
    public void update(double nowSeconds) {
        if (matchStartSeconds < 0) {
            matchStartSeconds = nowSeconds;
        }

        // 1. PERCEIVE.
        robot.updateWorldModel(nowSeconds);

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
                committedHive = robot.hiveMap.activeHive();
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
                    beginDriveToHive(world, now);
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

            case DRIVE_TO_HIVE: {
                ReplanReason reason = planner.detectReplanReason(world, null, committedHive);
                if (reason != ReplanReason.NONE) {
                    lastReplanReason = reason;
                    enter(AutoState.PLAN, now);
                    break;
                }

                boolean arrived = !robot.busy();
                boolean timedOut = now - stateEnteredAtSeconds >= HIVE_DRIVE_TIMEOUT_SECONDS;
                if (arrived || timedOut) {
                    enter(AutoState.AIM_AT_HIVE, now);
                }
                break;
            }

            case AIM_AT_HIVE: {
                if (currentHiveTarget == null) {
                    enter(AutoState.PLAN, now);
                    break;
                }

                Pose pose = world.robotPose();
                robot.follower.hold(new Pose(pose.x(), pose.y(), currentHiveTarget.aimHeadingRadians));

                double headingError = angleDifference(pose.heading(), currentHiveTarget.aimHeadingRadians);
                boolean aimed = Math.abs(headingError) <= AIM_TOLERANCE_RADIANS;
                boolean timedOut = now - stateEnteredAtSeconds >= AIM_TIMEOUT_SECONDS;
                if (aimed || timedOut) {
                    enter(AutoState.SCORE, now);
                }
                break;
            }

            case SCORE: {
                if (robot.mechanism.state() == ScoringState.READY_TO_SCORE) {
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
                // actually landed in the hive (vs. bouncing out) - being
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
                if (!robot.busy()) {
                    robot.mechanism.stopAll();
                    enter(AutoState.FINISHED, now);
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

    private void beginDriveToHive(WorldState world, double now) {
        Hive hive = planner.resolveScoringHive(world);
        committedHive = hive;
        if (hive == null) {
            // No known hive at all - shouldn't happen once alliance is
            // selected at init, but a real failsafe (project brief section
            // 25) beats driving somewhere made up.
            beginPark(world, now);
            return;
        }
        currentHiveTarget = hive.computeTarget();
        robot.follower.follow(robot.pathTo(currentHiveTarget.approachPose));
        enter(AutoState.DRIVE_TO_HIVE, now);
    }

    private void advanceAfterAcquisition(WorldState world, double now) {
        if (world.inventory.isFull() || currentIndex >= currentRoute.order.size()) {
            beginDriveToHive(world, now);
        } else {
            currentTargetBall = currentRoute.order.get(currentIndex);
            currentIndex++;
            driveToTarget(currentTargetBall);
            enter(AutoState.DRIVE_TO_BALL, now);
        }
    }

    private void beginPark(WorldState world, double now) {
        Pose parkPose = startPosition.fallbackParkPose(robot.redAlliance);
        robot.follower.follow(robot.pathTo(parkPose));
        enter(AutoState.PARK, now);
    }

    // ---- Checks ---------------------------------------------------------

    private boolean checkCollectionReplan(WorldState world, double now) {
        ReplanReason reason = planner.detectReplanReason(world, currentTargetBall, committedHive);
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

    private void enter(AutoState next, double now) {
        state = next;
        stateEnteredAtSeconds = now;
    }

    private double distanceTo(WorldState world, Pose target) {
        Pose pose = world.robotPose();
        return Math.hypot(target.x() - pose.x(), target.y() - pose.y());
    }

    private static double angleDifference(double a, double b) {
        double diff = a - b;
        while (diff > Math.PI) diff -= 2 * Math.PI;
        while (diff < -Math.PI) diff += 2 * Math.PI;
        return diff;
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

        Hive active = world.hiveMap.activeHive();
        telemetry.addData("ACTIVE HIVE", active == null ? "none"
                : active.side + " " + active.state() + String.format(" (conf %.2f)", active.confidence()));

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
        telemetry.addData("APRILTAG", world.hiveCameraAvailable ? "camera OK" : "camera UNAVAILABLE");
        telemetry.addData("TIME REMAINING", String.format("%.1f", world.timeRemainingSeconds));
        telemetry.update();
    }
}
