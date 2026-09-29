package org.firstinspires.ftc.teamcode;

/**
 * Physical robot dimensions, in one place, so the autonomous planner can
 * reason about clearance/approach distance without every file inventing
 * its own guess. Nothing here should be duplicated elsewhere - if a
 * distance constant is about the robot's physical size, it belongs here.
 *
 * All values are placeholders until measured on the real robot. They are
 * deliberately conservative (assume a slightly bigger robot than likely)
 * so that early testing errs toward extra clearance rather than
 * collisions.
 *
 * TODO: VERIFY WITH ACTUAL ROBOT - measure with a tape measure once chassis
 * is final, including bumpers/perimeter if the robot uses them.
 */
public final class RobotGeometry {

    private RobotGeometry() {}

    /** Full width, left-to-right, including bumpers. Inches. */
    public static final double WIDTH_INCHES = 18.0;

    /** Full length, front-to-back, including bumpers. Inches. */
    public static final double LENGTH_INCHES = 18.0;

    public static final double HALF_WIDTH_INCHES = WIDTH_INCHES / 2.0;
    public static final double HALF_LENGTH_INCHES = LENGTH_INCHES / 2.0;

    /**
     * How far the intake mechanism sticks out past the front bumper when
     * deployed. Used to figure out how close the robot's *center* needs to
     * get to a ball for the intake to actually reach it, and how much
     * extra clearance a path needs in front of the robot.
     * TODO: VERIFY once intake is built - measure fully extended.
     */
    public static final double INTAKE_EXTENSION_INCHES = 6.0;

    /** Extra chassis overhang beyond the drivetrain footprint, front. */
    public static final double FRONT_OVERHANG_INCHES = 1.0;

    /** Extra chassis overhang beyond the drivetrain footprint, rear. */
    public static final double REAR_OVERHANG_INCHES = 1.0;

    /**
     * Effective forward reach: how far in front of the robot's tracked
     * center point a ball needs to be for intake to reach it. Used by the
     * planner/controller to decide "close enough to stop driving and
     * intake" and to size the standoff distance used when approaching a
     * ball.
     */
    public static final double INTAKE_REACH_INCHES = HALF_LENGTH_INCHES + FRONT_OVERHANG_INCHES + INTAKE_EXTENSION_INCHES;

    /**
     * Minimum gap the path planner tries to keep between the outer edge of
     * the robot and any known opponent-ball position or field obstacle.
     * This is a safety margin on top of the robot's own half-width, not a
     * replacement for it - see {@link #totalHazardRadiusInches()}.
     */
    public static final double MIN_OBSTACLE_CLEARANCE_INCHES = 4.0;

    /**
     * The radius (from robot center) that should be treated as "do not let
     * any hazard get closer than this," combining the robot's own
     * footprint with the desired safety margin. This is what
     * AutonomousPlanner actually uses for opponent-ball avoidance checks.
     */
    public static double totalHazardRadiusInches() {
        return Math.max(HALF_WIDTH_INCHES, HALF_LENGTH_INCHES) + MIN_OBSTACLE_CLEARANCE_INCHES;
    }

    /** Standard game ball diameter (Pollen/Nectar), for reference. Inches. */
    public static final double BALL_DIAMETER_INCHES = 3.0;

    // ---- Cell camera mounting (upward-facing AprilTag camera) -----------
    //
    // The FTC SDK's AprilTagProcessor.setCameraPose(position, orientation)
    // needs to know exactly where this camera sits relative to the robot's
    // tracked center point in order to compute detection.robotPose (the
    // SDK's own directly-computed field-relative robot pose - see
    // CellVision). Get this right and the SDK does the "where is the robot"
    // trigonometry itself, more robustly (full 3D) than this project's own
    // 2D estimateFieldPose() fallback math.
    //
    // Per the FTC SDK's own camera-pose convention (see
    // ConceptAprilTagLocalization.java's docstring): position is (x=right+,
    // y=forward+, z=up+) from robot center, and - importantly - ZERO
    // rotation (yaw=pitch=roll=0) means "camera pointing straight up,"
    // which is exactly what this camera actually does. That is why all
    // three angles below are 0 by default, not a placeholder guess: a
    // level, straight-up-facing mount needs no rotation at all. You only
    // need to change yaw/pitch/roll from 0 if the real camera is tilted
    // off vertical or rotated away from facing straight along the robot's
    // forward axis.
    //
    // TODO: VERIFY the position numbers (and the "perfectly vertical, no
    // tilt" assumption) by actually measuring the real camera mount once
    // built - a wrong number here doesn't cause a compile or runtime
    // error, it just makes detection.robotPose subtly wrong in a way
    // that's easy to miss until localization looks "close but not quite
    // right."
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
