package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "TeleOp - Drive Only", group = "BIOBUZZ Tests")
public class TeleOpDriveOnly extends OpMode {
    private RobotHardware robot;

    @Override
    public void init() {
        robot = new RobotHardware(hardwareMap);
    }

    @Override
    public void loop() {
        robot.drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
        telemetry.addData("Drive", robot.driveReady() ? "ready" : "MISSING MOTORS");
        if (!robot.driveReady()) telemetry.addData("Missing", robot.missingDriveMotors());
        telemetry.update();
    }

    @Override
    public void stop() {
        if (robot != null) robot.stopDrive();
    }
}
