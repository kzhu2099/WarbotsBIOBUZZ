package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/** The approach pose and scoring pose calculated for a Cell. */
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
