package org.firstinspires.ftc.teamcode;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/** Pedro manual-drive test. Path following is intentionally not enabled here. */
@TeleOp(name = "TeleOp - Pedro + Pinpoint", group = "BIOBUZZ Tests")
public class TeleOpPedro extends OpMode {
    private Follower follower;
    private RobotHardware.Mechanisms mechanisms;
    private AprilTagCamera camera;
    private String initError;
    private boolean fieldCentric;

    @Override
    public void init() {
        mechanisms = new RobotHardware.Mechanisms(hardwareMap);
        camera = new AprilTagCamera(hardwareMap);

        try {
            follower = new Follower(
                    new PinpointLocalizer(hardwareMap, RobotConfig.pinpointConfig()),
                    new Mecanum(hardwareMap, RobotConfig.mecanumConfig()),
                    null
            );
        } catch (RuntimeException e) {
            initError = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        }
    }

    @Override
    public void loop() {
        if (follower == null) {
            telemetry.addLine("PEDRO NOT READY");
            telemetry.addData("Reason", initError);
            telemetry.addLine("Use TeleOp - Manual until the Pinpoint is configured.");
            telemetry.update();
            return;
        }

        if (gamepad1.bWasPressed()) fieldCentric = !fieldCentric;

        double scale = gamepad1.right_trigger > 0.1
                ? RobotConfig.DRIVE_PRECISION_SCALE : 1.0;
        DrivePowers powers = new DrivePowers(
                -gamepad1.left_stick_y * scale,
                gamepad1.left_stick_x * scale,
                gamepad1.right_stick_x * scale
        );

        if (fieldCentric) {
            powers = ManualDrive.fieldCentric(powers, follower.pose().heading());
        }

        follower.manual(powers);
        follower.update();

        mechanisms.update(getRuntime(), gamepad2);
        telemetry.addData("Drive", fieldCentric ? "Pedro / field-centric" : "Pedro / robot-centric");
        telemetry.addData("Pose (robot center)", follower.pose());
        telemetry.addData("Mechanism", mechanisms.state());
        String missing = mechanisms.missingMotors();
        telemetry.addData(missing.isEmpty() ? "Mechanism motors" : "MECHANISM WARNING",
                missing.isEmpty() ? "all found" : "missing: " + missing);
        camera.addTelemetry(telemetry);
        telemetry.update();
    }

    @Override
    public void stop() {
        if (follower != null) follower.stop();
        if (mechanisms != null) mechanisms.stop();
        if (camera != null) camera.close();
    }
}
