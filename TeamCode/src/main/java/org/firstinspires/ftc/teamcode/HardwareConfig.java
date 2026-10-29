package org.firstinspires.ftc.teamcode;

/** The hardware-map names used by the robot code. */
public final class HardwareConfig {

    private HardwareConfig() {}

    // ---- Drivetrain (used by pedro.Constants) --------------------------
    public static final String FRONT_LEFT_DRIVE = "fl";
    public static final String FRONT_RIGHT_DRIVE = "fr";
    public static final String BACK_LEFT_DRIVE = "bl";
    public static final String BACK_RIGHT_DRIVE = "br";

    // ---- Odometry --------------------------------------------------------
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
