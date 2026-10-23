package org.firstinspires.ftc.teamcode;

/** Measured chassis dimensions and camera offsets, in inches and degrees. */
public final class RobotGeometry {

    private RobotGeometry() {}

    // PLACEHOLDERS ONLY: replace both with tape-measured dimensions, in inches.
    public static final double WIDTH_INCHES = 18.0;
    public static final double LENGTH_INCHES = 18.0;

    public static final double HALF_WIDTH_INCHES = WIDTH_INCHES / 2.0;
    public static final double HALF_LENGTH_INCHES = LENGTH_INCHES / 2.0;

    // Measure with the intake fully extended, from the front bumper.
    public static final double INTAKE_EXTENSION_INCHES = 6.0;

    /** Extra chassis overhang beyond the drivetrain footprint, front. */
    public static final double FRONT_OVERHANG_INCHES = 1.0;

    /** Extra chassis overhang beyond the drivetrain footprint, rear. */
    public static final double REAR_OVERHANG_INCHES = 1.0;

    // Distance from the robot pose center to the fully extended intake tip.
    public static final double INTAKE_REACH_INCHES = HALF_LENGTH_INCHES + FRONT_OVERHANG_INCHES + INTAKE_EXTENSION_INCHES;

    // Extra gap from the chassis footprint to a tracked obstacle.
    public static final double MIN_OBSTACLE_CLEARANCE_INCHES = 4.0;

    /** Robot half-dimension plus the obstacle clearance margin. */
    public static double totalHazardRadiusInches() {
        return Math.max(HALF_WIDTH_INCHES, HALF_LENGTH_INCHES) + MIN_OBSTACLE_CLEARANCE_INCHES;
    }

    /** Official BIOBUZZ nominal scoring-element diameters. VERIFY physical variation. */
    public static final double POLLEN_DIAMETER_INCHES = 2.8;
    public static final double NECTAR_DIAMETER_INCHES = 3.6;

    /** Legacy alias; use the pollen/nectar-specific constants for new code. */
    @Deprecated
    public static final double BALL_DIAMETER_INCHES = POLLEN_DIAMETER_INCHES;

    // Camera pose is measured from the robot's tracked center.
    // Axes: right, forward, up in inches; yaw, pitch, roll in degrees.
    // VERIFY the mount position and angles after installing the camera.

    /** Camera position forward(+)/back(-) of robot center, inches. */
    public static final double CELL_CAMERA_FORWARD_INCHES = 0.0;
    /** Camera position right(+)/left(-) of robot center, inches. */
    public static final double CELL_CAMERA_LATERAL_INCHES = 0.0;
    /** Camera height above the field floor, inches. */
    public static final double CELL_CAMERA_HEIGHT_INCHES = 12.0;
    /** Camera yaw, degrees - 0 = facing the same direction as robot forward. */
    public static final double CELL_CAMERA_YAW_DEGREES = 0.0;
    /** Camera pitch, degrees - 0 = perfectly vertical (straight up). */
    public static final double CELL_CAMERA_PITCH_DEGREES = 0.0;
    /** Camera roll, degrees - 0 = not rotated about its own lens axis. */
    public static final double CELL_CAMERA_ROLL_DEGREES = 0.0;
}
