package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;
import com.pedropathing.utils.Angle;

/**
 * A real model of one of the field's four CELLS - "a three-dimensional
 * structure that can hold NECTAR and POLLEN" per the official BIOBUZZ
 * glossary, and the thing this project actually scores into and reads
 * AprilTags off of. This replaces the project's earlier "Hive" model,
 * which used the wrong term for this concept - see FieldTagLibrary's
 * Javadoc for the glossary definitions and why the rename happened.
 *
 * NOT YET MODELED - THE REAL HIVE/TIPPING MECHANIC: per the glossary, a
 * HIVE is a different, more specific thing than a Cell: "a bi-stable
 * structure made up of two CELLS and a connecting assembly that rotates on
 * a pivot," and TIP/TIPPED is a real scoring criterion ("the HIVE moves
 * from one stable state to the other stable state with the
 * downwards-facing CELL becoming the upwards-facing CELL"), feeding
 * POLLINATOR ranking points. This project currently treats each of the
 * four real Cells as an independent scoring target and does not model
 * which two Cells are paired into one physical Hive, which one is
 * currently up/accessible vs. down, or a robot action to cause a tip. If
 * tipping strategy becomes relevant, that would be a new small
 * Hive-pairing concept (e.g. "Cell X and Cell Y share a pivot; exactly one
 * is currently scorable") layered on top of this class, not a rewrite of
 * it - Cell's job (identity, tag, field pose, computing a scoring target)
 * stays the same either way.
 *
 * There are four of these on the real field, not two: each alliance has
 * one on the audience side and one on the side opposite the audience (see
 * FieldTagLibrary for the confirmed tag IDs). A Cell doesn't carry a
 * static "active" flag for that reason - with two valid same-alliance
 * targets, "which one is active" is a live decision based on where the
 * robot currently is, not a fixed choice made once at init. See
 * CellMap.bestOurCell(pose) and AutonomousPlanner.resolveScoringCell().
 *
 * fieldPose's heading is defined as the cell's USABLE SCORING DIRECTION:
 * the outward direction a ball travels when scored (i.e. the direction
 * pointing away from the cell, out into the field, through its opening).
 * A robot scores by standing out along that direction and facing back the
 * other way - see computeTarget().
 */
public class Cell {

    /** Below this, a sighting is treated as fresh enough to trust fully. */
    private static final double DETECTED_WINDOW_SECONDS = 0.5;
    /** Below this (but above DETECTED), still trusted for approach/aim. */
    private static final double CONFIRMED_WINDOW_SECONDS = 5.0;
    /** How much a fresh sighting moves the recorded position vs. keeping
     *  the prior estimate - smooths out single-frame noise. */
    private static final double POSITION_BLEND_WEIGHT = 0.6;

    public final Alliance alliance;
    public final FieldSide fieldSide;

    /** One representative tag ID for logging/telemetry - this structure
     *  actually carries four (see FieldTagLibrary.tagIdsFor); any of the
     *  four updates this same Cell object. */
    public final int primaryAprilTagId;

    private Pose fieldPose;
    private CellState state = CellState.UNKNOWN;
    private double confidence = 0.0;
    private double lastDetectedAtSeconds = Double.NEGATIVE_INFINITY;

    /** How far out from the cell's opening the robot should stop to line
     *  up before committing to the final scoring position. */
    private final double approachStandoffInches;
    /** How far out the robot should actually be when it fires/drops game
     *  elements into the cell. May equal approachStandoffInches if there's
     *  no separate lineup step for this mechanism. */
    private final double scoringStandoffInches;

    public Cell(Alliance alliance, FieldSide fieldSide, int primaryAprilTagId, Pose nominalFieldPose,
                double approachStandoffInches, double scoringStandoffInches) {
        this.alliance = alliance;
        this.fieldSide = fieldSide;
        this.primaryAprilTagId = primaryAprilTagId;
        this.fieldPose = nominalFieldPose;
        this.approachStandoffInches = approachStandoffInches;
        this.scoringStandoffInches = scoringStandoffInches;
    }

    /** Called by CellMap when any of this cell's four AprilTags is seen
     *  this cycle. */
    void updateFromSighting(Pose observedFieldPose, double observedConfidence, double nowSeconds) {
        double blendedX = fieldPose.x() + POSITION_BLEND_WEIGHT * (observedFieldPose.x() - fieldPose.x());
        double blendedY = fieldPose.y() + POSITION_BLEND_WEIGHT * (observedFieldPose.y() - fieldPose.y());
        // Heading (a mounted tag's orientation) isn't blended - a single
        // clean reading is trustworthy, and circular-mean blending isn't
        // worth the complexity here.
        this.fieldPose = new Pose(blendedX, blendedY, observedFieldPose.heading());
        this.confidence = observedConfidence;
        this.lastDetectedAtSeconds = nowSeconds;
        this.state = CellState.DETECTED;
    }

    /** Called every cycle (whether seen or not) to age the DETECTED state
     *  toward CONFIRMED/STALE as time passes since the last real sighting. */
    void tick(double nowSeconds) {
        if (state == CellState.UNKNOWN) return;

        double age = nowSeconds - lastDetectedAtSeconds;
        if (age <= DETECTED_WINDOW_SECONDS) {
            state = CellState.DETECTED;
        } else if (age <= CONFIRMED_WINDOW_SECONDS) {
            state = CellState.CONFIRMED;
        } else {
            state = CellState.STALE;
        }
    }

    public Pose fieldPose() {
        return fieldPose;
    }

    public CellState state() {
        return state;
    }

    public double confidence() {
        return confidence;
    }

    /** True once we have ever actually seen one of this cell's tags this match. */
    public boolean hasEverBeenDetected() {
        return state != CellState.UNKNOWN;
    }

    /**
     * Computes where to drive and which way to face to score here, right
     * now, using the current field pose estimate. Recomputed on demand -
     * never cached/hardcoded - so it stays correct as the estimate
     * improves or the confidence/staleness situation changes.
     *
     * approachStandoffInches/scoringStandoffInches (how far out from the
     * cell's opening these two poses sit) come from the mechanism's real
     * behavior, not a guessable geometric fact - see FieldTagLibrary/
     * CellMap for the concrete tuning procedure.
     */
    public CellTarget computeTarget() {
        double approachX = fieldPose.x() + Math.cos(fieldPose.heading()) * approachStandoffInches;
        double approachY = fieldPose.y() + Math.sin(fieldPose.heading()) * approachStandoffInches;
        double aimHeading = Angle.normalizeSigned(fieldPose.heading() + Math.PI);
        Pose approachPose = new Pose(approachX, approachY, aimHeading);

        double scoreX = fieldPose.x() + Math.cos(fieldPose.heading()) * scoringStandoffInches;
        double scoreY = fieldPose.y() + Math.sin(fieldPose.heading()) * scoringStandoffInches;
        Pose scoringPose = new Pose(scoreX, scoreY, aimHeading);

        return new CellTarget(this, approachPose, scoringPose, aimHeading);
    }

    @Override
    public String toString() {
        return String.format("Cell[%s %s tag~=%d state=%s conf=%.2f pose=(%.1f,%.1f,%.0fdeg)]",
                alliance, fieldSide, primaryAprilTagId, state, confidence, fieldPose.x(), fieldPose.y(),
                Math.toDegrees(fieldPose.heading()));
    }
}
