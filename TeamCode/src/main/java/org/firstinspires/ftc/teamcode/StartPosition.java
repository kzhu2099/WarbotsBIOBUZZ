package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/**
 * BIOBUZZ starting pose. Coordinates use Pedro's 144 by 144 inch field
 * frame, with (0, 0) at the bottom-left when viewed from the audience.
 *
 * VERIFY THESE VALUES: the field-side and Y position below are nominal
 * estimates, not measured robot starting marks. Confirm the loading-zone
 * tape, robot orientation, and bumper clearance on the official CAD/event field.
 */
public enum StartPosition {

    LOADING_ZONE(new Pose(
            Points.FIELD_SIZE - RobotGeometry.HALF_LENGTH_INCHES,
            BioBuzzAutoConfig.LOADING_ZONE_CENTER_Y_INCHES,
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
                bluePose.y(),
                Math.atan2(Math.sin(heading), Math.cos(heading))
        );
    }

    public Pose fallbackParkPose(boolean redAlliance) {
        Pose start = pose(redAlliance);
        return new Pose(
                start.x() + Math.cos(start.heading()) * 24.0,
                start.y() + Math.sin(start.heading()) * 24.0,
                start.heading()
        );
    }
}
