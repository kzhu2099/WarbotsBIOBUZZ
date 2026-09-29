package org.firstinspires.ftc.teamcode;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;

/**
 * Named autonomous starting configurations. Auto OpModes select one of
 * these instead of scattering raw field coordinates through their code.
 * Poses are stored for BLUE alliance as the canonical/measured version;
 * RED is mirrored automatically (see {@link #pose}) - measure once,
 * trust both.
 *
 * Mirroring uses PedroPathing's own PoseFactory.mirrorX() (added/fixed in
 * v3.0.1 - see that release's changelog) rather than hand-rolled mirror
 * math, since that's exactly the kind of easy-to-get-subtly-wrong
 * coordinate geometry a maintained library is worth trusting over a
 * one-off formula.
 *
 * TODO: VERIFY every pose against the real field once starting tiles are
 * finalized. These are placeholders based on a generic field layout
 * (144in x 144in, origin at a corner, 0 heading = facing +X). TODO: VERIFY
 * the field actually mirrors across x = 72 (left-right) and not across y
 * (front-back) - swap mirrorX for mirrorY below if the real field's
 * alliance walls are arranged that way instead.
 */
public enum StartPosition {

    LEFT(new Pose(36, RobotGeometry.HALF_LENGTH_INCHES, Math.toRadians(90))),
    CENTER(new Pose(72, RobotGeometry.HALF_LENGTH_INCHES, Math.toRadians(90))),
    RIGHT(new Pose(108, RobotGeometry.HALF_LENGTH_INCHES, Math.toRadians(90)));

    private static final PoseFactory MIRROR = PoseFactory.radians().mirrorX(Points.FIELD_SIZE / 2.0);

    private final Pose bluePose;

    StartPosition(Pose bluePose) {
        this.bluePose = bluePose;
    }

    /** This starting pose, mirrored across the field's centerline for RED
     *  if needed. */
    public Pose pose(boolean redAlliance) {
        if (!redAlliance) return bluePose;
        return MIRROR.of(bluePose.x(), bluePose.y(), bluePose.heading());
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
}
