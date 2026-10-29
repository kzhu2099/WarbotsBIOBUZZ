package org.firstinspires.ftc.teamcode.OpModes;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.NullLocalizer;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/** undefined */
@TeleOp(name = "Drive Only TeleOp")
public class DriveOnlyTeleOp extends OpMode {

    private Follower follower;

    @Override
    public void init() {
        Mecanum drivetrain = new Mecanum(hardwareMap, Constants.drivetrainConfig);
        // No real localizer (no odometry required) and no Algorithm (no
        // Foresight tuning required) - safe ONLY because this OpMode only
        // ever calls follower.manual(...), never follow()/hold(). See
        // NullLocalizer's header for exactly why that's safe.
        follower = new Follower(new NullLocalizer(), drivetrain, null);

        telemetry.addData("Status", "Drive-only ready (no odometry, no camera, no mechanisms required)");
        telemetry.update();
    }

    @Override
    public void loop() {
        double precision = 1 - gamepad1.right_trigger * 0.75;
        DrivePowers powers = new DrivePowers(
                -gamepad1.left_stick_y * precision,
                -gamepad1.left_stick_x * precision,
                -gamepad1.right_stick_x * precision);

        follower.manual(powers);
        follower.update();

        telemetry.addData("mode", "robot-centric (drive-only, no field-centric without odometry)");
        telemetry.addData("forward/strafe/turn", "%.2f / %.2f / %.2f",
                -gamepad1.left_stick_y * precision, -gamepad1.left_stick_x * precision, -gamepad1.right_stick_x * precision);
        telemetry.update();
    }

    @Override
    public void stop() {
        follower.stop();
    }
}
