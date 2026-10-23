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

/** PedroPathing configuration for the mecanum drivetrain, Pinpoint localizer, and Foresight controller. */
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
     * Fill this with the measured result from the Foresight tuner.
     *
     * Tune motor directions and the Pinpoint localizer first, then run
     * the Foresight tuner from pedro/Tuning.java at http://192.168.43.1:10158.
     * Copy the generated ForesightConfig here. Do not use guessed gains or
     * velocity values: they depend on this robot's drivetrain and mass.
     *
     * The autonomous modes refuse to start while this remains null.
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