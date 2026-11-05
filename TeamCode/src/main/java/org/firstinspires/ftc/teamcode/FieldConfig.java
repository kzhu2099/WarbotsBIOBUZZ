package org.firstinspires.ftc.teamcode;

public final class FieldConfig {
    private FieldConfig() {}

    public static final double FIELD_SIZE_INCHES = 144.0;
    public static final double CENTER_INCHES = FIELD_SIZE_INCHES / 2.0;

    public static final double HIVE_CELL_SEPARATION_INCHES = 18.8;
    public static final double HIVE_FRAME_WIDTH_INCHES = 49.46;
    public static final double HIVE_FRAME_DEPTH_INCHES = 38.95;
    public static final double HIVE_CLEARANCE_INCHES = 2.0;

    // Measure bumper-to-bumper dimensions, not just the frame.
    public static final double ROBOT_WIDTH_INCHES = Double.NaN;
    public static final double ROBOT_LENGTH_INCHES = Double.NaN;

    // Measure these from the current field CAD.
    public static final double CORNER_INSET_INCHES = Double.NaN;
    public static final double HIVE_CELL_X_OFFSET_INCHES = Double.NaN;
    public static final double HIVE_APPROACH_INCHES = Double.NaN;
    public static final double INSIDE_LANE_X_INCHES = Double.NaN;
    public static final double OUTSIDE_LANE_X_INCHES = Double.NaN;
    public static final double FLOWER_STANDOFF_INCHES = Double.NaN;

    // The four Flowers are centered on their respective field walls.
    public static final double FLOWER_POSITION_INCHES = CENTER_INCHES;
}
