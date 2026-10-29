package org.firstinspires.ftc.teamcode.OpModes;

import com.pedropathing.math.Pose;
import com.pedropathing.utils.Angle;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.BallType;
import org.firstinspires.ftc.teamcode.BioBuzzAutoConfig;
import org.firstinspires.ftc.teamcode.Cell;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.ScoringState;
import org.firstinspires.ftc.teamcode.StartPosition;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.ArrayList;
import java.util.List;

/** Leaves, shoots the preload, collects one Pollen, shoots again, and parks. */
@Autonomous(name = "BIOBUZZ - Shoot, Flower, Shoot, Park", group = "BIOBUZZ")
public class ShootCollectOnePollenAuto extends OpMode {

    private enum Stage {
        LEAVE_WALL,
        DRIVE_TO_PRELOAD_CELL,
        AIM_PRELOAD,
        DRIVE_TO_PRELOAD_SCORING_POINT,
        AIM_PRELOAD_AT_SCORING_POINT,
        SHOOT_PRELOAD,
        DRIVE_TO_FLOWER,
        INTAKE_FLOWER,
        DRIVE_BACK_TO_CELL,
        AIM_FLOWER_POLLEN,
        DRIVE_TO_FLOWER_SCORING_POINT,
        AIM_FLOWER_POLLEN_AT_SCORING_POINT,
        SHOOT_FLOWER_POLLEN,
        PARK_AROUND_FIELD,
        PARK_IN_LOADING_ZONE,
        DONE,
        ABORTED
    }

    private Robot robot;
    private String initError;
    private String status = "Init";
    private String stopReason;
    private Stage stage = Stage.LEAVE_WALL;
    private StartPosition startPosition = StartPosition.SIDE_WALL;
    private BioBuzzAutoConfig.Flower selectedFlower = BioBuzzAutoConfig.Flower.LEFT_WALL;
    private boolean flowerChoiceLocked;

    private Cell driveCell;
    private Pose aimHoldPose;

    private List<Pose> flowerRoute = new ArrayList<>();
    private int flowerWaypoint;
    private List<Pose> parkRoute = new ArrayList<>();
    private int parkWaypoint;
    private boolean parkRouteRetried;

    private double matchStartSeconds;
    private double stageStartSeconds;
    private boolean started;

    @Override
    public void init() {
        if (Constants.foresightConfig == null) {
            initError = "Foresight is not tuned. Run the Pedro tuners and paste the result into Constants.java.";
            return;
        }

        try {
            robot = new Robot(hardwareMap, telemetry, gamepad1, gamepad2, false);
        } catch (RuntimeException e) {
            initError = "Robot setup failed: " + e.getMessage();
        }
    }

    @Override
    public void init_loop() {
        if (robot == null) {
            showInitError();
            return;
        }

        robot.toggleAlliance();
        selectStartAndFlower();
        robot.setStartPose(startPosition.pose(robot.redAlliance));
        robot.updateWorldModel(getRuntime());

        telemetry.addData("Alliance", robot.redAlliance ? "Red" : "Blue");
        telemetry.addData("Start (D-pad left/right)", startPosition);
        telemetry.addData("Flower (D-pad up/down)", selectedFlower);
        telemetry.addData("Cell camera", robot.cellVision.isAvailable() ? "ready" : "not found; using estimates");
        telemetry.addData("Mechanism motors", robot.mechanism.hasAllMotors() ? "found" : "missing; will park only");
        telemetry.addData("Foresight", "configured");
        telemetry.addLine("Starts are on the perimeter. Check the pose against the field before pressing Play.");
        telemetry.update();
    }

