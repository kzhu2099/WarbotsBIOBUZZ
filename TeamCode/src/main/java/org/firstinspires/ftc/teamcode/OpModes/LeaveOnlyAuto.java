package org.firstinspires.ftc.teamcode.OpModes;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.BioBuzzAutoConfig;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.StartPosition;
import org.firstinspires.ftc.teamcode.pedro.Constants;

@Autonomous(name = "BIOBUZZ - Leave and Park", group = "BIOBUZZ")
public class LeaveOnlyAuto extends OpMode {

    private enum Stage { LEAVE_WALL, PARK, DONE }

    private Robot robot;
    private String initError;
    private String status = "INIT";
    private Stage stage = Stage.LEAVE_WALL;
    private double stageStartedAt;

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
        robot.setStartPose(StartPosition.SIDE_WALL.pose(robot.redAlliance));
        robot.updateWorldModel(getRuntime());

        telemetry.addData("Alliance", robot.redAlliance ? "RED" : "BLUE");
        telemetry.addData("Start", robot.follower.pose());
        telemetry.addData("Park target", BioBuzzAutoConfig.loadingZonePose(robot.redAlliance));
        telemetry.addLine("VERIFY THESE VALUES: wall start, robot dimensions and Loading Zone target.");
        telemetry.update();
    }

    @Override
    public void start() {
        if (robot == null) return;

        double now = getRuntime();
        robot.setStartPose(StartPosition.SIDE_WALL.pose(robot.redAlliance));
        robot.followTo(BioBuzzAutoConfig.leavePose(robot.follower.pose()));
        stage = Stage.LEAVE_WALL;
        stageStartedAt = now;
        status = "Moving clear of the perimeter";
    }

    @Override
    public void loop() {
        if (robot == null) {
            showInitError();
            return;
        }

        double now = getRuntime();
        robot.updateWorldModel(now);

        if (stage == Stage.LEAVE_WALL && (!robot.busy() || now - stageStartedAt >= 4.0)) {
            beginParking(now);
        } else if (stage == Stage.PARK
                && (!robot.busy() || now - stageStartedAt >= BioBuzzAutoConfig.PARK_TIMEOUT_SECONDS)) {
            robot.follower.stop();
            stage = Stage.DONE;
            status = robot.busy() ? "Park path timed out; stopped" : "Park path ended";
        }

        telemetry.addData("Auto", "Leave and Park");
        telemetry.addData("Stage", stage);
        telemetry.addData("Status", status);
        telemetry.addData("Pose", robot.follower.pose());
        telemetry.addData("Park target", BioBuzzAutoConfig.loadingZonePose(robot.redAlliance));
        telemetry.addLine("Check the final pose on the field before relying on the park points.");
        telemetry.update();
    }

    private void beginParking(double now) {
        robot.follower.stop();
        robot.followTo(BioBuzzAutoConfig.loadingZonePose(robot.redAlliance));
        stage = Stage.PARK;
        stageStartedAt = now;
        status = "Driving into the alliance Loading Zone";
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
