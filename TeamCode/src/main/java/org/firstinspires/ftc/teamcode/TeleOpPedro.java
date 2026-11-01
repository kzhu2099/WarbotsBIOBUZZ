package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "TeleOp - Pedro + Pinpoint", group = "BIOBUZZ Tests")
public class TeleOpPedro extends OpMode {
    private RobotHardware driveHardware;
    private RobotHardware.Mechanisms mechanisms;
    private Follower follower;
    private String driveError;
    private String mechanismError;

    @Override
    public void init() {
        driveHardware = new RobotHardware(hardwareMap);
        mechanisms = new RobotHardware.Mechanisms(hardwareMap, true);

        if (!driveHardware.driveReady()) {
            driveError = "Missing drive motors: " + driveHardware.missingDriveMotors();
            return;
        }

        try {
            follower = new Follower(
                    new PinpointLocalizer(hardwareMap, RobotConfig.pinpointConfig()),
                    new Mecanum(hardwareMap, RobotConfig.mecanumConfig()),
                    null // Manual driving only; no path or hold controller is needed.
            );
        } catch (RuntimeException e) {
            driveError = errorText(e);
            driveHardware.stopDrive();
        }
    }

    @Override
    public void loop() {
        if (mechanismError == null) {
            try {
                mechanisms.update(getRuntime(), gamepad2);
            } catch (RuntimeException e) {
                mechanismError = errorText(e);
                mechanisms.stop();
            }
        }

        if (follower != null && driveError == null) {
            try {
                follower.manual(-RobotConfig.driveAxis(gamepad1.left_stick_y), RobotConfig.driveAxis(gamepad1.left_stick_x), RobotConfig.driveAxis(gamepad1.right_stick_x));
                follower.update();
                telemetry.addData("Pedro", "ready");
                telemetry.addData("Pinpoint pose", follower.pose());
            } catch (RuntimeException e) {
                driveError = errorText(e);
                follower.stop();
                driveHardware.stopDrive();
            }
        } else {
            driveHardware.stopDrive();
            telemetry.addData("PEDRO STOPPED", driveError);
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
        if (follower != null) follower.stop();
        if (driveHardware != null) driveHardware.stopDrive();
        if (mechanisms != null) mechanisms.stop();
    }

    private static String errorText(RuntimeException error) {
        return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
    }
}
