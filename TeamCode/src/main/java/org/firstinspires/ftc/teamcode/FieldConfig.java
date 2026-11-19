package org.firstinspires.ftc.teamcode;

/**
 * Measured BIOBUZZ field and robot dimensions used by the drive-only path test.
 * Coordinates are measured from the inside wall faces.
 */
public final class FieldConfig {
    private FieldConfig() {}

    // FIRST's manual dimension is nominal. The inside-wall span below is measured
    // from FIRST's 2026-2027 STEP field CAD v26-27.2.
    public static final double NOMINAL_FIELD_SIZE_INCHES = 144.0;
    public static final double FIELD_SIZE_INCHES = 141.348;
    public static final double CENTER_INCHES = 70.674;

    // Assembled robot including bumpers.
    public static final double ROBOT_WIDTH_INCHES = 18.0;
    public static final double ROBOT_LENGTH_INCHES = 18.0;

    // CAD-derived ground footprint of the Hive frame and feet.
    public static final double HIVE_FRAME_MIN_X_INCHES = 45.939;
    public static final double HIVE_FRAME_MAX_X_INCHES = 95.409;
    public static final double HIVE_FRAME_MIN_Y_INCHES = 51.198;
    public static final double HIVE_FRAME_MAX_Y_INCHES = 90.150;

    // Additional gap outside the robot footprint around the Hive.
    public static final double HIVE_CLEARANCE_INCHES = 2.0;

    // Official nominal dimensions. 18.8 in is the gap between Cell inner faces,
    // not the distance between Cell centers.
    public static final double HIVE_CELL_INNER_GAP_INCHES = 18.8;
    public static final double HIVE_FRAME_WIDTH_X_INCHES = 49.46;
    public static final double HIVE_FRAME_DEPTH_Y_INCHES = 38.95;

    // CAD-derived Flower placement values.
    public static final double FLOWER_STATION_FROM_CENTER_INCHES = 23.392;
    public static final double FLOWER_CENTER_INSET_INCHES = 2.629;
    public static final double FLOWER_ROBOT_STOP_FROM_WALL_INCHES = 19.0;

    /*
     * Compatibility names for older code. New path code must use FieldPoses.java,
     * which contains four separate Flower positions and named robot-stop presets.
     */
    @Deprecated public static final double HIVE_CELL_SEPARATION_INCHES = HIVE_CELL_INNER_GAP_INCHES;
    @Deprecated public static final double START_WALL_CLEARANCE_INCHES = 0.0; // side bumper flush to wall
    @Deprecated public static final double HIVE_CELL_X_OFFSET_INCHES = 12.75; // Hive pivot offset from field center
    @Deprecated public static final double HIVE_APPROACH_INCHES = 26.924; // Cell center to Hive safe-stop center
    @Deprecated public static final double INSIDE_LANE_X_INCHES = 30.0; // Red-side centerline
    @Deprecated public static final double OUTSIDE_LANE_X_INCHES = 18.0; // Red-side centerline
    @Deprecated public static final double FLOWER_STANDOFF_INCHES = 19.0; // robot center from wall
    @Deprecated public static final double FLOWER_POSITION_INCHES = 23.392; // station from field center along wall
}
