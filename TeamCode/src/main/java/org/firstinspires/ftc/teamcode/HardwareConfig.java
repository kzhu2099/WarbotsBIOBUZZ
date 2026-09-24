package org.firstinspires.ftc.teamcode;

/**
 * Every hardware-map device name the robot uses, in one place, plus flags
 * for which optional sensors are actually installed. This is the ONE file
 * that should need editing at a meet to point the code at renamed devices
 * - nothing else in the project should contain a hardware-map name as a
 * string literal.
 *
 * (Drivetrain motor names and directions live in {@code pedro.Constants}
 * instead, because PedroPathing's Mecanum/MecanumConfig owns that wiring
 * directly - duplicating the same four names here would just create a
 * second place they could drift out of sync. Constants.java pulls its
 * names from here so there is still only one place to edit.)
 */
public final class HardwareConfig {

    private HardwareConfig() {}

    // ---- Drivetrain (used by pedro.Constants) --------------------------
    // TODO: VERIFY HARDWARE NAME - confirm against the actual Driver
    // Station hardware configuration file.
    public static final String FRONT_LEFT_DRIVE = "fl";
    public static final String FRONT_RIGHT_DRIVE = "fr";
    public static final String BACK_LEFT_DRIVE = "bl";
    public static final String BACK_RIGHT_DRIVE = "br";

    // ---- Odometry --------------------------------------------------------
    // TODO: VERIFY HARDWARE NAME
    public static final String ODOMETRY_COMPUTER = "odom";

    // ---- Cameras ----------------------------------------------------------
    // Camera 1: floor-facing, ball detection (GameVision).
    public static final String BALL_CAMERA_NAME = "Webcam 1";
    // Camera 2: upward-facing, AprilTag / hive detection (HiveVision).
    public static final String HIVE_CAMERA_NAME = "Webcam 2";

    // ---- Mechanisms --------------------------------------------------------
    // TODO: VERIFY HARDWARE NAME - these are placeholders until the real
    // mechanism is built and named in the hardware configuration.
    public static final String INTAKE_MOTOR = "intake";
    public static final String TRANSFER_MOTOR = "transfer";
    // The outtake mechanism is two motors, not one generic "launcher."
    public static final String OUTTAKE_LEFT_MOTOR = "outtakeLeft";
    public static final String OUTTAKE_RIGHT_MOTOR = "outtakeRight";

    // ---- Optional sensors ---------------------------------------------
    // Each sensor has both a name AND a flag for whether it is actually
    // wired up. ScoringMechanism checks the flag (not just "did
    // hardwareMap.get() throw") so the team can deliberately develop
    // without a sensor installed yet and get predictable, documented
    // fallback behavior instead of a mysteriously-empty try/catch result.

    /** Detects a ball entering the intake mouth. */
    public static final boolean HAS_INTAKE_BEAM_BREAK = false;
    public static final String INTAKE_BEAM_BREAK = "intakeBreak";

    /** Detects a ball has reached the ready-to-score position, and can
     *  optionally report its color to distinguish Pollen vs. Nectar. */
    public static final boolean HAS_TRANSFER_COLOR_SENSOR = false;
    public static final String TRANSFER_COLOR_SENSOR = "transferColor";

    /** Alternative/companion to the color sensor for confirming a ball is
     *  present at the ready position, if a color sensor alone is unreliable. */
    public static final boolean HAS_TRANSFER_DISTANCE_SENSOR = false;
    public static final String TRANSFER_DISTANCE_SENSOR = "transferDistance";

    /** Confirms the outtake path is physically clear (e.g. a limit switch
     *  on a flap/gate), if the mechanism has one. */
    public static final boolean HAS_OUTTAKE_LIMIT_SWITCH = false;
    public static final String OUTTAKE_LIMIT_SWITCH = "outtakeLimit";
}
