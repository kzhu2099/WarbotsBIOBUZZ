package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/**
 * One structured ball observation from a single camera frame, as produced
 * by {@link GameVision}. This replaces "boolean seesBall()" - a single
 * frame can (and often will) contain several of these at once.
 *
 * cameraX/cameraY are normalized image-plane coordinates (-1..1, 0 =
 * center) so callers that just want "is it left or right of center" don't
 * need to know the frame resolution. bearingDegrees/distanceInches and
 * fieldPose are the camera's best estimate of where the ball actually is,
 * using the robot pose at capture time - see GameVision for exactly how
 * that estimate is computed and its known limitations (monocular distance
 * estimate from blob size, not true depth).
 */
public class BallDetection {

    public final BallType type;
    public final BallOwner owner;

    public final double cameraX;
    public final double cameraY;

    public final double bearingDegrees;
    public final double distanceInches;

    /** Estimated field position at the moment of capture. */
    public final Pose fieldPose;

    /** 0..1, how confident the vision pipeline is in type+owner+position. */
    public final double confidence;

    public final double timestampSeconds;

    public BallDetection(BallType type, BallOwner owner, double cameraX, double cameraY,
                          double bearingDegrees, double distanceInches, Pose fieldPose,
                          double confidence, double timestampSeconds) {
        this.type = type;
        this.owner = owner;
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.bearingDegrees = bearingDegrees;
        this.distanceInches = distanceInches;
        this.fieldPose = fieldPose;
        this.confidence = confidence;
        this.timestampSeconds = timestampSeconds;
    }
}
