package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;

/**
 * The ONE authoritative source of "where is the robot right now" for the
 * whole project. Per the project brief: GameVision, Robot, the planner,
 * and Auto must not each keep their own separate idea of robot pose - they
 * all read through this instead. Under the hood this is still just Pedro's
 * Follower/localizer (odometry), with an optional AprilTag-based
 * correction layer on top.
 */
public class Localization {

    /** If a navigation-tag-based position estimate disagrees with current
     *  odometry by more than this, something is wrong (bad tag read, or
     *  the odometry has drifted badly) - don't blindly snap to it. Flag it
     *  instead so AutonomousController can fall back to conservative
     *  behavior (see project brief section 25). */
    private static final double MAX_TRUSTED_CORRECTION_INCHES = 12.0;

    /** How strongly a trusted correction nudges the pose vs. leaving
     *  odometry alone - a full snap-to-vision on one frame would make the
     *  robot twitch on every noisy tag read. */
    private static final double CORRECTION_BLEND_WEIGHT = 0.3;

    /** Confidence decays over this many seconds without a correction (down
     *  to a floor - odometry alone is still usable, just not vision-checked). */
    private static final double CONFIDENCE_DECAY_SECONDS = 30.0;
    private static final double MIN_CONFIDENCE = 0.5;

    private final Follower follower;

    private double lastCorrectionSeconds = Double.NEGATIVE_INFINITY;
    private double confidence = 0.6;
    private boolean deviationFlagged = false;

    public Localization(Follower follower) {
        this.follower = follower;
    }

    public Pose pose() {
        return follower.pose();
    }

    public void setPose(Pose pose) {
        follower.setPose(pose);
        lastCorrectionSeconds = Double.NEGATIVE_INFINITY;
        deviationFlagged = false;
    }

    /** Call once per loop, regardless of whether a correction came in. */
    public void update(double nowSeconds) {
        if (Double.isInfinite(lastCorrectionSeconds)) {
            confidence = 0.6;
        } else {
            double sinceCorrection = nowSeconds - lastCorrectionSeconds;
            confidence = Math.max(MIN_CONFIDENCE, 1.0 - sinceCorrection / CONFIDENCE_DECAY_SECONDS);
        }
    }

    public double confidence() {
        return confidence;
    }

    /** True if the last correction attempt disagreed with odometry by more
     *  than we're willing to trust - the controller should treat this as a
     *  reason to fall back to conservative behavior, not to keep planning
     *  precision approaches. */
    public boolean hasDeviatedSignificantly() {
        return deviationFlagged;
    }

    /**
     * Attempts to correct the robot's pose using one fixed, known-position
     * navigation tag sighting. Ignored for anything that isn't a
     * NAVIGATION_TAG, or a tag this project hasn't mapped a known field
     * position for (see FieldTagLibrary).
     */
    public void applyAprilTagCorrection(AprilTagObservation observation, double nowSeconds) {
        if (observation.role != AprilTagRole.NAVIGATION_TAG) return;

        Pose knownTagFieldPose = FieldTagLibrary.knownFieldPoseOf(observation.tagId);
        if (knownTagFieldPose == null) return;

        Pose current = follower.pose();
        double heading = current.heading();
        double forward = observation.robotRelativeY;
        double lateral = observation.robotRelativeX;

        // Invert AprilTagObservation.estimateFieldPose(): solve for the
        // robot position that would put the tag exactly at its known field
        // position, holding the current heading estimate fixed (this
        // corrects translational drift; full heading correction from a
        // single tag is intentionally left as a future improvement -
        // TODO: revisit if heading drift turns out to be significant).
        double estimatedRobotX = knownTagFieldPose.x() - (forward * Math.cos(heading) - lateral * Math.sin(heading));
        double estimatedRobotY = knownTagFieldPose.y() - (forward * Math.sin(heading) + lateral * Math.cos(heading));

        double dx = estimatedRobotX - current.x();
        double dy = estimatedRobotY - current.y();
        double disagreement = Math.hypot(dx, dy);

        if (disagreement > MAX_TRUSTED_CORRECTION_INCHES) {
            deviationFlagged = true;
            return;
        }

        deviationFlagged = false;
        Pose corrected = new Pose(
                current.x() + dx * CORRECTION_BLEND_WEIGHT,
                current.y() + dy * CORRECTION_BLEND_WEIGHT,
                heading);
        follower.setPose(corrected);
        lastCorrectionSeconds = nowSeconds;
    }
}
