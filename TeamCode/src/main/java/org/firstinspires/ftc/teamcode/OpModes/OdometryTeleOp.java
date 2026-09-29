package org.firstinspires.ftc.teamcode.OpModes;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * The middle bring-up stage, between {@link DriveOnlyTeleOp} and the full
 * {@code TeleOp}: real odometry, real field-centric driving, and real pose
 * telemetry - but still no Foresight tuning, no cameras, and no
 * intake/transfer/outtake mechanism required.
 *
 * Use this one once odometry (Pinpoint + pods) is physically wired up and
 * {@code pedro.Constants.localizerConfig} has real pod offsets/directions
 * in it, but BEFORE PedroPathing's AutoTune/Foresight tuning has been done.
 * It exists to let you verify odometry is actually working correctly - pose
 * increasing the right direction as you push the robot, heading matching
 * reality, field-centric driving pointing "forward" the way you expect -
 * before spending time on the full Foresight tuning pass, and before the
 * rest of the robot (mechanisms, cameras) needs to exist at all.
 *
 * Like DriveOnlyTeleOp, this only ever calls follower.manual(...), never
 * follow()/hold(), so passing a null Algorithm to Follower is safe - see
 * NullLocalizer's header (in the main package) for the full explanation of
 * why that's true. The difference here is the localizer is the REAL
 * PinpointLocalizer instead of a no-op stand-in, which is what unlocks
 * field-centric driving (it needs a real heading source) and honest pose
 * telemetry.
 *
 * Bring-up order recap:
 *   1. DriveOnlyTeleOp   - four drive motors only, robot-centric.
 *   2. OdometryTeleOp    - + real odometry, field-centric, real pose telemetry.
 *   3. Full TeleOp       - + Foresight tuned, + cameras + mechanisms wired up.
 */
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
