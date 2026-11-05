package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "TeleOp - No Shooting", group = "BIOBUZZ Tests")
public class TeleOpNoShoot extends OpMode {
    private RobotHardware robot;
    private RobotHardware.MechanismController mechanisms;
    private String error;

    @Override
    public void init() {
        robot = new RobotHardware(hardwareMap);
        mechanisms = new RobotHardware.MechanismController(hardwareMap, false);
    }

    @Override
    public void init_loop() {
        telemetry.addData("Drive motors", robot.driveReady() ? "ready" : robot.missingDriveMotors());
        telemetry.addData("Intake/transfer", mechanisms.missingMotors().isEmpty()
                ? "ready" : mechanisms.missingMotors());
        telemetry.addData("Outtake zero", mechanisms.missingOuttakeMotors().isEmpty()
                ? "both found and held at zero" : "WARNING: " + mechanisms.missingOuttakeMotors());
        telemetry.update();
    }

    @Override
    public void loop() {
        if (error == null) {
            try {
                robot.drive(
                        -RobotConfig.driveAxis(gamepad1.left_stick_y),
                        RobotConfig.driveAxis(gamepad1.left_stick_x),
                        RobotConfig.driveAxis(gamepad1.right_stick_x)
                );
                mechanisms.update(getRuntime(), gamepad2);
            } catch (RuntimeException exception) {
                error = exception.getMessage() == null
                        ? exception.getClass().getSimpleName() : exception.getMessage();
                robot.stopDrive();
                mechanisms.stop();
            }
        } else {
            robot.stopDrive();
            mechanisms.stop();
        }

        telemetry.addData("Drive", robot.driveReady() ? "ready" : "MISSING MOTORS");
        if (!robot.driveReady()) telemetry.addData("Missing drive motors", robot.missingDriveMotors());
        telemetry.addData("Mechanisms", mechanisms.action());
        telemetry.addLine("Outtake motors are held at zero and cannot be activated in this OpMode.");
        if (!mechanisms.missingMotors().isEmpty()) {
            telemetry.addData("Missing mechanism motors", mechanisms.missingMotors());
        }
        if (!mechanisms.missingOuttakeMotors().isEmpty()) {
            telemetry.addData("OUTTAKE ZERO WARNING", mechanisms.missingOuttakeMotors());
        }
        if (error != null) telemetry.addData("HARDWARE ERROR - stopped", error);
        telemetry.update();
    }

    @Override
    public void stop() {
        if (robot != null) robot.stopDrive();
        if (mechanisms != null) mechanisms.stop();
    }
}
