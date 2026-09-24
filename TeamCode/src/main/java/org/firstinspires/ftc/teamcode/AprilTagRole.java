package org.firstinspires.ftc.teamcode;

/**
 * What a given AprilTag ID means to the robot, per the "the robot should
 * not simply say 'AprilTag detected' - it should determine what the tag
 * means" requirement.
 */
public enum AprilTagRole {
    /** Identifies a hive (its alliance and field position/orientation). */
    HIVE_TAG,

    /** A fixed field tag usable to correct/validate robot localization. */
    NAVIGATION_TAG,

    /** Recognized by the tag library but not meaningful to this robot. */
    IRRELEVANT
}
