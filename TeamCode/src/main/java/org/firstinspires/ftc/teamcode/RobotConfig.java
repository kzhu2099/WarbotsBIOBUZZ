package org.firstinspires.ftc.teamcode;

import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

public final class RobotConfig {
    private RobotConfig() {}

    // Match these names to the Driver Station Hardware page.
    public static final String FRONT_LEFT = "fl";
    public static final String FRONT_RIGHT = "fr";
    public static final String BACK_LEFT = "bl";
    public static final String BACK_RIGHT = "br";

    public static final String INTAKE = "intake";
    public static final String TRANSFER = ""; // Blank disables the optional transfer motor.
    public static final String OL = "ol";
    public static final String OR = "or";

    public static final String ODOMETRY = "odom";
    public static final String CAM1 = "Webcam 2"; // Code name cam1; Driver Station name stays Webcam 2.

    public static final DcMotorSimple.Direction FRONT_LEFT_DIRECTION = DcMotorSimple.Direction.REVERSE;
    public static final DcMotorSimple.Direction FRONT_RIGHT_DIRECTION = DcMotorSimple.Direction.REVERSE;
    public static final DcMotorSimple.Direction BACK_LEFT_DIRECTION = DcMotorSimple.Direction.REVERSE;
    public static final DcMotorSimple.Direction BACK_RIGHT_DIRECTION = DcMotorSimple.Direction.FORWARD;

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

    // Starting values carried over from the earlier Pinpoint setup. Measure and verify them.
    public static final double PINPOINT_Y_POD_OFFSET_INCHES = 3.75;
    public static final double PINPOINT_X_POD_OFFSET_INCHES = 0.20;
    public static final GoBildaPinpointDriver.EncoderDirection PINPOINT_Y_DIRECTION =
            GoBildaPinpointDriver.EncoderDirection.REVERSED;
    public static final GoBildaPinpointDriver.EncoderDirection PINPOINT_X_DIRECTION =
            GoBildaPinpointDriver.EncoderDirection.REVERSED;

    public static boolean transferEnabled() {
        return TRANSFER != null && !TRANSFER.trim().isEmpty();
    }

    public static double driveAxis(double value) {
        return Math.abs(value) < JOYSTICK_DEADBAND ? 0.0 : value;
    }

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
