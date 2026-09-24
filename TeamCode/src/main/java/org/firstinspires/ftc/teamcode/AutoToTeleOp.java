package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/**
 * Carries state from the end of Autonomous into the start of TeleOp -
 * static fields survive an OpMode transition within the same Robot
 * Controller app session (they do NOT survive a full app restart/redeploy,
 * which is expected and fine).
 *
 * Extended beyond the original version to also carry Inventory: if
 * autonomous ends holding balls (e.g. it ran out of time before scoring
 * everything), TeleOp should start knowing that, not silently reset to
 * zero and let the driver over-collect past capacity without realizing it.
 */
public class AutoToTeleOp {

    private static Pose pose;
    private static Boolean redAlliance;
    private static Integer pollenCount;
    private static Integer nectarCount;

    public static void save(Pose endPose, boolean alliance, int pollen, int nectar) {
        pose = endPose;
        redAlliance = alliance;
        pollenCount = pollen;
        nectarCount = nectar;
    }

    public static boolean hasPose() {
        return pose != null;
    }

    public static Pose pose() {
        return pose;
    }

    public static boolean hasAlliance() {
        return redAlliance != null;
    }

    public static boolean redAlliance() {
        return redAlliance != null && redAlliance;
    }

    public static boolean hasInventory() {
        return pollenCount != null && nectarCount != null;
    }

    public static int pollenCount() {
        return pollenCount == null ? 0 : pollenCount;
    }

    public static int nectarCount() {
        return nectarCount == null ? 0 : nectarCount;
    }
}
