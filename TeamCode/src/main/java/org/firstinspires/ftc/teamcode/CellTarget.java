package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/**
 * A computed scoring target for a specific {@link Cell}: where to stop and
 * approach from, where to actually be when scoring, and which way to face.
 * Produced fresh by {@link Cell#computeTarget()} every time it's needed,
 * rather than ever being hardcoded - see project brief section 22/23.
 */
public class CellTarget {

    public final Cell cell;
    public final Pose approachPose;
    public final Pose scoringPose;
    public final double aimHeadingRadians;

    public CellTarget(Cell cell, Pose approachPose, Pose scoringPose, double aimHeadingRadians) {
        this.cell = cell;
        this.approachPose = approachPose;
        this.scoringPose = scoringPose;
        this.aimHeadingRadians = aimHeadingRadians;
    }
}
