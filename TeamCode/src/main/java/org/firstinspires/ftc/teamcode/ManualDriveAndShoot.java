package org.firstinspires.ftc.teamcode;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * The manual drive-and-shoot fallback. It only needs the drive motors and
 * whichever scoring motors are actually installed. It deliberately does
 * not initialize cameras, odometry, or Foresight.
 *
 * Driving is robot-centric; the driver aims by rotating the chassis. Intake
 * and transfer are timed, so the mechanism status is an estimate rather than
 * proof that a game piece reached the shooter.
 */
public class ManualDriveAndShoot {

    private final Telemetry telemetry;
    private final ScoringMechanism mechanism;
    private Follower follower;
    private String driveWarning;
    private String modeNotice;

    public ManualDriveAndShoot(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        mechanism = new ScoringMechanism(hardwareMap);

        try {
            Mecanum drivetrain = new Mecanum(hardwareMap, Constants.drivetrainConfig);
            follower = new Follower(new NullLocalizer(), drivetrain, null);
        } catch (RuntimeException e) {
            driveWarning = readableMessage(e);
        }
    }

    public void setModeNotice(String notice) {
        modeNotice = notice;
    }

    public void update(double nowSeconds, Gamepad gamepad1, Gamepad gamepad2) {
        mechanism.update(nowSeconds);

        if (follower != null) {
            double precision = 1 - gamepad1.right_trigger * 0.75;
            DrivePowers powers = new DrivePowers(
                    -gamepad1.left_stick_y * precision,
                    -gamepad1.left_stick_x * precision,
                    -gamepad1.right_stick_x * precision);
            follower.manual(powers);
            follower.update();
        }

        handleMechanism(nowSeconds, gamepad2);
        addTelemetry();
        telemetry.update();
    }

    private void handleMechanism(double nowSeconds, Gamepad gamepad2) {
        boolean overrideHeld = gamepad2.back;
        mechanism.setManualOverride(overrideHeld, nowSeconds);

        if (overrideHeld) {
            mechanism.manualIntake(-gamepad2.left_stick_y);
            mechanism.manualTransfer(-gamepad2.right_stick_y);
            mechanism.manualOuttake(gamepad2.right_trigger);
            return;
        }

        if (gamepad2.left_trigger > 0.1
                && mechanism.state() == ScoringState.INTAKE_OFF) {
            mechanism.startIntake(nowSeconds);
        } else if (gamepad2.left_trigger <= 0.1
                && mechanism.state() == ScoringState.INTAKING) {
            mechanism.stopIntake(nowSeconds);
        }

        if (gamepad2.right_trigger > 0.5) {
            mechanism.triggerOuttake(nowSeconds);
        }

        if (gamepad2.aWasPressed() && mechanism.state() == ScoringState.FAULT) {
            mechanism.clearFault(nowSeconds);
        }
    }

    public void addTelemetry() {
        telemetry.addData("Control mode", "manual drive + shoot; no camera or odometry");
        telemetry.addData("Drive", follower == null
                ? "UNAVAILABLE" : "ready (robot-centric)");
        if (driveWarning != null) {
            telemetry.addData("DRIVE WARNING", driveWarning);
        }
        telemetry.addData("Vision", "not used; aim by turning the robot");
        telemetry.addData("Mechanism", mechanism.state());

        String missing = mechanism.missingMotorNames();
        telemetry.addData(missing.isEmpty() ? "Mechanism motors" : "MECHANISM WARNING",
                missing.isEmpty() ? "all found" : "missing: " + missing);

        if (modeNotice != null && !modeNotice.isEmpty()) {
            telemetry.addData("TELEOP FALLBACK", modeNotice);
        }
    }

    public void stop() {
        if (follower != null) {
            follower.stop();
        }
        mechanism.stopAll();
    }

    private static String readableMessage(RuntimeException e) {
        String message = e.getMessage();
        return message == null || message.trim().isEmpty()
                ? e.getClass().getSimpleName() : message;
    }
}
