package org.firstinspires.ftc.teamcode.OpModes;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.NullLocalizer;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * The absolute minimum useful TeleOp: joystick -> mecanum wheels, through
 * PedroPathing's real drivetrain code (the same Mecanum/DrivePowers
 * classes the full robot uses), and nothing else.
 *
 * Use this one instead of the full {@code TeleOp} OpMode when:
 *   - odometry pods aren't wired up / configured yet (the full Robot class
 *     requires a working PinpointLocalizer to even construct - see
 *     pedro.Constants.create()),
 *   - PedroPathing's Foresight hasn't been tuned yet (also required by
 *     Constants.create() - it throws on purpose rather than guessing),
 *   - intake/transfer/outtake motors aren't installed/wired yet, or
 *   - a camera isn't mounted yet.
 *
 * This still goes through PedroPathing (per the ask: "only uses
 * PedroPathing") rather than hand-rolling separate mecanum joystick math,
 * so driving behaves identically to the full robot once the rest of the
 * hardware comes online - there is exactly one place mecanum power
 * mixing happens in this whole project, not two implementations that can
 * quietly drift apart.
 *
 * WHAT THIS CANNOT DO: no field-centric driving (that needs a real
 * heading source - NullLocalizer doesn't have one), no autonomous, no
 * aiming, no telemetry about robot position. It drives the chassis and
 * nothing more. Move up to the full TeleOp OpMode once odometry,
 * Foresight tuning, and the mechanisms are ready.
 */
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