    @Override
    public void start() {
        if (robot == null) return;

        matchStartSeconds = getRuntime();
        stageStartSeconds = matchStartSeconds;
        started = true;
        robot.setStartPose(startPosition.pose(robot.redAlliance));

        if (!robot.mechanism.hasAllMotors()) {
            stopReason = "A mechanism motor is missing";
        } else {
            // The preload starts in the same timed intake/transfer path used for a collected ball.
            robot.mechanism.startIntake(BallType.POLLEN, matchStartSeconds);
        }

        robot.followTo(BioBuzzAutoConfig.leavePose(robot.follower.pose()));
        enter(Stage.LEAVE_WALL, matchStartSeconds, "Leaving the perimeter wall");
    }

    @Override
    public void loop() {
        if (robot == null) {
            showInitError();
            return;
        }

        double now = getRuntime();
        robot.updateWorldModel(now);

        if (started && now - matchStartSeconds >= BioBuzzAutoConfig.AUTO_SAFETY_CUTOFF_SECONDS
                && stage != Stage.DONE && stage != Stage.ABORTED) {
            stopRobot();
            stage = Stage.DONE;
            status = "30-second auto ended; motors stopped";
        } else if (started && now - matchStartSeconds >= BioBuzzAutoConfig.PARK_START_SECONDS
                && stage != Stage.PARK_AROUND_FIELD && stage != Stage.PARK_IN_LOADING_ZONE
                && stage != Stage.DONE && stage != Stage.ABORTED) {
            beginParking(now, "Time reserved for parking");
        }

        switch (stage) {
            case LEAVE_WALL:
                if (!robot.busy()) {
                    if (stopReason != null) {
                        beginParking(now, stopReason);
                    } else {
                        driveCell = chooseCell();
                        driveToCellApproach(now, Stage.DRIVE_TO_PRELOAD_CELL,
                                "Driving to the Cell for the preload");
                    }
                } else if (now - stageStartSeconds >= BioBuzzAutoConfig.LEAVE_TIMEOUT_SECONDS) {
                    abort("Leave path timed out");
                }
                break;

            case DRIVE_TO_PRELOAD_CELL:
                if (retargetIfCellChanged(now, Stage.DRIVE_TO_PRELOAD_CELL)) break;
                if (!robot.busy() && robot.mechanism.state() == ScoringState.READY_TO_SCORE) {
                    aimHoldPose = robot.follower.pose();
                    enter(Stage.AIM_PRELOAD, now, "Aiming at the Cell");
                } else if (now - stageStartSeconds >= 8.0) {
                    abort("Cell approach or preload transfer timed out");
                }
                break;

            case AIM_PRELOAD:
                if (retargetIfCellChanged(now, Stage.DRIVE_TO_PRELOAD_CELL)) break;
                if (aimAtCell(driveCell)) {
                    Pose scorePose = driveCell.computeTarget(robot.follower.pose()).scoringPose;
                    robot.followTo(scorePose);
                    enter(Stage.DRIVE_TO_PRELOAD_SCORING_POINT, now, "Moving to scoring distance");
                } else if (now - stageStartSeconds >= BioBuzzAutoConfig.AIM_TIMEOUT_SECONDS) {
                    abort("Could not aim at the Cell");
                }
                break;

            case DRIVE_TO_PRELOAD_SCORING_POINT:
                if (retargetIfCellChanged(now, Stage.DRIVE_TO_PRELOAD_CELL)) break;
                if (!robot.busy()) {
                    aimHoldPose = robot.follower.pose();
                    enter(Stage.AIM_PRELOAD_AT_SCORING_POINT, now, "Final aim for the preload");
                } else if (now - stageStartSeconds >= 4.0) {
                    abort("Move to scoring distance timed out");
                }
                break;

            case AIM_PRELOAD_AT_SCORING_POINT:
                if (retargetIfCellChanged(now, Stage.DRIVE_TO_PRELOAD_CELL)) break;
                if (aimAtCell(driveCell)) {
                    if (robot.mechanism.triggerOuttake(now)) {
                        enter(Stage.SHOOT_PRELOAD, now, "Firing the preload");
                    } else {
                        abort("Preload was not ready to fire");
                    }
                } else if (now - stageStartSeconds >= BioBuzzAutoConfig.AIM_TIMEOUT_SECONDS) {
                    abort("Final preload aim timed out");
                }
                break;

            case SHOOT_PRELOAD:
                if (robot.mechanism.state() == ScoringState.INTAKE_OFF) {
                    startFlowerRoute(now);
                } else if (now - stageStartSeconds >= 3.0) {
                    abort("Preload outtake timed out");
                }
                break;

            case DRIVE_TO_FLOWER:
                if (!robot.busy()) {
                    followNextFlowerLeg(now);
                } else if (now - stageStartSeconds >= BioBuzzAutoConfig.FLOWER_LEG_TIMEOUT_SECONDS) {
                    abort("Flower route timed out");
                }
                break;

            case INTAKE_FLOWER:
                if (robot.mechanism.state() == ScoringState.READY_TO_SCORE) {
                    driveCell = chooseCell();
                    driveToCellApproach(now, Stage.DRIVE_BACK_TO_CELL,
                            "Returning to the Cell with the collected Pollen");
                } else if (now - stageStartSeconds >= 6.0) {
                    abort("Intake/transfer timed out");
                }
                break;

            case DRIVE_BACK_TO_CELL:
                if (retargetIfCellChanged(now, Stage.DRIVE_BACK_TO_CELL)) break;
                if (!robot.busy()) {
                    aimHoldPose = robot.follower.pose();
                    enter(Stage.AIM_FLOWER_POLLEN, now, "Aiming with the collected Pollen");
                } else if (now - stageStartSeconds >= 8.0) {
                    abort("Return to the Cell timed out");
                }
                break;

            case AIM_FLOWER_POLLEN:
                if (retargetIfCellChanged(now, Stage.DRIVE_BACK_TO_CELL)) break;
                if (aimAtCell(driveCell)) {
                    Pose scorePose = driveCell.computeTarget(robot.follower.pose()).scoringPose;
                    robot.followTo(scorePose);
                    enter(Stage.DRIVE_TO_FLOWER_SCORING_POINT, now, "Moving to scoring distance");
                } else if (now - stageStartSeconds >= BioBuzzAutoConfig.AIM_TIMEOUT_SECONDS) {
                    abort("Could not aim at the Cell");
                }
                break;

            case DRIVE_TO_FLOWER_SCORING_POINT:
                if (retargetIfCellChanged(now, Stage.DRIVE_BACK_TO_CELL)) break;
                if (!robot.busy()) {
                    aimHoldPose = robot.follower.pose();
                    enter(Stage.AIM_FLOWER_POLLEN_AT_SCORING_POINT, now,
                            "Final aim for the collected Pollen");
                } else if (now - stageStartSeconds >= 4.0) {
                    abort("Move to scoring distance timed out");
                }
                break;

            case AIM_FLOWER_POLLEN_AT_SCORING_POINT:
                if (retargetIfCellChanged(now, Stage.DRIVE_BACK_TO_CELL)) break;
                if (aimAtCell(driveCell)) {
                    if (robot.mechanism.triggerOuttake(now)) {
                        enter(Stage.SHOOT_FLOWER_POLLEN, now, "Firing the collected Pollen");
                    } else {
                        abort("Collected Pollen was not ready to fire");
                    }
                } else if (now - stageStartSeconds >= BioBuzzAutoConfig.AIM_TIMEOUT_SECONDS) {
                    abort("Final aim for the collected Pollen timed out");
                }
                break;

            case SHOOT_FLOWER_POLLEN:
                if (robot.mechanism.state() == ScoringState.INTAKE_OFF) {
                    beginParking(now, "Second shot finished");
                } else if (now - stageStartSeconds >= 3.0) {
                    abort("Second outtake timed out");
                }
                break;

            case PARK_AROUND_FIELD:
                if (!robot.busy()) {
                    followNextParkLeg(now);
                } else if (now - stageStartSeconds >= BioBuzzAutoConfig.PARK_LANE_TIMEOUT_SECONDS) {
                    robot.follower.stop();
                    if (!parkRouteRetried) {
                        parkRouteRetried = true;
                        parkRoute = BioBuzzAutoConfig.parkingLaneRoute(robot.redAlliance, robot.follower.pose());
                        parkWaypoint = 0;
                        followNextParkLeg(now);
                    } else {
                        stage = Stage.ABORTED;
                        status = "Park route timed out; stopped";
                    }
                }
                break;

            case PARK_IN_LOADING_ZONE:
                if (!robot.busy()) {
                    robot.follower.stop();
                    stage = Stage.DONE;
                    status = "Park path ended";
                } else if (now - stageStartSeconds >= BioBuzzAutoConfig.PARK_TIMEOUT_SECONDS) {
                    robot.follower.stop();
                    stage = Stage.DONE;
                    status = "Loading Zone approach timed out; stopped";
                }
                break;

            case DONE:
            case ABORTED:
                break;
        }

        Cell cell = driveCell;
        telemetry.addData("Auto", "Shoot preload, collect one Pollen, shoot, park");
        telemetry.addData("Stage", stage);
        telemetry.addData("Status", status);
        telemetry.addData("Elapsed", String.format("%.1f s", now - matchStartSeconds));
        telemetry.addData("Start", startPosition);
        telemetry.addData("Flower", selectedFlower);
        telemetry.addData("Cell target", cell == null ? "none" : cell.alliance + " " + cell.fieldSide + " " + cell.state());
        telemetry.addData("Target warning", targetWarning());
        telemetry.addData("Mechanism", robot.mechanism.state());
        telemetry.addData("Pose", robot.follower.pose());
        telemetry.addData("Loading Zone", BioBuzzAutoConfig.loadingZonePose(robot.redAlliance));
        telemetry.update();
    }

