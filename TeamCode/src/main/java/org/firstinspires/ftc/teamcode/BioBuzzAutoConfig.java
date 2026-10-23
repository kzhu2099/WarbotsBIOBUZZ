package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

public final class BioBuzzAutoConfig {

    private BioBuzzAutoConfig() {}

    public static final double FIELD_SIZE_INCHES = Points.FIELD_SIZE;

    // CHECK THESE AGAINST FIELD CAD. They are read from Figure 9-2, not survey measurements.
    public static final double BLUE_WALL_START_Y_INCHES = 36.0;
    public static final double FLOWER_LEFT_Y_INCHES = 72.0;
    public static final double FLOWER_TOP_X_INCHES = 48.0;
    public static final double FLOWER_RIGHT_Y_INCHES = 96.0;
    public static final double FLOWER_BOTTOM_X_INCHES = 96.0;

    public static final double LEAVE_DISTANCE_INCHES = 8.0;
    public static final double PARK_START_SECONDS = 22.0;
    public static final double PARK_TIMEOUT_SECONDS = 7.0;
    public static final double AUTO_SAFETY_CUTOFF_SECONDS = 29.5;
    public static final double AIM_TOLERANCE_RADIANS = Math.toRadians(3.0);
    public static final double AIM_TIMEOUT_SECONDS = 2.0;

    public enum Flower {
        LEFT_WALL(new Pose(0.0, FLOWER_LEFT_Y_INCHES, Math.PI)),
        TOP_WALL(new Pose(FLOWER_TOP_X_INCHES, FIELD_SIZE_INCHES, Math.PI / 2.0)),
        RIGHT_WALL(new Pose(FIELD_SIZE_INCHES, FLOWER_RIGHT_Y_INCHES, 0.0)),
        BOTTOM_WALL(new Pose(FLOWER_BOTTOM_X_INCHES, 0.0, -Math.PI / 2.0));

        public final Pose fieldPose;

        Flower(Pose fieldPose) {
            this.fieldPose = fieldPose;
        }
    }

    public static Pose leavePose(Pose start) {
        return new Pose(
                start.x() + Math.cos(start.heading()) * LEAVE_DISTANCE_INCHES,
                start.y() + Math.sin(start.heading()) * LEAVE_DISTANCE_INCHES,
                start.heading()
        );
    }

    public static Pose loadingZonePose(boolean redAlliance) {
        double xOffset = RobotGeometry.HALF_LENGTH_INCHES + 1.0;
        double yOffset = RobotGeometry.HALF_WIDTH_INCHES + 1.0;

        if (redAlliance) {
            return new Pose(xOffset, FIELD_SIZE_INCHES - yOffset, 0.0);
        }

        return new Pose(
                FIELD_SIZE_INCHES - xOffset,
                yOffset,
                Math.PI
        );
    }

    public static Flower closestFlower(Pose from) {
        Flower closest = Flower.LEFT_WALL;
        double bestDistance = Double.POSITIVE_INFINITY;

        for (Flower flower : Flower.values()) {
            Pose approach = flowerApproachPose(flower);
            double distance = Math.hypot(approach.x() - from.x(), approach.y() - from.y());
            if (distance < bestDistance) {
                bestDistance = distance;
                closest = flower;
            }
        }

        return closest;
    }

    public static Pose flowerApproachPose(Flower flower) {
        double wallDistance = Math.max(
                RobotGeometry.HALF_LENGTH_INCHES + 1.0,
                RobotGeometry.INTAKE_REACH_INCHES - 1.0
        );

        Pose flowerPose = flower.fieldPose;
        return new Pose(
                flowerPose.x() - Math.cos(flowerPose.heading()) * wallDistance,
                flowerPose.y() - Math.sin(flowerPose.heading()) * wallDistance,
                flowerPose.heading()
        );
    }
}
