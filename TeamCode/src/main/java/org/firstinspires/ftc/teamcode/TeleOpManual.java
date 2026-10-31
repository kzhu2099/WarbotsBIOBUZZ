package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "TeleOp - Manual (No Pedro)", group = "BIOBUZZ Tests")
public class TeleOpManual extends OpMode {
    private RobotHardware robot;
    private RobotHardware.Mechanisms mechanisms;

    @Override
    public void init() {
        robot = new RobotHardware(hardwareMap);
        mechanisms = new RobotHardware.Mechanisms(hardwareMap);
    }

    @Override
    public void loop() {
        robot.drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
        mechanisms.update(getRuntime(), gamepad2, true);
        showStatus();
    }

    private void showStatus() {
        telemetry.addData("Drive", robot.driveReady() ? "ready" : "MISSING MOTORS");
        if (!robot.driveReady()) telemetry.addData("Missing drive motors", robot.missingDriveMotors());
        telemetry.addData("Mechanisms", mechanisms.action());
        if (!mechanisms.missingMotors().isEmpty()) {
            telemetry.addData("Missing mechanism motors", mechanisms.missingMotors());
        }
        telemetry.update();
    }

    @Override
    public void stop() {
        if (robot != null) robot.stopDrive();
        if (mechanisms != null) mechanisms.stop();
    }
}
