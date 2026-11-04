package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointLocalizer;

import org.firstinspires.ftc.teamcode.pedro.procedures.ForesightTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.MecanumTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.PinpointTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.Tests;

/** Adds the Pedro tuning procedures to AutoTune. */
public final class Tuning {
    private Tuning() {}

    @Tuner
    public static Procedure mecanumTuner() {
        return new MecanumTuner();
    }

    @Tuner
    public static Procedure pinpointTuner() {
        return new PinpointTuner();
    }

    @Tuner
    public static Procedure foresightTuner() {
        return new ForesightTuner(
                hardwareMap -> {
                    if (!Constants.LOCALIZER_CONFIG_TUNED) {
                        throw new IllegalStateException("Run Pinpoint Tuner and paste its result into Constants.java first.");
                    }
                    return new PinpointLocalizer(hardwareMap, Constants.localizerConfig);
                },
                hardwareMap -> {
                    if (!Constants.DRIVETRAIN_CONFIG_TUNED) {
                        throw new IllegalStateException("Run Mecanum Tuner and verify Constants.java first.");
                    }
                    return new Mecanum(hardwareMap, Constants.drivetrainConfig);
                }
        );
    }

    @Tuner
    public static Procedure tests() {
        return new Tests(
                Constants.DRIVETRAIN_CONFIG_TUNED
                        ? hardwareMap -> new Mecanum(hardwareMap, Constants.drivetrainConfig)
                        : null,
                Constants.LOCALIZER_CONFIG_TUNED
                        ? hardwareMap -> new PinpointLocalizer(hardwareMap, Constants.localizerConfig)
                        : null,
                Constants.DRIVETRAIN_CONFIG_TUNED
                        && Constants.LOCALIZER_CONFIG_TUNED
                        && Constants.foresightConfig != null
                        ? () -> new Foresight(Constants.foresightConfig)
                        : null
        );
    }
}
