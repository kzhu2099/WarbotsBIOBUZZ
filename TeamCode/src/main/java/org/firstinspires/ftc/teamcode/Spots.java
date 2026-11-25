package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/**
 * Field positions, in inches, for RED. Blue uses the same spots rotated 180 degrees
 * about the field center (see forAlliance), because the BIOBUZZ field is rotated, not
 * mirrored. That rotation is also why Red's FRONT cell and Blue's BACK cell start up.
 *
 * Grid: 144 x 144 in, 6 x 6 tiles, 24 in per tile. Count tiles on the mat to place the robot.
 *   origin = Red wall / audience wall corner, +X toward Blue, +Y toward the scoring wall.
 *   Tile seams are at 0, 24, 48, 72, 96, 120, 144. Tile centers are at 12, 36, 60, 84, ...
 *   heading 0 = facing Blue, 90 = facing the scoring wall. Pose = robot center (18 x 18 in).
 *   A bumper flat on a wall puts the robot center 9 in from that wall.
 *
 * Red half, on the grid:
 *   Midline:  X = 72 (3 tiles). Never cross it.
 *   Hive:     angled supports on the X = 48 and X = 96 seams, 48 in of clear floor between them.
 *             Red cells sit over X = 60 (middle of tile column 3). The FRONT cell opens toward
 *             the audience (mouth at Y ~52), the BACK cell toward the scoring wall (Y ~92).
 *   Flowers:  Red wall at Y = 48 (2 tiles up), scoring wall at X = 48 (2 tiles over).
 *   Loading zone (PARK): on the Red wall, Y 96 to ~118 (tile row 5).
 *
 * INSIDE robot: tile column 3 (X = 60), under the hive. Straight shots only.
 * OUTSIDE robot: tile columns 1-2, between the Red wall and the hive support. Angled shots.
 * They never share floor: inside stays at X >= 51 below Y = 108; outside stays at X <= 43
 * and Y <= 105.
 */
public final class Spots {
    private Spots() {}

    public static final double TILE = 24;
    public static final double FIELD = 6 * TILE;   // 144

    // Middle of our 24 in half of the 48 in gap under the hive: 3 in clear of the support
    // (X = 48) and 3 in clear of the midline (X = 72).
    public static final double INSIDE_LANE_X = 60;  // 2.5 tiles

    static double deg(double degrees) {
        return Math.toRadians(degrees);
    }

    // ---------- INSIDE robot (tile column 3) ----------
    public static final Pose INSIDE_START = new Pose(INSIDE_LANE_X, 9, deg(90));  // back on audience wall, middle of column 3
    public static final Pose INSIDE_LANE_FRONT = new Pose(INSIDE_LANE_X, 24, deg(90));  // 1 tile up: front end of the lane
    public static final Pose INSIDE_LANE_BACK = new Pose(INSIDE_LANE_X, 108, deg(90));  // 4.5 tiles up: past the hive
    public static final Pose BACK_FLOWER_LINEUP = new Pose(48, 120, deg(90));            // 2 tiles over, 5 tiles up
    public static final Pose BACK_FLOWER = new Pose(48, 129, deg(90));                   // bumper 1 in off the flower
    public static final Pose INSIDE_PARK = new Pose(12, 120, deg(0));                    // top of the loading zone

    // ---------- OUTSIDE robot (tile columns 1-2) ----------
    public static final Pose OUTSIDE_START = new Pose(9, 24, deg(0));                    // back on Red wall, centered on the 1-tile seam
    public static final Pose WALL_FLOWER_LINEUP = new Pose(24, 48, deg(180));            // 1 tile over, 2 tiles up
    public static final Pose WALL_FLOWER = new Pose(15, 48, deg(180));                   // bumper 1 in off the flower
    public static final Pose OUTSIDE_PARK = new Pose(12, 96, deg(0));                    // bottom of the loading zone

    /** Where to stand, flywheel rpm, angle servo position. */
    public static final class Shot {
        public final String name;
        public final Pose pose;
        public final double rpm;
        public final double angle;

        public Shot(String name, Pose pose, double rpm, double angle) {
            this.name = name;
            this.pose = pose;
            this.rpm = rpm;
            this.angle = angle;
        }
    }

    // Straight into the FRONT cell's mouth (Y ~52), 28 in away.
    public static final Shot INSIDE_FRONT_SHOT =
            new Shot("INSIDE FRONT", INSIDE_LANE_FRONT, 3200, 0.50);
    // Straight into the BACK cell's mouth (Y ~92) from the scoring side, 28 in away.
    // X = 58, not 60: the robot turns around here, and 2 in left keeps its corners off the midline.
    public static final Shot INSIDE_BACK_SHOT =
            new Shot("INSIDE BACK", new Pose(58, 120, deg(-90)), 3200, 0.50);
    // Angled into the FRONT cell's mouth (60, 52) from tile column 2.
    public static final Shot OUTSIDE_FRONT_SHOT =
            new Shot("OUTSIDE FRONT", new Pose(30, 24, deg(43)), 3600, 0.50);

    /** Red spots are used as written; Blue spots are rotated 180 degrees about the center. */
    public static Pose forAlliance(Pose red, boolean isRed) {
        if (isRed) return red;
        return new Pose(FIELD - red.x(), FIELD - red.y(), red.heading() + Math.PI);
    }
}
