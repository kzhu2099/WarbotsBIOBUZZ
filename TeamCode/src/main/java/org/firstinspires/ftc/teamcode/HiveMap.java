package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/**
 * Tracks both hives on the field and answers the question the project
 * brief poses directly: "which hive am I currently supposed to score at?"
 * - never a hardcoded pose, always derived from alliance + AprilTag
 * observations.
 *
 * Both hives are tracked (not just "ours") because the vision system may
 * see either tag, and because a team strategy might eventually care about
 * the opponent hive's state too (e.g. congestion). But only one is ever
 * marked active for our own scoring purposes.
 */
public class HiveMap {

    // TODO: VERIFY standoff distances once the real outtake mechanism and
    // hive geometry are known. Approach is where the robot lines up before
    // committing; scoring is where it actually is when it fires/drops.
    private static final double APPROACH_STANDOFF_INCHES = 24.0;
    private static final double SCORING_STANDOFF_INCHES = 16.0;

    private final Hive redHive;
    private final Hive blueHive;

    public HiveMap() {
        Pose redNominal = FieldTagLibrary.knownFieldPoseOf(FieldTagLibrary.redHiveTagId());
        Pose blueNominal = FieldTagLibrary.knownFieldPoseOf(FieldTagLibrary.blueHiveTagId());
        redHive = new Hive(HiveSide.RED, FieldTagLibrary.redHiveTagId(), redNominal,
                APPROACH_STANDOFF_INCHES, SCORING_STANDOFF_INCHES);
        blueHive = new Hive(HiveSide.BLUE, FieldTagLibrary.blueHiveTagId(), blueNominal,
                APPROACH_STANDOFF_INCHES, SCORING_STANDOFF_INCHES);
    }

    public Hive hive(HiveSide side) {
        return side == HiveSide.RED ? redHive : blueHive;
    }

    /**
     * Sets which hive is the one we score into (our own alliance's), and
     * clears the flag on the other one. Call this once alliance is known
     * (init) and again any time it changes (e.g. driver toggles alliance
     * during init_loop).
     */
    public Hive selectOurHive(boolean redAlliance) {
        HiveSide ourSide = HiveSide.ofAlliance(redAlliance);
        redHive.setActive(ourSide == HiveSide.RED);
        blueHive.setActive(ourSide == HiveSide.BLUE);
        return hive(ourSide);
    }

    public Hive activeHive() {
        return redHive.isActive() ? redHive : (blueHive.isActive() ? blueHive : null);
    }

    /** Feeds one AprilTag observation in. Only observations already
     *  classified as HIVE_TAG should be passed here - see Robot's
     *  perception update. */
    public void updateFromTag(AprilTagObservation observation, Pose robotFieldPose, double nowSeconds) {
        if (observation.role != AprilTagRole.HIVE_TAG) return;

        HiveSide side = FieldTagLibrary.hiveSideOf(observation.tagId);
        if (side == null) return;

        Pose observedFieldPose = observation.estimateFieldPose(robotFieldPose);
        hive(side).updateFromSighting(observedFieldPose, observation.confidence, nowSeconds);
    }

    /** Ages both hives' DETECTED/CONFIRMED/STALE state. Call once per loop
     *  regardless of whether a tag was seen this cycle. */
    public void tick(double nowSeconds) {
        redHive.tick(nowSeconds);
        blueHive.tick(nowSeconds);
    }
}
