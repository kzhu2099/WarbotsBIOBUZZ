package org.firstinspires.ftc.teamcode;

/**
 * BIOBUZZ nominal field layout.
 *
 * FIRST describes the field as approximately 144 x 144 inches with 36
 * nominally 24 x 24 inch tiles. The autonomous uses that tile grid for
 * repeatable alignment; it does not substitute a CAD wall-to-wall estimate.
 *
 * The HIVE frame is centered on the field. Its footprint is slanted/triangular,
 * so routes are deliberately kept in the tile-center lanes outside the frame
 * instead of trying to path through its rectangular bounding box.
 */
public final class FieldConfig {
    private FieldConfig() {}

    public static final double FIELD_SIZE_INCHES = 144.0;
    public static final double TILE_SIZE_INCHES = 24.0;
    public static final int TILES_PER_SIDE = 6;
    public static final double FIELD_CENTER_INCHES = 72.0;

    public static final double ROBOT_WIDTH_INCHES = 18.0;
    public static final double ROBOT_LENGTH_INCHES = 18.0;

    // Official nominal HIVE frame dimensions.
    public static final double HIVE_FRAME_WIDTH_INCHES = 49.46;
    public static final double HIVE_FRAME_DEPTH_INCHES = 38.95;

    // Half extents are useful for checking the two safe travel corridors.
    public static final double HIVE_HALF_WIDTH_INCHES = HIVE_FRAME_WIDTH_INCHES / 2.0;
    public static final double HIVE_HALF_DEPTH_INCHES = HIVE_FRAME_DEPTH_INCHES / 2.0;
}
