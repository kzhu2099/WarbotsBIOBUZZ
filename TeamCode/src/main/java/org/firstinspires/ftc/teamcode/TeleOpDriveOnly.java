package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/** Four-motor test. Mechanisms and camera are not initialized. */
@TeleOp(name = "TeleOp - Drive Only", group = "BIOBUZZ Tests")
public class TeleOpDriveOnly extends OpMode {
    private RobotHardware robot;

    @Override
    public void init() {
        robot = new RobotHardware(hardwareMap, false);
    }

    @Override
    public void loop() {
        double scale = gamepad1.right_trigger > 0.1
                ? RobotConfig.DRIVE_PRECISION_SCALE : 1.0;
        robot.drive(
                -gamepad1.left_stick_y,
                gamepad1.left_stick_x,
                gamepad1.right_stick_x,
                scale
        );

        telemetry.addData("Mode", "manual drive only");
        telemetry.addData("Drive", robot.driveReady() ? "ready" : "missing motors");
        String missing = robot.missingDriveMotors();
        if (!missing.isEmpty()) telemetry.addData("DRIVE WARNING", missing);
        telemetry.update();
    }

    @Override
    public void stop() {
        if (robot != null) robot.stopDrive();
    }
}
