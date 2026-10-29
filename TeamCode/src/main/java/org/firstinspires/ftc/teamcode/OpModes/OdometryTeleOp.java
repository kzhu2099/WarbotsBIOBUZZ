package org.firstinspires.ftc.teamcode.OpModes;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedro.Constants;

/** undefined */
@TeleOp(name = "Odometry TeleOp")
public class OdometryTeleOp extends OpMode {

    private Follower follower;
    private boolean fieldCentric = true;

    @Override
    public void init() {
        Mecanum drivetrain = new Mecanum(hardwareMap, Constants.drivetrainConfig);
        PinpointLocalizer localizer = new PinpointLocalizer(hardwareMap, Constants.localizerConfig);
        // Real localizer, real drivetrain, still no Algorithm - Foresight
        // tuning isn't needed for plain manual driving (see class header).
        follower = new Follower(localizer, drivetrain, null);

        telemetry.addData("Status", "Odometry TeleOp ready (no Foresight tuning required yet)");
        telemetry.update();
    }

    @Override
    public void loop() {
        if (gamepad1.bWasPressed()) {
            fieldCentric = !fieldCentric;
        }

        DrivePowers powers = new DrivePowers(
                -gamepad1.left_stick_y,
                -gamepad1.left_stick_x,
                -gamepad1.right_stick_x);

        if (fieldCentric) {
            // No alliance concept at this bring-up stage - "forward" is
            // just wherever the robot was facing when the localizer zeroed.
            powers = ManualDrive.fieldCentric(powers, follower.pose().heading(), 0);
        }

        follower.manual(powers);
        follower.update();

        telemetry.addData("mode", fieldCentric ? "field-centric (b to toggle)" : "robot-centric (b to toggle)");
        telemetry.addData("pose", follower.pose());
        telemetry.update();
    }

    @Override
    public void stop() {
        follower.stop();
    }
}
