package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/**
 * One structured AprilTag observation, produced by {@link HiveVision} from
 * a single raw SDK {@code AprilTagDetection}. This is what lets the robot
 * go beyond "AprilTag detected" to "here is what that tag means and where
 * it (and whatever it's attached to) actually is."
 *
 * distance/bearing/robotRelativeX/Y/yaw all come directly from the SDK's
 * {@code ftcPose} (range, bearing, x, y, yaw respectively) - see
 * HiveVision for the exact mapping and units.
 */
public class AprilTagObservation {

    public final int tagId;
    public final AprilTagRole role;

    /** Straight-line distance from camera to tag, inches. */
    public final double distanceInches;

    /** Angle from camera's forward axis to the tag, degrees (SDK "bearing";
     *  positive = tag is to the left, matching ftcPose convention). */
    public final double bearingDegrees;

    /** Tag position in the robot/camera-relative frame, inches (SDK
     *  ftcPose.x/.y - x is lateral, y is forward, per FTC convention). */
    public final double robotRelativeX;
    public final double robotRelativeY;

    /** Tag's own rotation about the vertical axis relative to the camera,
     *  degrees (SDK ftcPose.yaw). */
    public final double yawDegrees;

    /** 0..1 detection confidence proxy (see HiveVision for how this is
     *  derived - the SDK does not hand back a single confidence number, so
     *  this is built from decision margin / staleness). */
    public final double confidence;

    public final double timestampSeconds;

    public AprilTagObservation(int tagId, AprilTagRole role, double distanceInches, double bearingDegrees,
                                double robotRelativeX, double robotRelativeY, double yawDegrees,
                                double confidence, double timestampSeconds) {
        this.tagId = tagId;
        this.role = role;
        this.distanceInches = distanceInches;
        this.bearingDegrees = bearingDegrees;
        this.robotRelativeX = robotRelativeX;
        this.robotRelativeY = robotRelativeY;
        this.yawDegrees = yawDegrees;
        this.confidence = confidence;
        this.timestampSeconds = timestampSeconds;
    }

    /**
     * Projects this observation into field coordinates, given the robot's
     * field pose at (approximately) the moment of capture. Used both to
     * estimate a hive's field position from a hive-tag sighting, and (in
     * reverse, see {@link Localization}) to correct the robot's own pose
     * from a navigation-tag sighting whose true field position is known.
     *
     * robotRelativeY is "forward" and robotRelativeX is "lateral" (FTC
     * ftcPose convention), so this rotates (y=forward, x=right) by the
     * robot's field heading before adding it to the robot's field
     * position.
     */
    public Pose estimateFieldPose(Pose robotFieldPose) {
        double heading = robotFieldPose.heading();
        double forward = robotRelativeY;
        double lateral = robotRelativeX;

        double fieldX = robotFieldPose.x() + forward * Math.cos(heading) - lateral * Math.sin(heading);
        double fieldY = robotFieldPose.y() + forward * Math.sin(heading) + lateral * Math.cos(heading);
        double fieldHeading = normalize(heading + Math.toRadians(yawDegrees));

        return new Pose(fieldX, fieldY, fieldHeading);
    }

    private static double normalize(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }
}
