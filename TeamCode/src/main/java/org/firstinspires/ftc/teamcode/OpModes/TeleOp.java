package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.ManualDriveAndShoot;
import org.firstinspires.ftc.teamcode.Robot;

/**
 * Main TeleOp. Uses the full robot when Pinpoint and Foresight are ready.
 * If full initialization fails, it falls back to manual robot-centric drive
 * and the timed scoring mechanism, without requiring cameras or odometry.
 */
@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "TeleOp")
public class TeleOp extends OpMode {

    private Robot robot;
    private ManualDriveAndShoot fallback;
    private String initWarning;

    @Override
    public void init() {
        try {
            robot = Robot.resumeFromAuto(hardwareMap, telemetry, gamepad1, gamepad2);
        } catch (RuntimeException e) {
            initWarning = readableMessage(e);
            fallback = new ManualDriveAndShoot(hardwareMap, telemetry);
            fallback.setModeNotice("Full TeleOp unavailable: " + initWarning
                    + ". Using robot-centric drive and manual scoring controls.");
            telemetry.addLine("WARNING: reduced TeleOp started.");
            fallback.addTelemetry();
            telemetry.update();
        }
    }

    @Override
    public void init_loop() {
        if (robot != null) {
            robot.toggleAlliance();
            robot.updateTelemetry();
        } else if (fallback != null) {
            fallback.addTelemetry();
            telemetry.update();
        }
    }

    @Override
    public void start() {
        if (robot != null) {
            robot.updateWorldModel(getRuntime());
        }
    }

    @Override
    public void loop() {
        double now = getRuntime();

        if (robot != null) {
            robot.updateWorldModel(now);
            robot.teleOpDrive();
            handleMechanism(now);
            robot.updateTelemetry(now);
        } else if (fallback != null) {
            fallback.update(now, gamepad1, gamepad2);
        }
    }

    private void handleMechanism(double now) {
        boolean overrideHeld = gamepad2.back;
        robot.mechanism.setManualOverride(overrideHeld, now);

        if (overrideHeld) {
            robot.mechanism.manualIntake(-gamepad2.left_stick_y);
            robot.mechanism.manualTransfer(-gamepad2.right_stick_y);
            robot.mechanism.manualOuttake(gamepad2.right_trigger);
            return;
        }

        if (gamepad2.left_trigger > 0.1
                && robot.mechanism.state() == org.firstinspires.ftc.teamcode.ScoringState.INTAKE_OFF) {
            robot.mechanism.startIntake(now);
        } else if (gamepad2.left_trigger <= 0.1
                && robot.mechanism.state() == org.firstinspires.ftc.teamcode.ScoringState.INTAKING) {
            robot.mechanism.stopIntake(now);
        }

        if (gamepad2.right_trigger > 0.5) {
            robot.mechanism.triggerOuttake(now);
        }

        if (gamepad2.aWasPressed()
                && robot.mechanism.state() == org.firstinspires.ftc.teamcode.ScoringState.FAULT) {
            robot.mechanism.clearFault(now);
        }
    }

    @Override
    public void stop() {
        if (robot != null) {
            robot.stop();
        }
        if (fallback != null) {
            fallback.stop();
        }
    }

    private static String readableMessage(RuntimeException e) {
        String message = e.getMessage();
        return message == null || message.trim().isEmpty()
                ? e.getClass().getSimpleName() : message;
    }
}
