package org.firstinspires.ftc.teamcode.OpModes;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.BallType;
import org.firstinspires.ftc.teamcode.Points;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.RobotGeometry;
import org.firstinspires.ftc.teamcode.Sequence;
import org.firstinspires.ftc.teamcode.Step;

/**
 * A simple, FIXED (non-dynamic) one-cycle autonomous, built from Step/
 * Sequence - useful both as a safe, easy-to-predict backup routine and as
 * a teaching example for how Step/Sequence work, in contrast with the
 * real dynamic system in {@link AutoOpMode}/AutonomousController.
 *
 * AUDIT FIX: this file previously referenced robot.vision (field is now
 * gameVision) and robot.stepIntakeFor()/stepLaunchFor() (raw-motor-power
 * Step helpers that no longer exist - see project brief section 31, "do
 * not consider intake finished because intake() sets motor power"). It
 * did not compile. It now drives the real ScoringMechanism state machine
 * through Robot.stepIntake()/stepScore().
 *
 * This intentionally does NOT use the dynamic Hive/BallMap system - the
 * hive position here is a fixed guess registered as a Point, not computed
 * from a live AprilTag sighting. That's the actual, honest difference
 * between this simple template and AutoOpMode: same Step/Sequence
 * mechanics, but no perception-driven decision making.
 */
@Autonomous(name = "Auto (Simple Template)")
public class AutoTemplate extends OpMode {

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
        // Fixed, guessed positions - NOT computed from AprilTag sightings.
        // TODO: VERIFY/adjust against the real field.
        Points.set("hive", Points.offset(Points.FIELD_SIZE / 2, Points.FIELD_SIZE / 2, startPose.heading(), -RobotGeometry.HALF_LENGTH_INCHES));
        Points.set("pickup", Points.offset(0, Points.FIELD_SIZE / 2, Math.toRadians(180), -RobotGeometry.HALF_LENGTH_INCHES));

        sequence = new Sequence(
                Step.named("leave & collect one ball",
                        Step.parallel(robot.stepTo("pickup"), robot.stepIntake(BallType.POLLEN))),
                Step.named("return to hive", robot.stepTo("hive")),
                Step.named("aim", robot.stepAimAtOwnHive(0.75)),
                Step.named("score", robot.stepScore())
        );
    }

    @Override
    public void loop() {
        double now = getRuntime();
        robot.updateWorldModel(now);

        if (gamepad1.aWasPressed()) {
            sequence.skip();
        }

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
