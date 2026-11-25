package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction;

/**
 * Hardware names and tunables. Field positions and shots: Spots.java. Pedro tuning: pedro/Constants.java.
 * A name of "" (or one missing from the configuration) is skipped and shown as missing.
 */
public final class Settings {
    private Settings() {}

    // ---------------- DRIVE ----------------
    public static final String FRONT_LEFT = "fl";
    public static final String FRONT_RIGHT = "fr";
    public static final String BACK_LEFT = "bl";
    public static final String BACK_RIGHT = "br";

    // From Pedro's Mecanum Tuner.
    public static final Direction FRONT_LEFT_DIR = Direction.REVERSE;
    public static final Direction FRONT_RIGHT_DIR = Direction.FORWARD;
    public static final Direction BACK_LEFT_DIR = Direction.REVERSE;
    public static final Direction BACK_RIGHT_DIR = Direction.FORWARD;

    public static final String PINPOINT = "odom";

    public static final double STICK_DEADZONE = 0.05;
    public static final double SLOW_MODE_POWER = 0.40;   // hold left bumper on gamepad 1

    // ---------------- MECHANISMS ----------------
    public static final String INTAKE = "intake";
    public static final String TRANSFER = "transfer";
    public static final String OUTTAKE = "outtake";
    public static final String ANGLE_SERVO = "shooterAngle";

    public static final Direction INTAKE_DIR = Direction.FORWARD;
    public static final Direction TRANSFER_DIR = Direction.FORWARD;
    public static final Direction OUTTAKE_DIR = Direction.FORWARD;

    public static final double INTAKE_POWER = 1.0;
    public static final double TRANSFER_POWER = 1.0;

    // Encoder ticks per motor revolution (bare goBILDA 5203 = 28).
    public static final double OUTTAKE_TICKS_PER_REV = 28;

    public static final double OUTTAKE_READY_RPM = 150;  // "ready" window around the target

    // Angle servo is untested: keep it mid-travel until its real limits are measured.
    public static final double ANGLE_LOWEST = 0.30;
    public static final double ANGLE_HIGHEST = 0.70;
    public static final double ANGLE_STEP = 0.02;      // per D-pad press
    public static final double RPM_STEP = 100;         // per D-pad press

    // ---------------- AUTO TIMING ----------------
    public static final double SPIN_UP_MAX_SECONDS = 2.0;  // feed anyway after this
    public static final double FEED_SECONDS = 2.0;         // run transfer to fire all balls
    public static final double FLOWER_INTAKE_SECONDS = 2.0; // sit at a flower with intake on
    public static final double DRIVE_TIMEOUT_SECONDS = 5.0; // one drive step gives up after this

    public static final double DONE_INCHES = 1.5;
    public static final double DONE_DEGREES = 4.0;

    // After this, remaining auto steps are skipped and the robot parks.
    public static final double GO_PARK_AFTER_SECONDS = 24.0;
}
