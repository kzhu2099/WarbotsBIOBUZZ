package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.HardwareConfig;

/**
 * AUDITED: this project's PedroPathing 3.0 integration (Follower,
 * MecanumConfig, PinpointConfig, Foresight/ForesightConfig) was checked
 * against the real, current PedroPathing source and found to already be
 * correct - it needed no API-level rewrite. The one real fix here is
 * wiring, per project brief section 27: drivetrain motor names were
 * hardcoded string literals in this file; they now come from
 * HardwareConfig, the single place hardware names live, so a rename at a
 * meet only has to happen once.
 */
public class Constants {

    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set(HardwareConfig.FRONT_LEFT_DRIVE);
        c.backLeftName.set(HardwareConfig.BACK_LEFT_DRIVE);
        c.frontRightName.set(HardwareConfig.FRONT_RIGHT_DRIVE);
        c.backRightName.set(HardwareConfig.BACK_RIGHT_DRIVE);
        // TODO: VERIFY these directions against the real drivetrain once
        // assembled - wrong direction here shows up as the robot
        // spinning/strafing backward relative to commanded input.
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.manualBrakeMode.set(true);
    });

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set(HardwareConfig.ODOMETRY_COMPUTER);
        // TODO: VERIFY pod offsets (inches from robot's tracking center)
        // and directions against the real odometry pod mounting.
        c.yPodOffset.set(3.75);
        c.xPodOffset.set(0.2);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
    });

    /**
     * Intentionally left null. Foresight's tuned constants come from
     * PedroPathing's real tuning workflow, not from guessing:
     *   1. Add a `@Tuner` method to a Tuning.java class that returns a
     *      ForesightTuner built from this project's real localizerConfig
     *      and drivetrainConfig (see pedropathing.com/docs/pathing/tuning/foresight).
     *   2. Deploy, then open http://192.168.43.1:10158 (the AutoTune web
     *      dashboard) while connected to the Robot Controller, and step
     *      through the velocity/deceleration/braking/kP identification
     *      routines it walks you through.
     *   3. Copy the ForesightConfig it generates (its "Java" tab) in here.
     * create() below fails loudly instead of quietly using made-up
     * numbers, because these values are physical properties of this
     * specific robot's mass/wheels/battery, not something to hand-derive.
     */
    public static ForesightConfig foresightConfig = null;

    public static Follower create(HardwareMap hardwareMap) {
        if (foresightConfig == null) {
            throw new IllegalStateException("Constants.foresightConfig is not set. Run ForesightTuner and paste its output in.");
        }

        Mecanum drivetrain = new Mecanum(hardwareMap, drivetrainConfig);
        PinpointLocalizer localizer = new PinpointLocalizer(hardwareMap, localizerConfig);
        return new Follower(localizer, drivetrain, new Foresight(foresightConfig));
    }
}
