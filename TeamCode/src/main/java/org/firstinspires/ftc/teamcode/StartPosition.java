package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

public enum StartPosition {

    SIDE_WALL(new Pose(
            Points.FIELD_SIZE - RobotGeometry.HALF_LENGTH_INCHES,
            BioBuzzAutoConfig.BLUE_WALL_START_Y_INCHES,
            Math.PI
    ));

    private final Pose bluePose;

    StartPosition(Pose bluePose) {
        this.bluePose = bluePose;
    }

    public Pose pose(boolean redAlliance) {
        if (!redAlliance) return bluePose;

        return new Pose(
                Points.FIELD_SIZE - bluePose.x(),
                Points.FIELD_SIZE - bluePose.y(),
                0.0
        );
    }

    public Pose fallbackParkPose(boolean redAlliance) {
        return BioBuzzAutoConfig.loadingZonePose(redAlliance);
    }
}
