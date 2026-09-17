package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

@Autonomous(name = "Auto")
public class AutoTemplate extends OpMode {

    private Robot robot;
    private Sequence sequence;

    @Override
    public void init() {
        Points.set("start", Pose.zero());

        robot = new Robot(hardwareMap, telemetry, gamepad1, gamepad2, false);
        robot.setStartPoseOptions("start");
    }

    @Override
    public void init_loop() {
        robot.toggleAlliance();
        robot.cycleStartPose();
        robot.updateTelemetry();
    }

    @Override
    public void start() {
        robot.setStartPose(robot.selectedStartPose());
        robot.start();

        Pose startPose = Points.get("start");
        Points.set("score", new Pose(startPose.x() + 24, startPose.y(), startPose.heading()));
        Points.set("pickup", new Pose(startPose.x(), startPose.y() + 24, startPose.heading()));

        sequence = new Sequence(
                robot.stepTo("score"),
                robot.stepLaunchFor(true, 1.5),
                Step.branch(() -> false, robot.stepTo("pickup"), robot.stepTo("start")),
                robot.stepIntakeFor(true, 1)
        );
    }

    @Override
    public void loop() {
        robot.updateFollower();
        sequence.update();
        robot.updateTelemetry();
    }

    @Override
    public void stop() {
        robot.stop();
    }
}