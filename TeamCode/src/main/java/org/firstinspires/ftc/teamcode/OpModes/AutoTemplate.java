package org.firstinspires.ftc.teamcode.OpModes;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.Points;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.Sequence;
import org.firstinspires.ftc.teamcode.Step;

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
        Points.set("hive", new Pose(startPose.x() + 24, startPose.y(), startPose.heading()));
        Points.set("pickup", new Pose(startPose.x(), startPose.y() + 24, startPose.heading()));

        sequence = new Sequence(
                Step.named("to hive",
                        Step.parallel(robot.stepTo("hive"), robot.stepIntakeFor(true, 1))),
                Step.named("launch", robot.stepLaunchFor(true, 1.5)),
                Step.named("next leg",
                        Step.race(
                                Step.branch(() -> robot.vision.seesPollen(),
                                        robot.stepToFacing("pickup", "hive"), robot.stepTo("start")),
                                Step.pause(5))),
                Step.named("intake", robot.stepIntakeFor(true, 1))
        );
    }

    @Override
    public void loop() {
        robot.updateFollower();

        if (gamepad1.aWasPressed()) {
            sequence.skip();
        }

        sequence.update();
        telemetry.addData("step", sequence.currentStepName());
        robot.updateTelemetry();
    }

    @Override
    public void stop() {
        robot.stop();
    }
}