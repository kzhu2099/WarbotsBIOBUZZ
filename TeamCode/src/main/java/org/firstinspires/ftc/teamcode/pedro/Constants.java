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

public class Constants {

    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("fl");
        c.backLeftName.set("bl");
        c.frontRightName.set("fr");
        c.backRightName.set("br");
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.manualBrakeMode.set(true);
    });

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("odom");
        c.yPodOffset.set(3.75);
        c.xPodOffset.set(0.2);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
    });

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