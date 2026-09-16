package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "TeleOp")
public class TeleOpTemplate extends OpMode {

    private Robot robot;

    @Override
    public void init() {
        robot = new Robot(hardwareMap, telemetry, gamepad1, gamepad2, false);
    }

    @Override
    public void init_loop() {
        if (gamepad1.xWasPressed()) {
            robot.redAlliance = !robot.redAlliance;
        }

        telemetry.addData("alliance (x to toggle)", robot.redAlliance ? "RED" : "BLUE");
        telemetry.update();
    }

    @Override
    public void start() {
        robot.start();
    }

    @Override
    public void loop() {
        robot.teleOpDrive();
        robot.updateTelemetry();
    }

    @Override
    public void stop() {
        robot.stop();
    }
}