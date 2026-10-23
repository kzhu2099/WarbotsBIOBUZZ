package org.firstinspires.ftc.teamcode.OpModes;

import com.pedropathing.math.Pose;
import com.pedropathing.utils.Angle;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.BallType;
import org.firstinspires.ftc.teamcode.BioBuzzAutoConfig;
import org.firstinspires.ftc.teamcode.Cell;
import org.firstinspires.ftc.teamcode.CellState;
import org.firstinspires.ftc.teamcode.CellTarget;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.ScoringState;
import org.firstinspires.ftc.teamcode.StartPosition;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.List;

@Autonomous(name = "BIOBUZZ - Shoot, Collect 1 Pollen, Shoot, Park", group = "BIOBUZZ")
public class ShootCollectOnePollenAuto extends OpMode {

    private enum Stage {
        LEAVE_WALL,
        DRIVE_TO_CELL_PRELOAD,
        AIM_PRELOAD,
        DRIVE_TO_SCORE_PRELOAD,
        AIM_PRELOAD_AT_SCORE,
        SHOOT_PRELOAD,
        DRIVE_TO_FLOWER,
        INTAKE_FLOWER,
        DRIVE_TO_CELL_POLLEN,
        AIM_FLOWER_POLLEN,
        DRIVE_TO_SCORE_POLLEN,
        AIM_FLOWER_POLLEN_AT_SCORE,
        SHOOT_FLOWER_POLLEN,
        PARK_TO_LANE,
        PARK_TO_ZONE,
        DONE,
        ABORTED
    }

    private Robot robot;
    private String initError;
    private String status = "INIT";
    private String skipScoringReason;
    private Stage stage = Stage.LEAVE_WALL;
    private Cell targetCell;
    private Cell driveCell;
    private BioBuzzAutoConfig.Flower selectedFlower;
    private Pose aimHoldPose;
    private double matchStartSeconds;
    private double stageStartSeconds;
    private int retargets;
    private boolean started;
    private StartPosition startPosition = StartPosition.SIDE_WALL;
    private List<Pose> parkingRoute;
    private int parkingWaypointIndex;
    private boolean replannedParkRoute;

    @Override
    public void init() {
        if (Constants.foresightConfig == null) {
            initError = "ForesightConfig is missing. Run the Pedro tuners before using this auto.";
            return;
        }

        try {
            robot = new Robot(hardwareMap, telemetry, gamepad1, gamepad2, false);
        } catch (RuntimeException e) {
            initError = "Robot initialization failed: " + e.getMessage();
        }
    }

    @Override
    public void init_loop() {
        if (robot == null) {
            showInitError();
            return;
        }

        robot.toggleAlliance();
        selectStartPosition();
        robot.setStartPose(startPosition.pose(robot.redAlliance));
        robot.updateWorldModel(getRuntime());

        telemetry.addData("Alliance", robot.redAlliance ? "Red" : "Blue");
        telemetry.addData("Start (dpad)", startPosition);
        telemetry.addData("Start pose", robot.follower.pose());
        telemetry.addData("Cell camera", robot.cellVision.isAvailable() ? "available" : "missing");
        telemetry.addData("Mechanism motors", robot.mechanism.hasAllMotors() ? "found" : "check Driver Station names");
        telemetry.addData("Park target", BioBuzzAutoConfig.loadingZonePose(robot.redAlliance));
        telemetry.addLine("D-pad left/right changes the perimeter start.");
        telemetry.addLine("Check the start and park poses against the taped field.");
        telemetry.update();
    }

    @Override
    public void start() {
        if (robot == null) return;

        matchStartSeconds = getRuntime();
        stageStartSeconds = matchStartSeconds;
        robot.setStartPose(startPosition.pose(robot.redAlliance));

        if (!robot.cellVision.isAvailable()) {
            skipScoringReason = "Cell camera is unavailable";
        } else if (!robot.mechanism.hasAllMotors()) {
            skipScoringReason = "One or more mechanism motors are missing from the hardware configuration";
        } else {
            robot.mechanism.startIntake(BallType.POLLEN, matchStartSeconds);
        }

        robot.followTo(BioBuzzAutoConfig.leavePose(robot.follower.pose()));
        status = "Leaving the wall";
        started = true;
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
            robot.follower.stop();
            robot.mechanism.setManualOverride(true, now);
            stage = Stage.ABORTED;
            status = "Safety cutoff reached; motors stopped";
        } else if (started && now - matchStartSeconds >= BioBuzzAutoConfig.PARK_START_SECONDS
                && stage != Stage.PARK_TO_LANE && stage != Stage.PARK_TO_ZONE
                && stage != Stage.DONE && stage != Stage.ABORTED) {
            beginParking(now, "Parking with the remaining time");
        }

