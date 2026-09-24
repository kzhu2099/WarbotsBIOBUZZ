package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.ScoringState;

/**
 * The one real TeleOp OpMode. (AUDIT NOTE: the recovered project had TWO
 * OpModes both registered with @TeleOp(name = "TeleOp") - this one, and
 * OpModes/TeleOpTemplate.java, which does not compile against the new
 * Robot API and has been removed as a duplicate. Two OpModes with the same
 * registered name is itself a real bug, independent of terminology - the
 * driver station's OpMode list can only meaningfully show one "TeleOp.")
 *
 * Real subsystem control (project brief section 26), not raw motors:
 * driving (with field-centric toggle, hive auto-aim, pollen auto-track),
 * intake/transfer/outtake as one automatic mechanism the driver starts
 * and stops rather than three separate raw powers, and a manual override
 * for recovering from a jam without it being possible to get the robot
 * stuck in an autonomous-only state.
 */
@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "TeleOp")
public class TeleOp extends OpMode {

    private Robot robot;

    @Override
    public void init() {
        robot = Robot.resumeFromAuto(hardwareMap, telemetry, gamepad1, gamepad2);
    }

    @Override
    public void init_loop() {
        robot.toggleAlliance();
        robot.updateTelemetry();
    }

    @Override
    public void start() {
        robot.updateWorldModel(getRuntime());
    }

    @Override
    public void loop() {
        double now = getRuntime();

        // 1. PERCEIVE - same world-model update Autonomous uses, so
        // TeleOp's telemetry/aiming/inventory are exactly as trustworthy.
        robot.updateWorldModel(now);

        // 2 & 3. DECIDE + ACT - here, "deciding" is the driver's job;
        // this just turns their inputs into commands.
        robot.teleOpDrive();
        handleMechanism(now);

        robot.updateTelemetry(now);
    }

    private void handleMechanism(double now) {
        boolean overrideHeld = gamepad2.back;
        robot.mechanism.setManualOverride(overrideHeld, now);

        if (overrideHeld) {
            // Raw control for freeing a jam - intentionally bypasses the
            // state machine entirely rather than trying to force a state
            // transition that might not reflect physical reality.
            robot.mechanism.manualIntake(-gamepad2.left_stick_y);
            robot.mechanism.manualTransfer(-gamepad2.right_stick_y);
            robot.mechanism.manualOuttake(gamepad2.right_trigger);
            return;
        }

        if (gamepad2.left_trigger > 0.1 && robot.mechanism.state() == ScoringState.INTAKE_OFF) {
            robot.mechanism.startIntake(now);
        } else if (gamepad2.left_trigger <= 0.1 && robot.mechanism.state() == ScoringState.INTAKING) {
            robot.mechanism.stopIntake(now);
        }

        if (gamepad2.right_trigger > 0.5) {
            robot.mechanism.triggerOuttake(now);
        }

        if (gamepad2.aWasPressed() && robot.mechanism.state() == ScoringState.FAULT) {
            robot.mechanism.clearFault(now);
        }
    }

    @Override
    public void stop() {
        robot.stop();
    }
}
