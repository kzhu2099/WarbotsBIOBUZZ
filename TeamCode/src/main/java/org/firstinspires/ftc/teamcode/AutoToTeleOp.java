package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/** Stores the alliance, pose, and inventory when switching from autonomous to TeleOp. */
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