    // The Flower choice stays put after a D-pad change; otherwise use the closest Flower to the start.
    private void selectStartAndFlower() {
        StartPosition[] starts = StartPosition.values();
        int startIndex = startPosition.ordinal();

        if (gamepad1.dpadRightWasPressed()) {
            startIndex = (startIndex + 1) % starts.length;
        } else if (gamepad1.dpadLeftWasPressed()) {
            startIndex = (startIndex - 1 + starts.length) % starts.length;
        }
        startPosition = starts[startIndex];

        if (!flowerChoiceLocked) {
            selectedFlower = BioBuzzAutoConfig.closestFlower(startPosition.pose(robot.redAlliance));
        }

        BioBuzzAutoConfig.Flower[] flowers = BioBuzzAutoConfig.Flower.values();
        int flowerIndex = selectedFlower.ordinal();
        if (gamepad1.dpadUpWasPressed()) {
            selectedFlower = flowers[(flowerIndex + 1) % flowers.length];
            flowerChoiceLocked = true;
        } else if (gamepad1.dpadDownWasPressed()) {
            selectedFlower = flowers[(flowerIndex - 1 + flowers.length) % flowers.length];
            flowerChoiceLocked = true;
        }
    }

    // Prefer a fresh tag; when neither Cell is tracked, use the nearer field estimate.
    private Cell chooseCell() {
        Cell tracked = robot.cellMap.bestTrackedOurCell();
        if (tracked != null) return tracked;
        return robot.cellMap.bestOurCell(robot.follower.pose());
    }

