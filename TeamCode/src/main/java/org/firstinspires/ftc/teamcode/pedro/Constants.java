package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
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
    public static final DcMotorSimple.Direction FRONT_RIGHT_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction BACK_LEFT_DIRECTION = DcMotorSimple.Direction.REVERSE;
    public static final DcMotorSimple.Direction BACK_RIGHT_DIRECTION = DcMotorSimple.Direction.FORWARD;

    public static final boolean DRIVETRAIN_CONFIG_TUNED = true;

    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("fl");
        c.frontRightName.set("fr");
        c.backLeftName.set("bl");
        c.backRightName.set("br");
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.manualBrakeMode.set(true);
    });

    // Replace this placeholder with the complete Pinpoint Tuner output.
    public static final boolean LOCALIZER_CONFIG_TUNED = true;

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("odom");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(0.8625159676619402);
        c.yPodOffset.set(2.470802547424797);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    // Paste the Foresight configuration generated on this robot.
    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.20530900360201954);
                Controller secondaryTranslationalForward = Controller.proportional(0.07585619087333363);
                Controller primaryTranslationalLateral = Controller.proportional(0.2390594842554431);
                Controller secondaryTranslationalLateral = Controller.proportional(0.08832609164532136);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.018670969642721455));
                c.brake.set(Controller.proportionalFeedforward(0.015870324196313235));

                c.headingFeedback.set(Controller.proportional(2.6860821429838655));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.05244148209866923, 0.00587020620739328));

                c.linearBrakeCoefficients.set(Matrix.diag(0.02114451802520909, 0.06696583386308999));
                c.quadraticBrakeCoefficients.set(Matrix.diag(-2.7694204985449365E-4, 0.0018771073670350075));

                c.maxAchievableForwardVelocity.set(55.19807800374277);
                c.maxAchievableStrafeVelocity.set(47.030772534236355);
                c.naturalForwardDeceleration.set(40.177506031152916);
                c.naturalStrafeDeceleration.set(54.95234318244346);
            }
    );

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
