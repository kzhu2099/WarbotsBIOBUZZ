package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/**
 * A single game element (Pollen or Nectar) as the robot currently
 * understands it - whether it's in view right now, or only remembered
 * from a previous sighting. This is what {@link BallMap} stores, what
 * {@link AutonomousPlanner} scores/selects, and what shows up in
 * telemetry as "TARGET BALL".
 */
public class Ball {

    private static int nextId = 1;

    public final int id;
    public final BallType type;

    private BallOwner owner;
    private BallState state;
    private Pose fieldPose;

    /** Rough radius of position uncertainty, inches. Grows the longer a
     *  ball goes unseen; shrinks (down to a sensor-accuracy floor) each
     *  time it is freshly re-observed. */
    private double positionUncertaintyInches;

    /** How sure we are this is a real, correctly-classified ball, 0..1. */
    private double confidence;

    private double lastSeenTimeSeconds;
    private int timesSeen;

    Ball(BallType type, BallOwner owner, Pose fieldPose, double confidence, double nowSeconds) {
        this.id = nextId++;
        this.type = type;
        this.owner = owner;
        this.fieldPose = fieldPose;
        this.confidence = confidence;
        this.state = BallState.VISIBLE;
        this.positionUncertaintyInches = BallMap.FRESH_SIGHTING_UNCERTAINTY_INCHES;
        this.lastSeenTimeSeconds = nowSeconds;
        this.timesSeen = 1;
    }

    /** Only for tests that need deterministic IDs across runs. */
    static void resetIdsForTest() {
        nextId = 1;
    }

    public BallOwner owner() {
        return owner;
    }

    public BallState state() {
        return state;
    }

    public Pose fieldPose() {
        return fieldPose;
    }

    public double positionUncertaintyInches() {
        return positionUncertaintyInches;
    }

    public double confidence() {
        return confidence;
    }

    public double lastSeenTimeSeconds() {
        return lastSeenTimeSeconds;
    }

    public int timesSeen() {
        return timesSeen;
    }

    public boolean isActiveTarget() {
        return state == BallState.VISIBLE || state == BallState.REMEMBERED;
    }

    void reobserve(BallOwner owner, Pose fieldPose, double confidence, double nowSeconds) {
        // A fresh sighting always wins on position/owner - it is strictly
        // better information than a stale memory.
        this.owner = owner;
        this.fieldPose = fieldPose;
        this.confidence = Math.max(this.confidence, confidence);
        this.state = BallState.VISIBLE;
        this.positionUncertaintyInches = BallMap.FRESH_SIGHTING_UNCERTAINTY_INCHES;
        this.lastSeenTimeSeconds = nowSeconds;
        this.timesSeen++;
    }

    void ageOutOfView(double nowSeconds) {
        if (state == BallState.VISIBLE) {
            state = BallState.REMEMBERED;
        }
        double secondsUnseen = nowSeconds - lastSeenTimeSeconds;
        // Uncertainty grows over time so a stale memory is treated as "it
        // could be roughly here" rather than "it is exactly here."
        positionUncertaintyInches = BallMap.FRESH_SIGHTING_UNCERTAINTY_INCHES
                + secondsUnseen * BallMap.UNCERTAINTY_GROWTH_INCHES_PER_SECOND;
    }

    void markCollected() {
        state = BallState.COLLECTED;
    }

    void markMissing() {
        state = BallState.MISSING;
    }

    void markInvalid() {
        state = BallState.INVALID;
    }

    @Override
    public String toString() {
        return String.format("Ball#%d[%s/%s @ (%.1f,%.1f) state=%s unc=%.1fin conf=%.2f]",
                id, type, owner, fieldPose.x(), fieldPose.y(), state, positionUncertaintyInches, confidence);
    }
}