    private boolean retargetIfCellChanged(double now, Stage returnStage) {
        Cell observed = robot.cellMap.bestTrackedOurCell();
        if (observed == null || observed == driveCell) return false;

        driveCell = observed;
        driveToCellApproach(now, returnStage, "New Cell tag seen; adjusting the route");
        return true;
    }

    private void driveToCellApproach(double now, Stage nextStage, String message) {
        Cell cell = driveCell == null ? chooseCell() : driveCell;
        driveCell = cell;
        robot.followTo(cell.computeTarget(robot.follower.pose()).approachPose);
        enter(nextStage, now, message);
    }

    private boolean aimAtCell(Cell cell) {
        if (cell == null || aimHoldPose == null) return false;

        Pose pose = robot.follower.pose();
        double heading = robot.headingTo(cell.fieldPose().x(), cell.fieldPose().y());
        robot.follower.hold(new Pose(aimHoldPose.x(), aimHoldPose.y(), heading));

        return Math.abs(Angle.error(pose.heading(), heading))
                <= BioBuzzAutoConfig.AIM_TOLERANCE_RADIANS;
    }

    private void startFlowerRoute(double now) {
        flowerRoute = BioBuzzAutoConfig.flowerRoute(robot.follower.pose(), selectedFlower);
        flowerWaypoint = 0;
        followNextFlowerLeg(now);
    }

