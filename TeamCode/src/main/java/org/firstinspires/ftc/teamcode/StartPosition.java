package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

// All three starts are outside the Loading Zone and face into the field.
public enum StartPosition {
    SIDE_WALL,
    NEAR_CORNER,
    FAR_CORNER;

    public Pose pose(boolean redAlliance) {
        double halfWidth = RobotGeometry.HALF_WIDTH_INCHES;
        double halfLength = RobotGeometry.HALF_LENGTH_INCHES;
        double field = Points.FIELD_SIZE;

        switch (this) {
            case NEAR_CORNER:
                return redAlliance
                        ? new Pose(halfWidth + 9.0, field - halfLength, -Math.PI / 2.0)
                        : new Pose(field - halfWidth - 9.0, halfLength, Math.PI / 2.0);

            case FAR_CORNER:
                return redAlliance
                        ? new Pose(halfWidth + 9.0, halfLength, Math.PI / 2.0)
                        : new Pose(field - halfWidth - 9.0, field - halfLength, -Math.PI / 2.0);

            case SIDE_WALL:
            default:
                return redAlliance
                        ? new Pose(halfLength, field / 2.0, 0.0)
                        : new Pose(field - halfLength, field / 2.0, Math.PI);
        }
    }

    public Pose fallbackParkPose(boolean redAlliance) {
        return BioBuzzAutoConfig.loadingZonePose(redAlliance);
    }
}
