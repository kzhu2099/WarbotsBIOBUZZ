package org.firstinspires.ftc.teamcode.OpModes;

import com.pedropathing.math.Pose;
import com.pedropathing.utils.Angle;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.BallType;
import org.firstinspires.ftc.teamcode.BioBuzzAutoConfig;
import org.firstinspires.ftc.teamcode.Cell;
import org.firstinspires.ftc.teamcode.CellTarget;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.ScoringState;
import org.firstinspires.ftc.teamcode.CellState;
import org.firstinspires.ftc.teamcode.StartPosition;
import org.firstinspires.ftc.teamcode.pedro.Constants;

@Autonomous(name = "BIOBUZZ - Shoot, Collect 1 Pollen, Shoot", group = "BIOBUZZ")
public class ShootCollectOnePollenAuto extends OpMode {

    private enum Stage {
        FEED_PRELOAD_AND_DRIVE,
        AIM_PRELOAD,
        SHOOT_PRELOAD,
        DRIVE_TO_FLOWER,
        INTAKE_FLOWER,
        DRIVE_TO_CELL,
        AIM_FLOWER_POLLEN,
        SHOOT_FLOWER_POLLEN,
        DONE,
        ABORTED
    }

    private Robot robot;
    private String initError;
    private String status = "INIT";
    private Stage stage = Stage.FEED_PRELOAD_AND_DRIVE;
    private Cell targetCell;
    private Cell driveCell;
    private Pose aimHoldPose;
    private double matchStartSeconds;
    private double stageStartSeconds;
    private int retargets;
    private boolean started;