        switch (stage) {
            case LEAVE_WALL:
                if (!robot.busy()) {
                    if (skipScoringReason != null) {
                        beginParking(now, skipScoringReason);
                    } else {
                        targetCell = robot.cellMap.bestOurCell(robot.follower.pose());
                        driveCell = targetCell;
                        followToCellApproach(now);
                        enter(Stage.DRIVE_TO_CELL_PRELOAD, now,
                                "Approaching the estimated Cell while scanning for its tag");
                    }
                } else if (now - stageStartSeconds > BioBuzzAutoConfig.LEAVE_TIMEOUT_SECONDS) {
                    abort("Leave path timed out");
                }
                break;

            case DRIVE_TO_CELL_PRELOAD:
                if (now - stageStartSeconds > 8.0) {
                    abort("Cell approach timed out");
                } else if (!robot.busy() && robot.mechanism.state() == ScoringState.READY_TO_SCORE) {
                    Cell liveCell = robot.cellMap.bestOurCell(robot.follower.pose());
                    if (!hasUsableCell(liveCell)) {
                        abort("Cell tracking was lost before the preload shot");
                    } else if (liveCell != driveCell) {
                        retargetCell(liveCell, now, Stage.DRIVE_TO_CELL_PRELOAD);
                    } else {
                        targetCell = liveCell;
                        aimHoldPose = robot.follower.pose();
                        enter(Stage.AIM_PRELOAD, now, "Aiming at the tracked Cell");
                    }
                }
                break;

            case AIM_PRELOAD:
                targetCell = robot.cellMap.bestOurCell(robot.follower.pose());
                if (!hasUsableCell(targetCell)) {
                    abort("Cell tracking was lost before the preload shot");
                } else if (targetCell != driveCell) {
                    retargetCell(targetCell, now, Stage.DRIVE_TO_CELL_PRELOAD);
                } else if (aimAtTarget(targetCell)) {
                    CellTarget scoringTarget = driveCell.computeTarget(robot.follower.pose());
                    robot.followTo(scoringTarget.scoringPose);
                    enter(Stage.DRIVE_TO_SCORE_PRELOAD, now, "Moving to scoring distance");
                } else if (now - stageStartSeconds > BioBuzzAutoConfig.AIM_TIMEOUT_SECONDS) {
                    abort("Preload aim timed out");
                }
                break;

            case DRIVE_TO_SCORE_PRELOAD:
                if (now - stageStartSeconds > 4.0) {
                    abort("Move to scoring distance timed out");
                } else if (!robot.busy()) {
                    Cell liveCell = robot.cellMap.bestOurCell(robot.follower.pose());
                    if (!hasUsableCell(liveCell)) {
                        abort("Cell tracking was lost at scoring distance");
                    } else if (liveCell != driveCell) {
                        retargetCell(liveCell, now, Stage.DRIVE_TO_CELL_PRELOAD);
                    } else {
                        targetCell = liveCell;
                        aimHoldPose = robot.follower.pose();
                        enter(Stage.AIM_PRELOAD_AT_SCORE, now, "Aiming at scoring distance");
                    }
                }
                break;

            case AIM_PRELOAD_AT_SCORE:
                targetCell = robot.cellMap.bestOurCell(robot.follower.pose());
                if (!hasUsableCell(targetCell)) {
                    abort("Cell tracking was lost before the preload shot");
                } else if (targetCell != driveCell) {
                    retargetCell(targetCell, now, Stage.DRIVE_TO_CELL_PRELOAD);
                } else if (aimAtTarget(targetCell)) {
                    if (robot.mechanism.triggerOuttake(now)) {
                        enter(Stage.SHOOT_PRELOAD, now, "Shooting the preload");
                    } else {
                        abort("Preload was not ready to shoot");
                    }
                } else if (now - stageStartSeconds > BioBuzzAutoConfig.AIM_TIMEOUT_SECONDS) {
                    abort("Preload final aim timed out");
                }
                break;

            case SHOOT_PRELOAD:
                if (robot.mechanism.state() == ScoringState.INTAKE_OFF) {
                    selectedFlower = BioBuzzAutoConfig.closestFlower(robot.follower.pose());
                    robot.followTo(BioBuzzAutoConfig.flowerApproachPose(selectedFlower));
                    enter(Stage.DRIVE_TO_FLOWER, now, "Driving to " + selectedFlower);
                } else if (now - stageStartSeconds > 3.0) {
                    abort("Preload outtake timed out");
                }
                break;

            case DRIVE_TO_FLOWER:
                if (now - stageStartSeconds > 8.0) {
                    abort("Flower approach timed out");
                } else if (!robot.busy()) {
                    robot.mechanism.startIntake(BallType.POLLEN, now);
                    enter(Stage.INTAKE_FLOWER, now, "Intaking one Pollen");
                }
                break;

            case INTAKE_FLOWER:
                if (robot.mechanism.state() == ScoringState.READY_TO_SCORE) {
                    targetCell = robot.cellMap.bestOurCell(robot.follower.pose());
                    if (!hasUsableCell(targetCell)) {
                        abort("No usable Cell tag on return from the Flower");
                    } else {
                        driveCell = targetCell;
                        retargets = 0;
                        followToCellApproach(now);
                        enter(Stage.DRIVE_TO_CELL_POLLEN, now, "Returning to the Cell");
                    }
                } else if (now - stageStartSeconds > 6.0) {
                    abort("Intake and transfer did not finish");
                }
                break;

            case DRIVE_TO_CELL_POLLEN:
                if (now - stageStartSeconds > 8.0) {
                    abort("Return to the Cell timed out");
                } else if (!robot.busy()) {
                    Cell liveCell = robot.cellMap.bestOurCell(robot.follower.pose());
                    if (!hasUsableCell(liveCell)) {
                        abort("Cell tracking was lost on return");
                    } else if (liveCell != driveCell) {
                        retargetCell(liveCell, now, Stage.DRIVE_TO_CELL_POLLEN);
                    } else {
                        targetCell = liveCell;
                        aimHoldPose = robot.follower.pose();
                        enter(Stage.AIM_FLOWER_POLLEN, now, "Aiming at the Cell");
                    }
                }
                break;

            case AIM_FLOWER_POLLEN:
                targetCell = robot.cellMap.bestOurCell(robot.follower.pose());
                if (!hasUsableCell(targetCell)) {
                    abort("Cell tracking was lost before the second shot");
                } else if (targetCell != driveCell) {
                    retargetCell(targetCell, now, Stage.DRIVE_TO_CELL_POLLEN);
                } else if (aimAtTarget(targetCell)) {
                    CellTarget scoringTarget = driveCell.computeTarget(robot.follower.pose());
                    robot.followTo(scoringTarget.scoringPose);
                    enter(Stage.DRIVE_TO_SCORE_POLLEN, now, "Moving to scoring distance");
                } else if (now - stageStartSeconds > BioBuzzAutoConfig.AIM_TIMEOUT_SECONDS) {
                    abort("Second shot aim timed out");
                }
                break;

            case DRIVE_TO_SCORE_POLLEN:
                if (now - stageStartSeconds > 4.0) {
                    abort("Move to scoring distance timed out");
                } else if (!robot.busy()) {
                    Cell liveCell = robot.cellMap.bestOurCell(robot.follower.pose());
                    if (!hasUsableCell(liveCell)) {
                        abort("Cell tracking was lost at scoring distance");
                    } else if (liveCell != driveCell) {
                        retargetCell(liveCell, now, Stage.DRIVE_TO_CELL_POLLEN);
                    } else {
                        targetCell = liveCell;
                        aimHoldPose = robot.follower.pose();
                        enter(Stage.AIM_FLOWER_POLLEN_AT_SCORE, now, "Aiming at scoring distance");
                    }
                }
                break;

            case AIM_FLOWER_POLLEN_AT_SCORE:
                targetCell = robot.cellMap.bestOurCell(robot.follower.pose());
                if (!hasUsableCell(targetCell)) {
                    abort("Cell tracking was lost before the second shot");
                } else if (targetCell != driveCell) {
                    retargetCell(targetCell, now, Stage.DRIVE_TO_CELL_POLLEN);
                } else if (aimAtTarget(targetCell)) {
                    if (robot.mechanism.triggerOuttake(now)) {
                        enter(Stage.SHOOT_FLOWER_POLLEN, now, "Shooting the collected Pollen");
                    } else {
                        abort("Collected Pollen was not ready to shoot");
                    }
                } else if (now - stageStartSeconds > BioBuzzAutoConfig.AIM_TIMEOUT_SECONDS) {
                    abort("Second shot final aim timed out");
                }
                break;

            case SHOOT_FLOWER_POLLEN:
                if (robot.mechanism.state() == ScoringState.INTAKE_OFF) {
                    beginParking(now, "Scoring sequence finished");
                } else if (now - stageStartSeconds > 3.0) {
                    abort("Second outtake timed out");
                }
                break;

            case PARK_TO_LANE:
                if (!robot.busy()) {
                    followNextParkingLeg(now);
                } else if (now - stageStartSeconds >= BioBuzzAutoConfig.PARK_LANE_TIMEOUT_SECONDS) {
                    robot.follower.stop();
                    if (!replannedParkRoute) {
                        replannedParkRoute = true;
                        parkingRoute = BioBuzzAutoConfig.parkingLaneRoute(robot.redAlliance, robot.follower.pose());
                        parkingWaypointIndex = 0;
                        status = "Replanning the park route";
                        followNextParkingLeg(now);
                    } else {
                        stage = Stage.ABORTED;
                        status = "Perimeter route timed out; stopped";
                    }
                }
                break;

            case PARK_TO_ZONE:
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

            default:
                break;
        }

