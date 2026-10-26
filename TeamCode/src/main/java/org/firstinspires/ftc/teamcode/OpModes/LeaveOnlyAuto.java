package org.firstinspires.ftc.teamcode.OpModes;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.BioBuzzAutoConfig;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.StartPosition;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.List;

/** Short route for earning Leave, then attempting Auto Park. */
@Autonomous(name = "BIOBUZZ - Leave and Park", group = "BIOBUZZ")
public class LeaveOnlyAuto extends OpMode {

    private enum Stage { LEAVE_WALL, PARK_TO_LANE, PARK_TO_ZONE, DONE }

    private Robot robot;
    private String initError;
    private String status = "Init";
    private Stage stage = Stage.LEAVE_WALL;
    private StartPosition startPosition = StartPosition.SIDE_WALL;
    private List<Pose> parkingRoute;
    private int parkingWaypointIndex;
    private double stageStartedAt;
    private boolean replannedParkRoute;
    private double autoStartedAt;

    @Override
    public void init() {
        if (Constants.foresightConfig == null) {
            initError = "ForesightConfig is missing. Run the Pedro tuners first.";
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
        telemetry.addData("Loading Zone target", BioBuzzAutoConfig.loadingZonePose(robot.redAlliance));
        telemetry.addLine("D-pad left/right changes the legal perimeter start.");
        telemetry.addLine("Check robot dimensions and the start pose against the taped field.");
        telemetry.update();
    }

    @Override
    public void start() {
        if (robot == null) return;

        double now = getRuntime();
        autoStartedAt = now;
        robot.setStartPose(startPosition.pose(robot.redAlliance));
        robot.followTo(BioBuzzAutoConfig.leavePose(robot.follower.pose()));
        stage = Stage.LEAVE_WALL;
        stageStartedAt = now;
        status = "Leaving the perimeter wall";
    }

    @Override
    public void loop() {
        if (robot == null) {
            showInitError();
            return;
        }

        double now = getRuntime();
        robot.updateWorldModel(now);

        if (now - autoStartedAt >= BioBuzzAutoConfig.AUTO_SAFETY_CUTOFF_SECONDS
                && stage != Stage.DONE) {
            robot.follower.stop();
            robot.mechanism.setManualOverride(true, now);
            stage = Stage.DONE;
            status = "Auto time limit reached; stopped";
        }

        switch (stage) {
            case LEAVE_WALL:
                if (!robot.busy()) {
                    beginParking(now);
                } else if (now - stageStartedAt >= BioBuzzAutoConfig.LEAVE_TIMEOUT_SECONDS) {
                    robot.follower.stop();
                    beginParking(now);
                    status = "Leave path timed out; trying the park route from the current pose";
                }
                break;

            case PARK_TO_LANE:
                if (!robot.busy()) {
                    followNextParkingLeg(now);
                } else if (now - stageStartedAt >= BioBuzzAutoConfig.PARK_LANE_TIMEOUT_SECONDS) {
                    robot.follower.stop();
                    if (!replannedParkRoute) {
                        replannedParkRoute = true;
                        parkingRoute = BioBuzzAutoConfig.parkingLaneRoute(robot.redAlliance, robot.follower.pose());
                        parkingWaypointIndex = 0;
                        status = "Replanning the perimeter route";
                        followNextParkingLeg(now);
                    } else {
                        stage = Stage.DONE;
                        status = "Perimeter route timed out; stopped";
                    }
                }
                break;

            case PARK_TO_ZONE:
                if (!robot.busy()) {
                    robot.follower.stop();
                    stage = Stage.DONE;
                    status = "Park path ended";
                } else if (now - stageStartedAt >= BioBuzzAutoConfig.PARK_TIMEOUT_SECONDS) {
                    robot.follower.stop();
                    stage = Stage.DONE;
                    status = "Loading Zone approach timed out; stopped";
                }
                break;

            default:
                break;
        }

        telemetry.addData("Auto", "Leave and Park");
        telemetry.addData("Stage", stage);
        telemetry.addData("Status", status);
        telemetry.addData("Pose", robot.follower.pose());
        telemetry.addData("Loading Zone target", BioBuzzAutoConfig.loadingZonePose(robot.redAlliance));
        telemetry.update();
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

    // Build the route from the robot’s actual current pose, including after a timeout.
    private void beginParking(double now) {
        robot.follower.stop();
        parkingRoute = BioBuzzAutoConfig.parkingLaneRoute(robot.redAlliance, robot.follower.pose());
        parkingWaypointIndex = 0;
        replannedParkRoute = false;
        followNextParkingLeg(now);
    }

    // Do not skip a waypoint; each leg must finish before the next one starts.
    private void followNextParkingLeg(double now) {
        if (parkingWaypointIndex < parkingRoute.size()) {
            robot.followTo(parkingRoute.get(parkingWaypointIndex++));
            stage = Stage.PARK_TO_LANE;
            stageStartedAt = now;
            status = "Following the perimeter route";
        } else {
            robot.followTo(BioBuzzAutoConfig.loadingZonePose(robot.redAlliance));
            stage = Stage.PARK_TO_ZONE;
            stageStartedAt = now;
            status = "Entering the alliance Loading Zone";
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
