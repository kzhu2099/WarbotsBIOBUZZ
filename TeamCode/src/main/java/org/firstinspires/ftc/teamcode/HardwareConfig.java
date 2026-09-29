package org.firstinspires.ftc.teamcode;

/**
 * Every hardware-map device name the robot uses, in one place. This is the
 * ONE file that should need editing at a meet to point the code at renamed
 * devices - nothing else in the project should contain a hardware-map name
 * as a string literal.
 *
 * CONFIRMED (not a guess): this robot has exactly eight motors and no
 * mechanism sensors at all - four drivetrain, plus intake, transfer, and
 * two outtake motors (both fire together for every scoring cycle). There
 * is no beam-break, color sensor, distance sensor, or limit switch on the
 * mechanism. ScoringMechanism is written accordingly: ball acquisition and
 * transfer-arrival are timing-based assumptions, not sensor-verified, and
 * that is a real, permanent property of this robot - not a placeholder
 * waiting on hardware that's still TODO.
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
    // Camera 2: upward-facing, AprilTag / cell detection (CellVision).
    public static final String CELL_CAMERA_NAME = "Webcam 2";

    // ---- Mechanisms --------------------------------------------------------
    // Exactly four mechanism motors, confirmed - no sensors of any kind.
    // TODO: VERIFY HARDWARE NAME against the real Driver Station config.
    public static final String INTAKE_MOTOR = "intake";
    public static final String TRANSFER_MOTOR = "transfer";
    // Both outtake motors fire together for every scoring cycle - see
    // ScoringMechanism.triggerOuttake().
    public static final String OUTTAKE_LEFT_MOTOR = "outtakeLeft";
    public static final String OUTTAKE_RIGHT_MOTOR = "outtakeRight";
}
