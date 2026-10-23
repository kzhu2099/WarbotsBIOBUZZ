package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

public final class BioBuzzAutoConfig {

    private BioBuzzAutoConfig() {}

    public static final double FIELD_SIZE_INCHES = Points.FIELD_SIZE;

    // VERIFY THESE VALUES against the official CAD and your event field.
    public static final double LOADING_ZONE_CENTER_Y_INCHES = 36.0;
    public static final double FLOWER_CENTER_Y_INCHES = FIELD_SIZE_INCHES / 2.0;
    public static final double FLOWER_ROBOT_CENTER_FROM_WALL_INCHES = 15.0;

    public static final double LEAVE_DISTANCE_INCHES = 8.0;
    public static final double AUTO_SAFETY_CUTOFF_SECONDS = 28.0;
    public static final double AIM_TOLERANCE_RADIANS = Math.toRadians(3.0);
    public static final double AIM_TIMEOUT_SECONDS = 2.0;

    public static Pose flowerApproachPose(boolean redAlliance) {
        double x = redAlliance
                ? FLOWER_ROBOT_CENTER_FROM_WALL_INCHES
                : FIELD_SIZE_INCHES - FLOWER_ROBOT_CENTER_FROM_WALL_INCHES;
        double heading = redAlliance ? Math.PI : 0.0;
        return new Pose(x, FLOWER_CENTER_Y_INCHES, heading);
    }
}
