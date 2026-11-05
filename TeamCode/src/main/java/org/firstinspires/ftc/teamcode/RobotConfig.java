package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotorSimple;

public final class RobotConfig {
    private RobotConfig() {}

    public static final String INTAKE_MOTOR_NAME = "intake";
    public static final String TRANSFER_MOTOR_NAME = "";
    public static final String LEFT_OUTTAKE_MOTOR_NAME = "ol";
    public static final String RIGHT_OUTTAKE_MOTOR_NAME = "or";
    public static final String WEBCAM_NAME = "Webcam 2";

    public static final DcMotorSimple.Direction INTAKE_MOTOR_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction TRANSFER_MOTOR_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction LEFT_OUTTAKE_MOTOR_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction RIGHT_OUTTAKE_MOTOR_DIRECTION = DcMotorSimple.Direction.FORWARD;

    public static final double JOYSTICK_DEADBAND = 0.05;
    public static final double MOTOR_TEST_MAX_POWER = 0.20;

    public static final double AUTO_FORWARD_POWER = 0.20;
    public static final long AUTO_FORWARD_TIME_MS = 200;

    public static final double INTAKE_POWER = 1.0;
    public static final double INTAKE_SECONDS = 2.5;
    public static final double TRANSFER_POWER = 1.0;
    public static final double TRANSFER_SECONDS = 1.5;
    public static final double LEFT_OUTTAKE_POWER = 1.0;
    public static final double RIGHT_OUTTAKE_POWER = 1.0;
    public static final double SHOOT_SECONDS = 1.0;

    public static final double PATH_PAUSE_SECONDS = 1.0;
    public static final double PATH_TIMEOUT_SECONDS = 20.0;

    public static boolean transferEnabled() {
        return TRANSFER_MOTOR_NAME != null && !TRANSFER_MOTOR_NAME.trim().isEmpty();
    }

    public static double driveAxis(double value) {
        return Math.abs(value) < JOYSTICK_DEADBAND ? 0.0 : value;
    }
}
