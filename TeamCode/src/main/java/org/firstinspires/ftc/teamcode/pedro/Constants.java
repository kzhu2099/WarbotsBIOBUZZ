package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/** Pedro settings shared by manual drive and the path test. */
public final class Constants {
    private Constants() {}

    // These names must match the Driver Station configuration.
    public static final String FRONT_LEFT_NAME = "fl";
    public static final String FRONT_RIGHT_NAME = "fr";
    public static final String BACK_LEFT_NAME = "bl";
    public static final String BACK_RIGHT_NAME = "br";

    // Check these starting directions with Mecanum Tuner before driving.
    public static final DcMotorSimple.Direction FRONT_LEFT_DIRECTION = DcMotorSimple.Direction.REVERSE;
    public static final DcMotorSimple.Direction FRONT_RIGHT_DIRECTION = DcMotorSimple.Direction.REVERSE;
    public static final DcMotorSimple.Direction BACK_LEFT_DIRECTION = DcMotorSimple.Direction.REVERSE;
    public static final DcMotorSimple.Direction BACK_RIGHT_DIRECTION = DcMotorSimple.Direction.FORWARD;

    public static final boolean DRIVETRAIN_CONFIG_TUNED = false;

    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set(FRONT_LEFT_NAME);
        c.frontRightName.set(FRONT_RIGHT_NAME);
        c.backLeftName.set(BACK_LEFT_NAME);
        c.backRightName.set(BACK_RIGHT_NAME);
        c.frontLeftDirection.set(FRONT_LEFT_DIRECTION);
        c.frontRightDirection.set(FRONT_RIGHT_DIRECTION);
        c.backLeftDirection.set(BACK_LEFT_DIRECTION);
        c.backRightDirection.set(BACK_RIGHT_DIRECTION);
        c.manualBrakeMode.set(true);
    });

    // Replace this placeholder with the complete Pinpoint Tuner output.
    public static final boolean LOCALIZER_CONFIG_TUNED = false;

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("odom");
        c.xPodOffset.set(0.20);
        c.yPodOffset.set(3.75);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    // Paste the Foresight configuration generated on this robot.
    public static ForesightConfig foresightConfig = null;

    /** Build a Follower once the three Pedro settings are ready. */
    public static Follower create(HardwareMap hardwareMap) {
        if (!DRIVETRAIN_CONFIG_TUNED) {
            throw new IllegalStateException(
                    "Run Mecanum Tuner, paste its drivetrainConfig into pedro/Constants.java, and set DRIVETRAIN_CONFIG_TUNED = true."
            );
        }
        if (!LOCALIZER_CONFIG_TUNED) {
            throw new IllegalStateException(
                    "Run Pinpoint Tuner, paste its complete localizerConfig into pedro/Constants.java, and set LOCALIZER_CONFIG_TUNED = true."
            );
        }
        if (foresightConfig == null) {
            throw new IllegalStateException(
                    "Run Foresight Tuner and paste this robot's ForesightConfig into pedro/Constants.java."
            );
        }

        return new Follower(
                new PinpointLocalizer(hardwareMap, localizerConfig),
                new Mecanum(hardwareMap, drivetrainConfig),
                new Foresight(foresightConfig)
        );
    }
}
