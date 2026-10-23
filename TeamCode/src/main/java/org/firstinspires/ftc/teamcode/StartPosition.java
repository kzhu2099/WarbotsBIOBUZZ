package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/**
 * BIOBUZZ starting pose. Coordinates use Pedro's 144 by 144 inch field
 * frame, with (0, 0) at the bottom-left when viewed from the audience.
 *
 * VERIFY THESE VALUES: the loading-zone centers come from the official
 * field diagram. Robot length and the exact match-field tape placement must
 * be checked on the assembled robot and event field.
 */
public enum StartPosition {

    LOADING_ZONE(new Pose(
            Points.FIELD_SIZE - RobotGeometry.HALF_LENGTH_INCHES,
            36.0,
            Math.PI
    ));

    private final Pose bluePose;

    StartPosition(Pose bluePose) {
        this.bluePose = bluePose;
    }

    public Pose pose(boolean redAlliance) {
        if (!redAlliance) return bluePose;

        double heading = bluePose.heading() + Math.PI;
        return new Pose(
                Points.FIELD_SIZE - bluePose.x(),
                Points.FIELD_SIZE - bluePose.y(),
                Math.atan2(Math.sin(heading), Math.cos(heading))
        );
    }

    public Pose fallbackParkPose(boolean redAlliance) {
        return pose(redAlliance);
    }
}