        telemetry.addData("Auto", "Shoot, Collect 1 Pollen, Shoot, Park");
        telemetry.addData("Stage", stage);
        telemetry.addData("Status", status);
        telemetry.addData("Elapsed", String.format("%.1f s", now - matchStartSeconds));
        telemetry.addData("Pose", robot.follower.pose());
        telemetry.addData("Mechanism", robot.mechanism.state());
        telemetry.addData("Target Cell", targetCell == null ? "none" : targetCell.toString());
        telemetry.addData("Flower", selectedFlower == null ? "none" : selectedFlower);
        telemetry.addData("Cell camera", robot.cellVision.isAvailable() ? "available" : "missing");
        telemetry.addData("Mechanism motors", robot.mechanism.hasAllMotors() ? "found" : "missing");
        telemetry.addData("Park target", BioBuzzAutoConfig.loadingZonePose(robot.redAlliance));
        telemetry.addLine("Mechanism timing is estimated; collection and scoring are not sensor-confirmed.");
        telemetry.update();
    }

    private void followToCellApproach(double now) {
        CellTarget target = driveCell.computeTarget(robot.follower.pose());
        robot.followTo(target.approachPose);
        stageStartSeconds = now;
    }

    private void retargetCell(Cell newCell, double now, Stage returnStage) {
        if (retargets >= 2) {
            abort("Cell target kept changing");
            return;
        }
        retargets++;
        driveCell = newCell;
        targetCell = newCell;
        followToCellApproach(now);
        enter(returnStage, now, "Repositioning for the tracked Cell");
    }

    private boolean hasUsableCell(Cell cell) {
        return cell != null
                && cell.hasEverBeenDetected()
                && cell.state() != CellState.UNKNOWN
                && cell.state() != CellState.STALE;
    }

    private boolean aimAtTarget(Cell cell) {
        if (cell == null || aimHoldPose == null) return false;

        Pose pose = robot.follower.pose();
        double heading = robot.headingTo(cell.fieldPose().x(), cell.fieldPose().y());
        robot.follower.hold(new Pose(aimHoldPose.x(), aimHoldPose.y(), heading));
        return Math.abs(Angle.error(pose.heading(), heading)) <= BioBuzzAutoConfig.AIM_TOLERANCE_RADIANS;
    }

    private void enter(Stage next, double now, String description) {
        stage = next;
        stageStartSeconds = now;
        status = description;
    }

    private void abort(String reason) {
        double now = getRuntime();
        if (now - matchStartSeconds < BioBuzzAutoConfig.PARK_START_SECONDS
                && stage != Stage.PARK_TO_LANE && stage != Stage.PARK_TO_ZONE) {
            beginParking(now, reason);
        } else {
            robot.follower.stop();
            robot.mechanism.setManualOverride(true, now);
            stage = Stage.ABORTED;
            status = reason + "; motors stopped";
        }
    }

    private void selectStartPosition() {
        StartPosition[] options = StartPosition.values();
        int index = startPosition.ordinal();

        if (gamepad1.dpadRightWasPressed()) {
            index = (index + 1) % options.length;
        } else if (gamepad1.dpadLeftWasPressed()) {
            index = (index - 1 + options.length) % options.length;
        }
        startPosition = options[index];
    }

    private void beginParking(double now, String reason) {
        if (stage == Stage.PARK_TO_LANE || stage == Stage.PARK_TO_ZONE || stage == Stage.DONE) return;

        robot.follower.stop();
        robot.mechanism.setManualOverride(true, now);
        parkingRoute = BioBuzzAutoConfig.parkingLaneRoute(robot.redAlliance, robot.follower.pose());
        parkingWaypointIndex = 0;
        replannedParkRoute = false;
        followNextParkingLeg(now);
        status = reason + "; parking";
    }

    private void followNextParkingLeg(double now) {
        if (parkingWaypointIndex < parkingRoute.size()) {
            robot.followTo(parkingRoute.get(parkingWaypointIndex++));
            enter(Stage.PARK_TO_LANE, now, "Following the perimeter route");
        } else {
            robot.followTo(BioBuzzAutoConfig.loadingZonePose(robot.redAlliance));
            enter(Stage.PARK_TO_ZONE, now, "Entering the alliance Loading Zone");
        }
    }

    @Override
    public void stop() {
        if (robot != null) robot.stop();
    }

    private void showInitError() {
        telemetry.addLine("AUTO NOT READY");
        telemetry.addLine(initError == null ? "Robot did not initialize." : initError);
        telemetry.update();
    }
}
