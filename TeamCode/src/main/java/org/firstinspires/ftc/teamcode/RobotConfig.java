package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotorSimple;

public final class RobotConfig {
    private RobotConfig() {}

    public static final String INTAKE = "intake";
    public static final String TRANSFER = ""; // Blank keeps the transfer motor disabled.
    public static final String OL = "ol";
    public static final String OR = "or";

    public static final String CAM1 = "Webcam 2"; // Driver Station name for the webcam.

    // Measure the chassis, including bumpers. PathTestAuto checks these before starting.
    public static final double ROBOT_WIDTH_INCHES = Double.NaN;
    public static final double ROBOT_LENGTH_INCHES = Double.NaN;
    public static final double FIELD_SIZE_INCHES = 144.0;
    public static final double CORNER_INSET_INCHES = 9.0;

    // Set both lane points from the current field layout before running PathTestAuto.
    public static final double INSIDE_LANE_X = Double.NaN;
    public static final double INSIDE_LANE_Y = Double.NaN;
    public static final double OUTSIDE_LANE_X = Double.NaN;
    public static final double OUTSIDE_LANE_Y = Double.NaN;

    public static final DcMotorSimple.Direction INTAKE_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction TRANSFER_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction OL_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction OR_DIRECTION = DcMotorSimple.Direction.FORWARD;

    public static final double JOYSTICK_DEADBAND = 0.05;

    // Starting values; tune them on the robot.
    public static final double AUTO_FORWARD_POWER = 0.20;
    public static final long AUTO_FORWARD_TIME_MS = 200;

    public static final double INTAKE_POWER = 1.0;
    public static final double INTAKE_SECONDS = 2.5;
    public static final double TRANSFER_POWER = 1.0;
    public static final double TRANSFER_SECONDS = 1.5;
    public static final double OL_POWER = 1.0;
    public static final double OR_POWER = 1.0;
    public static final double SHOOT_SECONDS = 1.0;

    // Starting field estimates. Check the Hive and Flower positions against the current CAD.
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
