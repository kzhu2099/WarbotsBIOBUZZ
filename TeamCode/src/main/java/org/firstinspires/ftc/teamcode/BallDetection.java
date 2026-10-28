package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/** A game-piece observation from one camera frame. */
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
