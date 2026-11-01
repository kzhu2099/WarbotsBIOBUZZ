package org.firstinspires.ftc.teamcode;

import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

public final class RobotConfig {
    private RobotConfig() {}

    /*
     * Hardware used by this code:
     * 4 drive motors + 4 mechanism motors.
     * 0 servos. 1 Pinpoint (Pedro TeleOp only). 1 webcam (camera test only).
     *
     * These are the names from the team's existing code, not a live read
     * of the Control Hub configuration. Check the names before running.
     */
    public static final String FRONT_LEFT = "fl";
    public static final String FRONT_RIGHT = "fr";
    public static final String BACK_LEFT = "bl";
    public static final String BACK_RIGHT = "br";

    public static final String INTAKE = "intake";
    public static final String TRANSFER = "transfer";
    public static final String OUTTAKE_LEFT = "outtakeLeft";
    public static final String OUTTAKE_RIGHT = "outtakeRight";

    public static final String ODOMETRY = "odom";
    public static final String CAMERA = "Webcam 2";

    // Check wheel direction with the robot raised before driving.
    public static final DcMotorSimple.Direction FRONT_LEFT_DIRECTION = DcMotorSimple.Direction.REVERSE;
    public static final DcMotorSimple.Direction FRONT_RIGHT_DIRECTION = DcMotorSimple.Direction.REVERSE;
    public static final DcMotorSimple.Direction BACK_LEFT_DIRECTION = DcMotorSimple.Direction.REVERSE;
    public static final DcMotorSimple.Direction BACK_RIGHT_DIRECTION = DcMotorSimple.Direction.FORWARD;

    public static final DcMotorSimple.Direction INTAKE_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction TRANSFER_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction OUTTAKE_LEFT_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction OUTTAKE_RIGHT_DIRECTION = DcMotorSimple.Direction.FORWARD;

    // Ignore small joystick drift while keeping full stick range.
    public static final double JOYSTICK_DEADBAND = 0.05;

    public static double driveAxis(double value) {
        return Math.abs(value) < JOYSTICK_DEADBAND ? 0.0 : value;
    }

    // Forward-only test: one power, one duration, then stop.
    public static final double AUTO_FORWARD_POWER = 0.20;
    public static final long AUTO_FORWARD_TIME_MS = 200;

    // Timed mechanism presets. Tune these on the physical robot.
    public static final double INTAKE_POWER = 1.0;
    public static final double INTAKE_SECONDS = 2.5;
    public static final double TRANSFER_POWER = 1.0;
    public static final double TRANSFER_SECONDS = 1.5;
    public static final double OUTTAKE_LEFT_POWER = 1.0;
    public static final double OUTTAKE_RIGHT_POWER = 1.0;
    public static final double SHOOT_SECONDS = 1.0;

    // BIOBUZZ Manual TU07: §10.5.4 p.88 and Table 10-2 p.89.
    // LEAVE = 3 AUTO points. PARK = 5 points in AUTO and TELEOP.

    // Pinpoint values are inherited starting values. Measure before using pose data.
    public static final double PINPOINT_Y_POD_OFFSET_INCHES = 3.75;
    public static final double PINPOINT_X_POD_OFFSET_INCHES = 0.20;
    public static final GoBildaPinpointDriver.EncoderDirection PINPOINT_Y_DIRECTION =
            GoBildaPinpointDriver.EncoderDirection.REVERSED;
    public static final GoBildaPinpointDriver.EncoderDirection PINPOINT_X_DIRECTION =
            GoBildaPinpointDriver.EncoderDirection.REVERSED;

    public static MecanumConfig mecanumConfig() {
        return new MecanumConfig(c -> {
            c.frontLeftName.set(FRONT_LEFT);
            c.frontRightName.set(FRONT_RIGHT);
            c.backLeftName.set(BACK_LEFT);
            c.backRightName.set(BACK_RIGHT);
            c.frontLeftDirection.set(FRONT_LEFT_DIRECTION);
            c.frontRightDirection.set(FRONT_RIGHT_DIRECTION);
            c.backLeftDirection.set(BACK_LEFT_DIRECTION);
            c.backRightDirection.set(BACK_RIGHT_DIRECTION);
            c.manualBrakeMode.set(true);
        });
    }

    public static PinpointConfig pinpointConfig() {
        return new PinpointConfig(c -> {
            c.name.set(ODOMETRY);
            c.yPodOffset.set(PINPOINT_Y_POD_OFFSET_INCHES);
            c.xPodOffset.set(PINPOINT_X_POD_OFFSET_INCHES);
            c.yPodDirection.set(PINPOINT_Y_DIRECTION);
            c.xPodDirection.set(PINPOINT_X_DIRECTION);
        });
    }
}
