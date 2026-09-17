package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.Robot;

@Disabled
@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "TeleOp")
public class MainTeleOp extends OpMode {

    private Robot robot;

    @Override
    public void init() {
        robot = new Robot(hardwareMap, telemetry, gamepad1, gamepad2, false);
    }

    @Override
    public void init_loop() {
        robot.toggleAlliance();
        robot.updateTelemetry();
    }

    @Override
    public void start() {
        robot.start();
    }

    @Override
    public void loop() {
        robot.teleOpDrive();
        robot.intake(gamepad2.left_trigger > 0.1);
        robot.launch(gamepad2.right_trigger > 0.1);
        robot.updateTelemetry();
    }

    @Override
    public void stop() {
        robot.stop();
    }
}