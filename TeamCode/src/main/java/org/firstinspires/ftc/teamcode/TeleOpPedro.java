package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedro.Constants;

@TeleOp(name = "TeleOp - Pedro + Pinpoint", group = "BIOBUZZ Tests")
public class TeleOpPedro extends OpMode {
    private RobotHardware driveHardware;
    private RobotHardware.MechanismController mechanisms;
    private Follower follower;
    private String driveError;
    private String mechanismError;

    @Override
    public void init() {
        driveHardware = new RobotHardware(hardwareMap);
        mechanisms = new RobotHardware.MechanismController(hardwareMap, true);

        if (!driveHardware.driveReady()) {
            driveError = "Missing drive motors: " + driveHardware.missingDriveMotors();
            return;
        }

        try {
            follower = Constants.create(hardwareMap);
        } catch (RuntimeException exception) {
            driveError = errorText(exception);
            driveHardware.stopDrive();
        }
    }

    @Override
    public void init_loop() {
        telemetry.addData("Drive motors", driveHardware.driveReady()
                ? "ready" : driveHardware.missingDriveMotors());
        telemetry.addData("Pedro + Pinpoint", follower == null
                ? "NOT READY: " + driveError : "ready");
        telemetry.addData("Mechanism motors", mechanisms.missingMotors().isEmpty()
                ? "all found/configured" : mechanisms.missingMotors());
        telemetry.addLine("Check Pinpoint pod offsets and directions before driving.");
        telemetry.update();
    }

    @Override
    public void loop() {
        if (mechanismError == null) {
            try {
                mechanisms.update(getRuntime(), gamepad2);
            } catch (RuntimeException exception) {
                mechanismError = errorText(exception);
                mechanisms.stop();
            }
        }

        if (follower != null && driveError == null) {
            try {
                follower.manual(
                        -RobotConfig.driveAxis(gamepad1.left_stick_y),
                        RobotConfig.driveAxis(gamepad1.left_stick_x),
                        RobotConfig.driveAxis(gamepad1.right_stick_x)
                );
                follower.update();
                telemetry.addData("Pedro + Pinpoint", "manual drive");
                telemetry.addData("Pose", follower.pose());
            } catch (RuntimeException exception) {
                driveError = errorText(exception);
                stopPedroDrive();
            }
        } else {
            driveHardware.stopDrive();
            telemetry.addData("DRIVE STOPPED", driveError);
            telemetry.addLine("Use TeleOp - Manual (No Pedro) to test the drive motors.");
        }

        telemetry.addData("Mechanisms", mechanisms.action());
        if (!mechanisms.missingMotors().isEmpty()) {
            telemetry.addData("Missing mechanism motors", mechanisms.missingMotors());
        }
        if (mechanismError != null) telemetry.addData("MECHANISM ERROR", mechanismError);
        telemetry.update();
    }

    @Override
    public void stop() {
        stopPedroDrive();
        if (mechanisms != null) mechanisms.stop();
    }

    private void stopPedroDrive() {
        if (driveHardware != null) driveHardware.stopDrive();
        if (follower != null) {
            try {
                follower.stop();
            } catch (RuntimeException ignored) {
                // Direct motor stop remains the fallback.
            }
        }
        if (driveHardware != null) driveHardware.stopDrive();
    }

    private static String errorText(RuntimeException exception) {
        return exception.getMessage() == null
                ? exception.getClass().getSimpleName() : exception.getMessage();
    }
}
