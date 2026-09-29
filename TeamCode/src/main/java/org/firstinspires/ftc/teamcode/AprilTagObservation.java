package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;
import com.pedropathing.utils.Angle;

/**
 * One structured AprilTag observation, produced by {@link CellVision} from
 * a single raw SDK {@code AprilTagDetection}. This is what lets the robot
 * go beyond "AprilTag detected" to "here is what that tag means and where
 * it (and whatever it's attached to) actually is."
 *
 * distance/bearing/robotRelativeX/Y/yaw all come directly from the SDK's
 * {@code ftcPose} (range, bearing, x, y, yaw respectively) - see
 * CellVision for the exact mapping and units.
 *
 * sdkRobotFieldPose (new) carries the SDK's OWN directly-computed
 * field-relative robot pose, when the SDK could produce one (from
 * {@code detection.robotPose} - only populated when the tag is in the
 * (default, current-season) tag library AND the camera's mount pose was
 * configured via setCameraPose() - see CellVision). When present, this is
 * more trustworthy than this class's own estimateFieldPose()/inversion
 * math below: it's a real 3D computation done by the SDK, not this
 * project's simplified 2D approximation. estimateFieldPose() remains as a
 * fallback for tags the SDK's default library doesn't know about (kept
 * plain-Pose-only, with no FTC SDK type dependency, specifically so this
 * class stays usable in the project's plain-Java test harness).
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

    /** 0..1 detection confidence proxy (see CellVision for how this is
     *  derived - the SDK does not hand back a single confidence number, so
     *  this is built from decision margin / staleness). */
    public final double confidence;

    public final double timestampSeconds;

    /** The SDK's own computed robot field pose for this detection, or null
     *  if it couldn't produce one (unknown tag, or a cluster reading
     *  without full pose data). See class Javadoc. */
    public final Pose sdkRobotFieldPose;

    public AprilTagObservation(int tagId, AprilTagRole role, double distanceInches, double bearingDegrees,
                                double robotRelativeX, double robotRelativeY, double yawDegrees,
                                double confidence, double timestampSeconds) {
        this(tagId, role, distanceInches, bearingDegrees, robotRelativeX, robotRelativeY, yawDegrees,
                confidence, timestampSeconds, null);
    }

    public AprilTagObservation(int tagId, AprilTagRole role, double distanceInches, double bearingDegrees,
                                double robotRelativeX, double robotRelativeY, double yawDegrees,
                                double confidence, double timestampSeconds, Pose sdkRobotFieldPose) {
        this.tagId = tagId;
        this.role = role;
        this.distanceInches = distanceInches;
        this.bearingDegrees = bearingDegrees;
        this.robotRelativeX = robotRelativeX;
        this.robotRelativeY = robotRelativeY;
        this.yawDegrees = yawDegrees;
        this.confidence = confidence;
        this.timestampSeconds = timestampSeconds;
        this.sdkRobotFieldPose = sdkRobotFieldPose;
    }

    /**
     * Projects this observation into field coordinates, given the robot's
     * field pose at (approximately) the moment of capture. Used to
     * estimate a cell's field position from a cell-tag sighting (there is
     * no SDK-native "where is this OTHER thing" equivalent to
     * sdkRobotFieldPose - that field only ever tells you about the robot).
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
        double fieldHeading = Angle.normalizeSigned(heading + Math.toRadians(yawDegrees));

        return new Pose(fieldX, fieldY, fieldHeading);
    }
}
