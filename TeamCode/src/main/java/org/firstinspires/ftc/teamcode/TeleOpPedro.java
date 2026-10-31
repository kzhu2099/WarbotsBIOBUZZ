package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "TeleOp - Pedro + Pinpoint", group = "BIOBUZZ Tests")
public class TeleOpPedro extends OpMode {
    private Follower follower;
    private RobotHardware.Mechanisms mechanisms;
    private String error;

    @Override
    public void init() {
        mechanisms = new RobotHardware.Mechanisms(hardwareMap);

        try {
            follower = new Follower(
                    new PinpointLocalizer(hardwareMap, RobotConfig.pinpointConfig()),
                    new Mecanum(hardwareMap, RobotConfig.mecanumConfig()),
                    null
            );
        } catch (RuntimeException e) {
            error = message(e);
        }
    }

    @Override
    public void loop() {
        mechanisms.update(getRuntime(), gamepad2);

        if (follower != null && error == null) {
            try {
                follower.manual(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
                follower.update();
                telemetry.addData("Pedro", "driving");
                telemetry.addData("Pinpoint pose", follower.pose());
            } catch (RuntimeException e) {
                error = message(e);
                follower.stop();
            }
        }

        if (error != null) {
            telemetry.addData("PEDRO ERROR", error);
            telemetry.addLine("Check motor names, Pinpoint name, offsets, and encoder directions.");
            telemetry.addLine("Use TeleOp - Manual (No Pedro) to test the drivetrain separately.");
        }

        telemetry.addData("Mechanisms", mechanisms.action());
        if (!mechanisms.missingMotors().isEmpty()) {
            telemetry.addData("Missing mechanism motors", mechanisms.missingMotors());
        }
        telemetry.update();
    }

    @Override
    public void stop() {
        if (follower != null) follower.stop();
        if (mechanisms != null) mechanisms.stop();
    }

    private static String message(RuntimeException error) {
        return error.getMessage() == null
                ? error.getClass().getSimpleName() : error.getMessage();
    }
}