    // Follow one waypoint at a time so the robot reaches the wall before intaking.
    private void followNextFlowerLeg(double now) {
        if (flowerWaypoint < flowerRoute.size()) {
            robot.followTo(flowerRoute.get(flowerWaypoint++));
            enter(Stage.DRIVE_TO_FLOWER, now,
                    "Following the route to " + selectedFlower);
        } else {
            robot.mechanism.startIntake(BallType.POLLEN, now);
            enter(Stage.INTAKE_FLOWER, now, "Intaking one Pollen");
        }
    }

    // Stop scoring before starting the route back to the alliance Loading Zone.
    private void beginParking(double now, String reason) {
        if (stage == Stage.PARK_AROUND_FIELD || stage == Stage.PARK_IN_LOADING_ZONE
                || stage == Stage.DONE || stage == Stage.ABORTED) return;

        robot.follower.stop();
        robot.mechanism.setManualOverride(true, now);
        parkRoute = BioBuzzAutoConfig.parkingLaneRoute(robot.redAlliance, robot.follower.pose());
        parkWaypoint = 0;
        parkRouteRetried = false;
        status = reason + "; parking";
        followNextParkLeg(now);
    }

    private void followNextParkLeg(double now) {
        if (parkWaypoint < parkRoute.size()) {
            robot.followTo(parkRoute.get(parkWaypoint++));
            enter(Stage.PARK_AROUND_FIELD, now, status);
        } else {
            robot.followTo(BioBuzzAutoConfig.loadingZonePose(robot.redAlliance));
            enter(Stage.PARK_IN_LOADING_ZONE, now, "Entering the alliance Loading Zone");
        }
    }

    private void abort(String reason) {
        double now = getRuntime();
        if (now - matchStartSeconds < BioBuzzAutoConfig.PARK_START_SECONDS) {
            beginParking(now, reason);
        } else {
            stopRobot();
            stage = Stage.ABORTED;
            status = reason + "; motors stopped";
        }
    }

    private String targetWarning() {
        if (!robot.cellVision.isAvailable()) return "Cell camera missing; aiming from field estimates";
        if (driveCell == null) return "No Cell target";
        if (driveCell.state() == org.firstinspires.ftc.teamcode.CellState.UNKNOWN) {
            return "No Cell tag yet; using the nearest nominal Cell";
        }
        if (driveCell.state() == org.firstinspires.ftc.teamcode.CellState.STALE) {
            return "Cell tag is stale; using its last known position";
        }
        return "none";
    }

    private void enter(Stage next, double now, String message) {
        stage = next;
        stageStartSeconds = now;
        status = message;
    }

    private void stopRobot() {
        robot.follower.stop();
        robot.mechanism.setManualOverride(true, getRuntime());
    }

    @Override
    public void stop() {
        if (robot != null) robot.stop();
    }

    private void showInitError() {
        telemetry.addLine("AUTO NOT READY");
        telemetry.addLine(initError == null ? "Robot setup failed." : initError);
        telemetry.update();
    }
}
