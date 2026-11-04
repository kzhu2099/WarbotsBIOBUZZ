package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotorSimple;

public final class RobotConfig {
    private RobotConfig() {}

    public static final String INTAKE = "intake";
    public static final String TRANSFER = ""; // Blank disables the optional transfer motor.
    public static final String OL = "ol";
    public static final String OR = "or";

    public static final String CAM1 = "Webcam 2"; // Code name cam1; Driver Station name stays Webcam 2.

    // Measure these on the assembled robot. PathTestAuto will not start with NaN values.
    public static final double ROBOT_WIDTH_INCHES = Double.NaN;
    public static final double ROBOT_LENGTH_INCHES = Double.NaN;
    public static final double FIELD_SIZE_INCHES = 144.0;
    public static final double CORNER_INSET_INCHES = 9.0;

    // Set these four coordinates from the field layout. The path test will not run until set.
    public static final double INSIDE_LANE_X = Double.NaN;
    public static final double INSIDE_LANE_Y = Double.NaN;
    public static final double OUTSIDE_LANE_X = Double.NaN;
    public static final double OUTSIDE_LANE_Y = Double.NaN;

    public static final DcMotorSimple.Direction INTAKE_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction TRANSFER_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction OL_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction OR_DIRECTION = DcMotorSimple.Direction.FORWARD;

    public static final double JOYSTICK_DEADBAND = 0.05;

    // Change these after testing the actual robot.
    public static final double AUTO_FORWARD_POWER = 0.20;
    public static final long AUTO_FORWARD_TIME_MS = 200;

    public static final double INTAKE_POWER = 1.0;
    public static final double INTAKE_SECONDS = 2.5;
    public static final double TRANSFER_POWER = 1.0;
    public static final double TRANSFER_SECONDS = 1.5;
    public static final double OL_POWER = 1.0;
    public static final double OR_POWER = 1.0;
    public static final double SHOOT_SECONDS = 1.0;

    // Current field-CAD estimates carried over from the earlier autonomous code.
    // Verify the Hive and Flower coordinates before using them on the robot.
    public static final double HIVE_X_OFFSET_INCHES = 12.0;
    public static final double CELL_SEPARATION_INCHES = 18.8;
    public static final double HIVE_APPROACH_INCHES = 24.0;
    public static final double FLOWER_LEFT_Y_INCHES = 48.0;
    public static final double FLOWER_TOP_X_INCHES = 48.0;
    public static final double FLOWER_RIGHT_Y_INCHES = 96.0;
    public static final double FLOWER_BOTTOM_X_INCHES = 96.0;
    public static final double FLOWER_STANDOFF_INCHES = Double.NaN;
    public static final double PATH_PAUSE_SECONDS = 1.0;
    public static final double PATH_TIMEOUT_SECONDS = 8.0;

    public static boolean transferEnabled() {
        return TRANSFER != null && !TRANSFER.trim().isEmpty();
    }

    public static double driveAxis(double value) {
        return Math.abs(value) < JOYSTICK_DEADBAND ? 0.0 : value;
    }

}
