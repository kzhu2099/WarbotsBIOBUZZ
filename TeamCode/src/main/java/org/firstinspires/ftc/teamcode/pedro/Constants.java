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
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.Settings;

/**
 * Pedro's tuning numbers. Every number below was produced by Pedro AutoTune
 * (open http://<Robot Controller IP>:10158 and run the tuners in Tuning.java,
 * in order: Mecanum, Pinpoint, Foresight, Tests). Paste the tuner output here.
 *
 * These numbers belong to ONE physical robot. They were measured on the
 * drive-only test robot. The full robot must be re-tuned before its paths
 * are trusted. Motor and Pinpoint names come from Settings.java.
 */
public final class Constants {
    private Constants() {}

    public static final MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set(Settings.FRONT_LEFT);
        c.frontRightName.set(Settings.FRONT_RIGHT);
        c.backLeftName.set(Settings.BACK_LEFT);
        c.backRightName.set(Settings.BACK_RIGHT);
        c.frontLeftDirection.set(Settings.FRONT_LEFT_DIR);
        c.frontRightDirection.set(Settings.FRONT_RIGHT_DIR);
        c.backLeftDirection.set(Settings.BACK_LEFT_DIR);
        c.backRightDirection.set(Settings.BACK_RIGHT_DIR);
        c.manualBrakeMode.set(true);
    });

    // Pinpoint Tuner output for this robot.
    public static final PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set(Settings.PINPOINT);
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(2.327253086360421);
        c.yPodOffset.set(2.576290340874139);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    // Foresight Tuner output for this robot.
    public static final ForesightConfig foresightConfig = new ForesightConfig(
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

    /** The Follower drives the robot along paths and keeps track of where it is. */
    public static Follower create(HardwareMap hardwareMap) {
        return new Follower(
                new PinpointLocalizer(hardwareMap, localizerConfig),
                new Mecanum(hardwareMap, drivetrainConfig),
                new Foresight(foresightConfig)
        );
    }
}
