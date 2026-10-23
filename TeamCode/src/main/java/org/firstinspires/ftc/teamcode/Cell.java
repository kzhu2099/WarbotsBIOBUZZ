package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;
import com.pedropathing.utils.Angle;

/** One Cell's tag identity, field pose, tracking state, and approach/scoring poses. The pose heading points outward through its opening; the robot faces back toward the Cell to score. The paired-Hive tipping state is not modeled here. */
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

    public double lastDetectedAtSeconds() {
        return lastDetectedAtSeconds;
    }

    void setInferredFieldPose(Pose inferredFieldPose) {
        this.fieldPose = inferredFieldPose;
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

    public CellTarget computeTarget(Pose robotPose) {
        double dx = robotPose.x() - fieldPose.x();
        double dy = robotPose.y() - fieldPose.y();
        double distance = Math.hypot(dx, dy);

        if (distance < 1e-6) {
            dx = Math.cos(fieldPose.heading());
            dy = Math.sin(fieldPose.heading());
            distance = 1.0;
        }

        double ux = dx / distance;
        double uy = dy / distance;
        double approachX = fieldPose.x() + ux * approachStandoffInches;
        double approachY = fieldPose.y() + uy * approachStandoffInches;
        double scoringX = fieldPose.x() + ux * scoringStandoffInches;
        double scoringY = fieldPose.y() + uy * scoringStandoffInches;
        double aimHeading = Math.atan2(fieldPose.y() - approachY, fieldPose.x() - approachX);

        return new CellTarget(
                this,
                new Pose(approachX, approachY, aimHeading),
                new Pose(scoringX, scoringY, aimHeading),
                aimHeading
        );
    }

    @Override
    public String toString() {
        return String.format("Cell[%s %s tag~=%d state=%s conf=%.2f pose=(%.1f,%.1f,%.0fdeg)]",
                alliance, fieldSide, primaryAprilTagId, state, confidence, fieldPose.x(), fieldPose.y(),
                Math.toDegrees(fieldPose.heading()));
    }
}
