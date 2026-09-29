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
 *
 * THAT CORRECTION LAYER IS OPTIONAL AND TOGGLEABLE ON PURPOSE - see
 * CorrectionMode. Continuously blending a vision-based reading into good
 * odometry every single time a tag is seen isn't automatically a win: this
 * robot's Pinpoint odometry is accurate and drifts very little over a
 * ~30-second autonomous period, while a single AprilTag reading's own
 * distance/angle estimate has its own real error, especially at range or a
 * steep viewing angle. Fighting two imperfect sources continuously can
 * make things WORSE than trusting one good one. The default here instead
 * matches a pattern real teams use: treat odometry as authoritative during
 * a run, and only use vision to explicitly RE-establish position when you
 * have reason to think odometry might be wrong (right after a robot
 * reset, or on driver request) - see RESYNC_ONLY and requestResync().
 *
 * CURRENT REAL LIMITATION: as of this pass, every confirmed real AprilTag
 * ID on this field (see FieldTagLibrary) is a CELL_TAG, not a
 * NAVIGATION_TAG - no dedicated, separate localization-only tags have been
 * identified for BIOBUZZ. That means this correction path currently has
 * nothing to fire against in practice, regardless of which mode is
 * selected, until either (a) BIOBUZZ turns out to have separate navigation
 * tags elsewhere that haven't been catalogued yet, or (b) this project is
 * deliberately extended to also feed CELL_TAG sightings into this same
 * correction (which is a real, defensible option - cell tags ARE fixed,
 * known-position field references too - but is NOT wired up automatically
 * here, because FieldTagLibrary's cell tag field poses are still
 * placeholders, not measured values, and correcting robot pose from a
 * wrong "known" position would actively make things worse, not better).
 * Wiring is left in place and ready for either case.
 */
public class Localization {

    public enum CorrectionMode {
        /** Never use AprilTag sightings to adjust pose - odometry only. */
        DISABLED,
        /** Do nothing until requestResync() is called; then, on the next
         *  trusted sighting, fully replace the pose estimate (not a
         *  partial blend - a resync means "I don't trust odometry right
         *  now at all"). This is the default. */
        RESYNC_ONLY,
        /** Continuously nudge pose toward every trusted sighting, all
         *  match long. Only turn this on once you've verified in testing
         *  that it actually improves accuracy for your camera/mounting
         *  rather than introducing noise. */
        CONTINUOUS
    }

    /** If a tag-based position estimate disagrees with current odometry by
     *  more than this, something is wrong (bad tag read, or the odometry
     *  has drifted badly) - don't blindly snap to it in CONTINUOUS mode.
     *  Flag it instead so AutonomousController can fall back to
     *  conservative behavior (see project brief section 25). Does not
     *  apply to an explicit resync - see requestResync(). */
    private static final double MAX_TRUSTED_CORRECTION_INCHES = 12.0;

    /** How strongly a trusted CONTINUOUS-mode correction nudges the pose
     *  vs. leaving odometry alone - a full snap-to-vision on one frame
     *  would make the robot twitch on every noisy tag read. */
    private static final double CORRECTION_BLEND_WEIGHT = 0.3;

    /** Confidence decays over this many seconds without a correction (down
     *  to a floor - odometry alone is still usable, just not vision-checked). */
    private static final double CONFIDENCE_DECAY_SECONDS = 30.0;
    private static final double MIN_CONFIDENCE = 0.5;

    private final Follower follower;

    private CorrectionMode correctionMode = CorrectionMode.RESYNC_ONLY;
    private boolean resyncRequested = false;

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

    public void setCorrectionMode(CorrectionMode mode) {
        this.correctionMode = mode;
    }

    public CorrectionMode correctionMode() {
        return correctionMode;
    }

    /**
     * Requests that the next trusted AprilTag sighting fully replace the
     * current pose estimate, regardless of mode (RESYNC_ONLY waits for
     * this; CONTINUOUS and DISABLED both still honor an explicit resync
     * request as a one-time override). Call this right after a pose reset
     * you're not fully confident in, or bind it to a driver button for
     * "I think I got bumped, please re-sync." Self-clears once used.
     */
    public void requestResync() {
        resyncRequested = true;
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
     * Attempts to correct the robot's pose using one AprilTag sighting.
     * Ignored for anything that isn't a NAVIGATION_TAG, or if
     * correctionMode is DISABLED and no resync was requested.
     *
     * Prefers observation.sdkRobotFieldPose (the FTC SDK's own directly-
     * computed, real 3D robot pose - see AprilTagObservation/CellVision)
     * when available - it's more trustworthy than this method's own
     * fallback 2D inversion math below. knownTagFieldPose is only used for
     * that fallback, when the SDK couldn't produce sdkRobotFieldPose
     * itself (e.g. a tag outside its default library). The caller
     * (Robot.updateWorldModel()) resolves knownTagFieldPose via
     * FieldTagLibrary - this class deliberately doesn't depend on that
     * registry directly, both to keep this pose-correction math
     * field-registry-agnostic and to make it testable with an arbitrary
     * known pose without needing any particular tag to actually be
     * registered anywhere.
     */
    public void applyAprilTagCorrection(AprilTagObservation observation, Pose knownTagFieldPose, double nowSeconds) {
        if (observation.role != AprilTagRole.NAVIGATION_TAG) return;

        boolean doingResync = resyncRequested;
        if (correctionMode == CorrectionMode.DISABLED && !doingResync) return;
        if (correctionMode == CorrectionMode.RESYNC_ONLY && !doingResync) return;

        Pose current = follower.pose();
        Pose estimatedRobotPose;

        if (observation.sdkRobotFieldPose != null) {
            estimatedRobotPose = observation.sdkRobotFieldPose;
        } else {
            if (knownTagFieldPose == null) return;
            // Invert AprilTagObservation.estimateFieldPose(): solve for the
            // robot position that would put the tag exactly at its known
            // field position, holding the current heading estimate fixed
            // (this fallback only ever corrects translation, never
            // heading - see estimatedRobotPose's use below).
            double heading = current.heading();
            double forward = observation.robotRelativeY;
            double lateral = observation.robotRelativeX;
            double estimatedX = knownTagFieldPose.x() - (forward * Math.cos(heading) - lateral * Math.sin(heading));
            double estimatedY = knownTagFieldPose.y() - (forward * Math.sin(heading) + lateral * Math.cos(heading));
            estimatedRobotPose = new Pose(estimatedX, estimatedY, heading);
        }

        if (doingResync) {
            // A resync means "don't trust odometry right now at all" -
            // snap fully (heading included, when the SDK provided one)
            // rather than blend, and skip the disagreement-too-large
            // safety check below, since overriding a possibly-very-wrong
            // current guess is the entire point.
            follower.setPose(estimatedRobotPose);
            lastCorrectionSeconds = nowSeconds;
            deviationFlagged = false;
            resyncRequested = false;
            return;
        }

        double dx = estimatedRobotPose.x() - current.x();
        double dy = estimatedRobotPose.y() - current.y();
        double disagreement = Math.hypot(dx, dy);

        if (disagreement > MAX_TRUSTED_CORRECTION_INCHES) {
            deviationFlagged = true;
            return;
        }

        deviationFlagged = false;
        Pose corrected = new Pose(
                current.x() + dx * CORRECTION_BLEND_WEIGHT,
                current.y() + dy * CORRECTION_BLEND_WEIGHT,
                current.heading());
        follower.setPose(corrected);
        lastCorrectionSeconds = nowSeconds;
    }
}
