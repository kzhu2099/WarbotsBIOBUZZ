package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/**
 * A computed scoring target for a specific {@link Hive}: where to stop and
 * approach from, where to actually be when scoring, and which way to face.
 * Produced fresh by {@link Hive#computeTarget()} every time it's needed,
 * rather than ever being hardcoded - see project brief section 22/23.
 */
public class HiveTarget {

    public final Hive hive;
    public final Pose approachPose;
    public final Pose scoringPose;
    public final double aimHeadingRadians;

    public HiveTarget(Hive hive, Pose approachPose, Pose scoringPose, double aimHeadingRadians) {
        this.hive = hive;
        this.approachPose = approachPose;
        this.scoringPose = scoringPose;
        this.aimHeadingRadians = aimHeadingRadians;
    }
}
