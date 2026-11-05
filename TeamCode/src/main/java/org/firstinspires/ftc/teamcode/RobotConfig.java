package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotorSimple;

public final class RobotConfig {
    private RobotConfig() {}

    public static final String INTAKE = "intake";
    public static final String TRANSFER = "";
    public static final String OL = "ol";
    public static final String OR = "or";
    public static final String CAM1 = "Webcam 2";

    public static final DcMotorSimple.Direction INTAKE_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction TRANSFER_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction OL_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction OR_DIRECTION = DcMotorSimple.Direction.FORWARD;

    public static final double JOYSTICK_DEADBAND = 0.05;
    public static final double MOTOR_TEST_MAX_POWER = 0.20;

    public static final double AUTO_FORWARD_POWER = 0.20;
    public static final long AUTO_FORWARD_TIME_MS = 200;

    public static final double INTAKE_POWER = 1.0;
    public static final double INTAKE_SECONDS = 2.5;
    public static final double TRANSFER_POWER = 1.0;
    public static final double TRANSFER_SECONDS = 1.5;
    public static final double OL_POWER = 1.0;
    public static final double OR_POWER = 1.0;
    public static final double SHOOT_SECONDS = 1.0;

    public static final double PATH_PAUSE_SECONDS = 1.0;
    public static final double PATH_TIMEOUT_SECONDS = 20.0;

    public static boolean transferEnabled() {
        return TRANSFER != null && !TRANSFER.trim().isEmpty();
    }

    public static double driveAxis(double value) {
        return Math.abs(value) < JOYSTICK_DEADBAND ? 0.0 : value;
    }
}
