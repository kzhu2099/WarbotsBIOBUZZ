package org.firstinspires.ftc.teamcode.OpModes;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.BioBuzzAutoConfig;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.StartPosition;
import org.firstinspires.ftc.teamcode.pedro.Constants;

@Autonomous(name = "BIOBUZZ - Leave Wall", group = "BIOBUZZ")
public class LeaveOnlyAuto extends OpMode {

    private Robot robot;
    private String initError;
    private boolean pathStarted;
    private boolean finished;
    private double pathStartSeconds;

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
        telemetry.addLine("VERIFY THESE VALUES: loading-zone position and robot size.");
        telemetry.update();
    }

    @Override
    public void start() {
        if (robot == null) return;

        Pose start = StartPosition.LOADING_ZONE.pose(robot.redAlliance);
        robot.setStartPose(start);
        Pose leave = new Pose(
                start.x() + Math.cos(start.heading()) * BioBuzzAutoConfig.LEAVE_DISTANCE_INCHES,
                start.y() + Math.sin(start.heading()) * BioBuzzAutoConfig.LEAVE_DISTANCE_INCHES,
                start.heading()
        );
        robot.followTo(leave);
        pathStarted = true;
        pathStartSeconds = getRuntime();
    }

    @Override
    public void loop() {
        if (robot == null) {
            showInitError();
            return;
        }

        robot.updateWorldModel(getRuntime());
        if (pathStarted && !finished
                && (!robot.busy() || getRuntime() - pathStartSeconds > 4.0)) {
            robot.follower.stop();
            finished = true;
        }

        telemetry.addData("Auto", "Leave Wall");
        telemetry.addData("Status", finished ? "DONE" : "Driving clear of wall");
        telemetry.addData("Pose", robot.follower.pose());
        telemetry.update();
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