    @Override
    public void init() {
        if (Constants.foresightConfig == null) {
            initError = "ForesightConfig is null. Tune Foresight and copy its generated config into Constants.java.";
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
        robot.setStartPose(StartPosition.LOADING_ZONE.pose(robot.redAlliance));
        robot.updateWorldModel(getRuntime());
        telemetry.addData("Status", "Ready");
        telemetry.addData("Alliance", robot.redAlliance ? "RED" : "BLUE");
        telemetry.addData("Start pose", robot.follower.pose());
        telemetry.addData("Cell camera", robot.cellVision.isAvailable() ? "available" : "unavailable");
        telemetry.addLine("VERIFY THESE VALUES: start pose and Flower approach.");
        telemetry.update();
    }

    @Override
    public void start() {
        if (robot == null) return;

        matchStartSeconds = getRuntime();
        if (!robot.cellVision.isAvailable()) {
            stage = Stage.ABORTED;
            status = "Cell camera unavailable; unable to confirm the upward-facing scoring Cell.";
            return;
        }

        Pose start = StartPosition.LOADING_ZONE.pose(robot.redAlliance);
        robot.setStartPose(start);
        robot.updateWorldModel(matchStartSeconds);

        targetCell = robot.cellMap.bestOurCell(start);
        driveCell = targetCell;
        CellTarget target = driveCell.computeTarget(start);
        robot.mechanism.startIntake(BallType.POLLEN, matchStartSeconds);
        robot.follower.follow(robot.pathTo(target.approachPose));
        enter(Stage.FEED_PRELOAD_AND_DRIVE, matchStartSeconds,
                "Feed one preload while approaching the alliance Cell");
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

        if (started && stage != Stage.DONE && stage != Stage.ABORTED
                && now - matchStartSeconds >= BioBuzzAutoConfig.AUTO_SAFETY_CUTOFF_SECONDS) {
            abort("Safety cutoff reached before the 30-second autonomous period ends.");
        }

        if (stage != Stage.DONE && stage != Stage.ABORTED) {
            switch (stage) {
                case FEED_PRELOAD_AND_DRIVE:
                    if (now - stageStartSeconds > 8.0) {
                        abort("Preload feed or Cell approach timed out.");
                        break;
                    }
                    if (!robot.busy() && robot.mechanism.state() == ScoringState.READY_TO_SCORE) {
                        Cell liveCell = robot.cellMap.bestOurCell(robot.follower.pose());
                        if (!hasFreshCellSighting(liveCell)) {
                            abort("No alliance Cell tag sighting; refusing to shoot at an estimated target.");
                            break;
                        }
                        if (liveCell != driveCell) {
                            if (retargets >= 2) {
                                abort("Active Cell changed repeatedly; stopping instead of shooting from an unverified position.");
                                break;
                            }
                            driveCell = liveCell;
                            targetCell = liveCell;
                            retargets++;
                            followToCellApproach(now);
                            enter(Stage.FEED_PRELOAD_AND_DRIVE, now, "Repositioning for the current Cell");
                            break;
                        }
                        targetCell = liveCell;
                        aimHoldPose = robot.follower.pose();
                        enter(Stage.AIM_PRELOAD, now, "Aiming at the current upward-facing Cell");
                    }
                    break;

                case AIM_PRELOAD:
                    targetCell = robot.cellMap.bestOurCell(robot.follower.pose());
                    if (targetCell != driveCell) {
                        if (retargets >= 2) {
                            abort("Active Cell changed repeatedly; stopping instead of shooting from an unverified position.");
                            break;
                        }
                        driveCell = targetCell;
                        retargets++;
                        followToCellApproach(now);
                        enter(Stage.FEED_PRELOAD_AND_DRIVE, now, "Repositioning for the current Cell");
                        break;
                    }
                    if (aimAtTarget(targetCell)) {
                        if (robot.mechanism.triggerOuttake(now)) {
                            enter(Stage.SHOOT_PRELOAD, now, "Shooting one preloaded Pollen");
                        } else {
                            abort("Preload was not in READY_TO_SCORE state.");
                        }
                    } else if (now - stageStartSeconds > BioBuzzAutoConfig.AIM_TIMEOUT_SECONDS) {
                        abort("Could not aim at the Cell within the allowed time.");
                    }
                    break;

                case SHOOT_PRELOAD:
                    if (now - stageStartSeconds > 3.0) {
                        abort("Preload outtake timed out.");
                    } else if (robot.mechanism.state() == ScoringState.INTAKE_OFF) {
                        retargets = 0;
                        robot.followTo(BioBuzzAutoConfig.flowerApproachPose(robot.redAlliance));
                        enter(Stage.DRIVE_TO_FLOWER, now, "Driving to the nearest side-wall Flower");
                    }
                    break;

                case DRIVE_TO_FLOWER:
                    if (now - stageStartSeconds > 9.0) {
                        abort("Flower approach timed out.");
                    } else if (!robot.busy()) {
                        robot.mechanism.startIntake(BallType.POLLEN, now);
                        enter(Stage.INTAKE_FLOWER, now, "Intaking one Pollen from the Flower");
                    }
                    break;

                case INTAKE_FLOWER:
                    if (now - stageStartSeconds > 6.0) {
                        abort("Timed Flower intake did not reach READY_TO_SCORE.");
                    } else if (robot.mechanism.state() == ScoringState.READY_TO_SCORE) {
                        targetCell = robot.cellMap.bestOurCell(robot.follower.pose());
                        driveCell = targetCell;
                        retargets = 0;
                        followToCellApproach(now);
                        enter(Stage.DRIVE_TO_CELL, now, "Returning to the current alliance Cell");
                    } else if (robot.mechanism.state() == ScoringState.FAULT
                            || robot.mechanism.state() == ScoringState.INTAKE_OFF) {
                        abort("Flower intake ended without a ready Pollen.");
                    }
                    break;

                case DRIVE_TO_CELL:
                    if (now - stageStartSeconds > 8.0) {
                        abort("Return to Cell timed out.");
                    } else if (!robot.busy()) {
                        Cell liveCell = robot.cellMap.bestOurCell(robot.follower.pose());
                        if (liveCell == null || !liveCell.hasEverBeenDetected()) {
                            abort("No alliance Cell tag sighting on return; refusing to shoot at an estimated target.");
                            break;
                        }
                        if (liveCell != driveCell) {
                            if (retargets >= 2) {
                                abort("Active Cell changed repeatedly; stopping instead of shooting from an unverified position.");
                                break;
                            }
                            driveCell = liveCell;
                            targetCell = liveCell;
                            retargets++;
                            followToCellApproach(now);
                        } else {
                            targetCell = liveCell;
                            aimHoldPose = robot.follower.pose();
                            enter(Stage.AIM_FLOWER_POLLEN, now, "Aiming the collected Pollen");
                        }
                    }
                    break;

                case AIM_FLOWER_POLLEN:
                    targetCell = robot.cellMap.bestOurCell(robot.follower.pose());
                    if (targetCell != driveCell) {
                        if (retargets >= 2) {
                            abort("Active Cell changed repeatedly; stopping instead of shooting from an unverified position.");
                            break;
                        }
                        driveCell = targetCell;
                        retargets++;
                        followToCellApproach(now);
                        enter(Stage.DRIVE_TO_CELL, now, "Repositioning for the current Cell");
                        break;
                    }
                    if (aimAtTarget(targetCell)) {
                        if (robot.mechanism.triggerOuttake(now)) {
                            enter(Stage.SHOOT_FLOWER_POLLEN, now, "Shooting the collected Pollen");
                        } else {
                            abort("Collected Pollen was not in READY_TO_SCORE state.");
                        }
                    } else if (now - stageStartSeconds > BioBuzzAutoConfig.AIM_TIMEOUT_SECONDS) {
                        abort("Could not aim at the Cell within the allowed time.");
                    }
                    break;

                case SHOOT_FLOWER_POLLEN:
                    if (now - stageStartSeconds > 3.0) {
                        abort("Collected-Pollen outtake timed out.");
                    } else if (robot.mechanism.state() == ScoringState.INTAKE_OFF) {
                        robot.follower.stop();
                        robot.mechanism.stopAll();
                        enter(Stage.DONE, now,
                                "Both scoring attempts completed; physical scoring is not sensor-verified");
                    }
                    break;

                default:
                    break;
            }
        }

        telemetry.addData("Auto", "Shoot, Collect 1 Pollen, Shoot");
        telemetry.addData("Stage", stage);
        telemetry.addData("Status", status);
        telemetry.addData("Elapsed", String.format("%.1f s", now - matchStartSeconds));
        telemetry.addData("Pose", robot.follower.pose());
        telemetry.addData("Mechanism", robot.mechanism.state());
        telemetry.addData("Target Cell", targetCell == null ? "none" : targetCell.toString());
        telemetry.addData("Cell camera", robot.cellVision.isAvailable() ? "available" : "unavailable");
        telemetry.addLine("Intake and scoring are timed; this robot has no mechanism sensors.");
        telemetry.update();
    }

    private void followToCellApproach(double now) {
        CellTarget target = driveCell.computeTarget(robot.follower.pose());
        robot.follower.follow(robot.pathTo(target.approachPose));
        stageStartSeconds = now;
        status = "Driving to Cell approach";
    }

    private boolean hasFreshCellSighting(Cell cell) {
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
        robot.follower.stop();
        robot.mechanism.stopAll();
        stage = Stage.ABORTED;
        status = reason;
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
