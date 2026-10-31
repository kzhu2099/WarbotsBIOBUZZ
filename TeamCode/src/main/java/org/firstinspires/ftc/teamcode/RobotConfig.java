package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/** Hardware names and values that are expected to change during robot testing. */
public final class RobotConfig {
    private RobotConfig() {}

    public static final String FRONT_LEFT = "fl";
    public static final String FRONT_RIGHT = "fr";
    public static final String BACK_LEFT = "bl";
    public static final String BACK_RIGHT = "br";

    public static final String ODOMETRY = "odom";
    public static final String APRILTAG_CAMERA = "Webcam 2";

    public static final String INTAKE = "intake";
    public static final String TRANSFER = "transfer";
    public static final String OUTTAKE_LEFT = "outtakeLeft";
    public static final String OUTTAKE_RIGHT = "outtakeRight";

    // These are the directions already in the project. Verify them with the robot raised.
    public static final DcMotorSimple.Direction FRONT_LEFT_DIRECTION = DcMotorSimple.Direction.REVERSE;
    public static final DcMotorSimple.Direction FRONT_RIGHT_DIRECTION = DcMotorSimple.Direction.REVERSE;
    public static final DcMotorSimple.Direction BACK_LEFT_DIRECTION = DcMotorSimple.Direction.REVERSE;
    public static final DcMotorSimple.Direction BACK_RIGHT_DIRECTION = DcMotorSimple.Direction.FORWARD;

    public static final DcMotorSimple.Direction INTAKE_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction TRANSFER_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction OUTTAKE_LEFT_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction OUTTAKE_RIGHT_DIRECTION = DcMotorSimple.Direction.FORWARD;

    public static final double DRIVE_PRECISION_SCALE = 0.35;

    // Start low. Increase only after checking every wheel direction.
    public static final double AUTO_TEST_POWER = 0.20;
    public static final double AUTO_TEST_MOVE_SECONDS = 0.20;
    public static final double AUTO_TEST_PAUSE_SECONDS = 0.25;

    // Set these from a tape measure. Poses in this project are chassis-center poses.
    public static final double ROBOT_WIDTH_INCHES = Double.NaN;
    public static final double ROBOT_LENGTH_INCHES = Double.NaN;
    public static final double FIELD_SIZE_INCHES = 144.0;

    // Current estimates from the old project. Calibrate the Pinpoint before field-centric driving.
    public static final double PINPOINT_Y_POD_OFFSET_INCHES = 3.75;
    public static final double PINPOINT_X_POD_OFFSET_INCHES = 0.20;
    public static final GoBildaPinpointDriver.EncoderDirection PINPOINT_Y_DIRECTION =
            GoBildaPinpointDriver.EncoderDirection.REVERSED;
    public static final GoBildaPinpointDriver.EncoderDirection PINPOINT_X_DIRECTION =
            GoBildaPinpointDriver.EncoderDirection.REVERSED;

    // Mechanism powers and times. Tune one stage at a time on the real robot.
    public static final double INTAKE_POWER = 1.0;
    public static final double INTAKE_SECONDS = 2.5;
    public static final double TRANSFER_POWER = 1.0;
    public static final double TRANSFER_SECONDS = 1.5;
    public static final double OUTTAKE_LEFT_POWER = 1.0;
    public static final double OUTTAKE_RIGHT_POWER = 1.0;
    public static final double OUTTAKE_SECONDS = 1.0;
    public static final double CLEAR_SECONDS = 0.3;

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

    /** Returns the robot-center pose just inside a wall. Heading is in radians. */
    public static Pose robotCenterFromWall(
            double wallX, double wallY, double inwardHeading, double clearanceInches) {
        if (!Double.isFinite(ROBOT_WIDTH_INCHES) || !Double.isFinite(ROBOT_LENGTH_INCHES)) {
            throw new IllegalStateException("Measure ROBOT_WIDTH_INCHES and ROBOT_LENGTH_INCHES first.");
        }

        double halfExtent = Math.abs(Math.cos(inwardHeading)) * ROBOT_LENGTH_INCHES / 2.0
                + Math.abs(Math.sin(inwardHeading)) * ROBOT_WIDTH_INCHES / 2.0;
        double centerDistance = halfExtent + clearanceInches;

        return new Pose(
                wallX + Math.cos(inwardHeading) * centerDistance,
                wallY + Math.sin(inwardHeading) * centerDistance,
                inwardHeading
        );
    }
}
