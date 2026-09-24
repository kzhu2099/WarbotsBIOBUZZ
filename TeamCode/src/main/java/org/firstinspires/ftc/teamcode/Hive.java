package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/**
 * A real model of one of the field's two hives - what the project brief
 * calls for instead of a hardcoded scoring pose. Everything the
 * autonomous planner/controller needs to answer "where do I go, which way
 * do I face, and how sure am I about any of that" lives here.
 *
 * fieldPose's heading is defined as the hive's USABLE SCORING DIRECTION:
 * the outward direction a ball travels when scored (i.e. the direction
 * pointing away from the hive, out into the field, through its opening).
 * A robot scores by standing out along that direction and facing back the
 * other way - see computeTarget().
 */
public class Hive {

    /** Below this, a sighting is treated as fresh enough to trust fully. */
    private static final double DETECTED_WINDOW_SECONDS = 0.5;
    /** Below this (but above DETECTED), still trusted for approach/aim. */
    private static final double CONFIRMED_WINDOW_SECONDS = 5.0;
    /** How much a fresh sighting moves the recorded position vs. keeping
     *  the prior estimate - smooths out single-frame noise. */
    private static final double POSITION_BLEND_WEIGHT = 0.6;

    public final HiveSide side;
    public final int aprilTagId;

    private Pose fieldPose;
    private HiveState state = HiveState.UNKNOWN;
    private double confidence = 0.0;
    private double lastDetectedAtSeconds = Double.NEGATIVE_INFINITY;
    private boolean active;

    /** How far out from the hive's opening the robot should stop to line
     *  up before committing to the final scoring position. */
    private final double approachStandoffInches;
    /** How far out the robot should actually be when it fires/drops game
     *  elements into the hive. May equal approachStandoffInches if there's
     *  no separate lineup step for this mechanism. */
    private final double scoringStandoffInches;

    public Hive(HiveSide side, int aprilTagId, Pose nominalFieldPose,
                double approachStandoffInches, double scoringStandoffInches) {
        this.side = side;
        this.aprilTagId = aprilTagId;
        this.fieldPose = nominalFieldPose;
        this.approachStandoffInches = approachStandoffInches;
        this.scoringStandoffInches = scoringStandoffInches;
    }

    /** Called by HiveMap when this hive's AprilTag is seen this cycle. */
    void updateFromSighting(Pose observedFieldPose, double observedConfidence, double nowSeconds) {
        double blendedX = fieldPose.x() + POSITION_BLEND_WEIGHT * (observedFieldPose.x() - fieldPose.x());
        double blendedY = fieldPose.y() + POSITION_BLEND_WEIGHT * (observedFieldPose.y() - fieldPose.y());
        // Heading (a mounted tag's orientation) isn't blended - a single
        // clean reading is trustworthy, and circular-mean blending isn't
        // worth the complexity here.
        this.fieldPose = new Pose(blendedX, blendedY, observedFieldPose.heading());
        this.confidence = observedConfidence;
        this.lastDetectedAtSeconds = nowSeconds;
        this.state = HiveState.DETECTED;
    }

    /** Called every cycle (whether seen or not) to age the DETECTED state
     *  toward CONFIRMED/STALE as time passes since the last real sighting. */
    void tick(double nowSeconds) {
        if (state == HiveState.UNKNOWN) return;

        double age = nowSeconds - lastDetectedAtSeconds;
        if (age <= DETECTED_WINDOW_SECONDS) {
            state = HiveState.DETECTED;
        } else if (age <= CONFIRMED_WINDOW_SECONDS) {
            state = HiveState.CONFIRMED;
        } else {
            state = HiveState.STALE;
        }
    }

    void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public Pose fieldPose() {
        return fieldPose;
    }

    public HiveState state() {
        return state;
    }

    public double confidence() {
        return confidence;
    }

    /** True once we have ever actually seen this hive's tag this match. */
    public boolean hasEverBeenDetected() {
        return state != HiveState.UNKNOWN;
    }

    /**
     * Computes where to drive and which way to face to score here, right
     * now, using the current field pose estimate. Recomputed on demand -
     * never cached/hardcoded - so it stays correct as the estimate
     * improves or the confidence/staleness situation changes.
     */
    public HiveTarget computeTarget() {
        double approachX = fieldPose.x() + Math.cos(fieldPose.heading()) * approachStandoffInches;
        double approachY = fieldPose.y() + Math.sin(fieldPose.heading()) * approachStandoffInches;
        double aimHeading = normalize(fieldPose.heading() + Math.PI);
        Pose approachPose = new Pose(approachX, approachY, aimHeading);

        double scoreX = fieldPose.x() + Math.cos(fieldPose.heading()) * scoringStandoffInches;
        double scoreY = fieldPose.y() + Math.sin(fieldPose.heading()) * scoringStandoffInches;
        Pose scoringPose = new Pose(scoreX, scoreY, aimHeading);

        return new HiveTarget(this, approachPose, scoringPose, aimHeading);
    }

    private static double normalize(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    @Override
    public String toString() {
        return String.format("Hive[%s tag=%d state=%s conf=%.2f pose=(%.1f,%.1f,%.0fdeg) active=%b]",
                side, aprilTagId, state, confidence, fieldPose.x(), fieldPose.y(),
                Math.toDegrees(fieldPose.heading()), active);
    }
}
