package org.firstinspires.ftc.teamcode;

/**
 * Which side of the field a Cell structure is on, relative to the
 * audience. Added once the real AprilTag layout confirmed each alliance
 * actually has TWO physical cell structures - one on the audience side of
 * the field, one on the side opposite the audience - not just one per
 * alliance as originally assumed. See FieldTagLibrary for the real tag IDs.
 */
public enum FieldSide {
    AUDIENCE,
    OPPOSITE
}
