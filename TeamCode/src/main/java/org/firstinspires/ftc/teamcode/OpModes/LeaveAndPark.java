package org.firstinspires.ftc.teamcode.OpModes;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.Points;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.RobotGeometry;
import org.firstinspires.ftc.teamcode.Sequence;
import org.firstinspires.ftc.teamcode.Step;

/**
 * The simplest possible useful autonomous: leave the starting tile and
 * park. No vision, no scoring - just guaranteed autonomous points and a
 * predictable parking spot, for when nothing else is trusted yet (or as a
 * safe emergency backup OpMode at an event). Updated to the new Robot API
 * (RobotGeometry instead of the old Robot.HALF_LENGTH constant,
 * updateWorldModel() instead of updateFollower()) but otherwise unchanged
 * in spirit from the original.
 */
@Autonomous(name = "Leave and Park")
public class LeaveAndPark extends OpMode {

    private Robot robot;
    private Sequence sequence;

    @Override
    public void init() {
        Points.set("start", Points.offset(Points.FIELD_SIZE / 2, 0, Math.toRadians(90), RobotGeometry.HALF_LENGTH_INCHES));

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
        robot.updateWorldModel(getRuntime());

        Pose startPose = Points.get("start");
        Points.set("park", Points.offset(startPose.x(), startPose.y(), startPose.heading(), 24));

        sequence = new Sequence(Step.named("park", robot.stepTo("park")));
    }

    @Override
    public void loop() {
        robot.updateWorldModel(getRuntime());
        sequence.update();
        telemetry.addData("step", sequence.currentStepName());
        robot.updateTelemetry();
    }

    @Override
    public void stop() {
        robot.saveForTeleOp();
        robot.stop();
    }
}
