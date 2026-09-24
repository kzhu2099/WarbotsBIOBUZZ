package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/**
 * Named autonomous starting configurations. Auto OpModes select one of
 * these instead of scattering raw field coordinates through their code.
 * Poses are stored for BLUE alliance as the canonical/measured version;
 * RED is mirrored automatically (see {@link #pose}) - measure once,
 * trust both.
 *
 * TODO: VERIFY every pose against the real field once starting tiles are
 * finalized. These are placeholders based on a generic field layout
 * (144in x 144in, origin at a corner, 0 heading = facing +X).
 */
public enum StartPosition {

    LEFT(new Pose(36, RobotGeometry.HALF_LENGTH_INCHES, Math.toRadians(90))),
    CENTER(new Pose(72, RobotGeometry.HALF_LENGTH_INCHES, Math.toRadians(90))),
    RIGHT(new Pose(108, RobotGeometry.HALF_LENGTH_INCHES, Math.toRadians(90)));

    private final Pose bluePose;

    StartPosition(Pose bluePose) {
        this.bluePose = bluePose;
    }

    /** This starting pose, mirrored across the field's centerline for RED
     *  if needed. Mirroring assumes a field symmetric about x = FIELD_SIZE/2
     *  with heading mirrored as (PI - heading) - TODO: VERIFY this matches
     *  the real field's actual symmetry once known; some fields mirror
     *  across Y instead of X depending on alliance wall layout. */
    public Pose pose(boolean redAlliance) {
        if (!redAlliance) return bluePose;

        double mirroredX = Points.FIELD_SIZE - bluePose.x();
        double mirroredHeading = normalize(Math.PI - bluePose.heading());
        return new Pose(mirroredX, bluePose.y(), mirroredHeading);
    }

    /** A conservative default parking pose near this starting position,
     *  for use as a last-resort fallback if no better parking spot has
     *  been planned. */
    public Pose fallbackParkPose(boolean redAlliance) {
        Pose start = pose(redAlliance);
        double parkX = start.x() + Math.cos(start.heading()) * 24;
        double parkY = start.y() + Math.sin(start.heading()) * 24;
        return new Pose(parkX, parkY, start.heading());
    }

    private static double normalize(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }
}
