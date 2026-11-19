package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/**
 * Hand-written BIOBUZZ route presets.
 *
 * Coordinate system:
 *   (0, 0) = audience-left inside corner.
 *   +X = toward the Blue wall.
 *   +Y = toward the Scoring/back wall.
 *   Heading 0 = +X, 90 = +Y, 180 = -X, 270 = -Y.
 *
 * Field references are the 144 inch / 6 tile / 24 inch grid.
 * Every route below is a literal list of poses. No mirrored coordinates,
 * field calculations, route permutations, or generated waypoints.
 */
public final class FieldPoses {
    private FieldPoses() {}

    public static final String[] START_NAMES = {
            "ALLIANCE WALL",
            "AUDIENCE WALL",
            "SCORING WALL"
    };

    public static final String[] FLOWER_NAMES = {
            "ALLIANCE-SIDE FLOWER",
            "FAR WALL FLOWER (SCORING)",
            "CLOSE WALL FLOWER (AUDIENCE)"
    };

    // Exactly three starts for Red and three for Blue: six starts total.
    // The 18 x 18 robot touches the chosen wall.
    public static final Pose[] RED_STARTS = {
            new Pose(9, 72, 0),
            new Pose(36, 9, Math.PI / 2),
            new Pose(36, 135, 3 * Math.PI / 2)
    };

    public static final Pose[] BLUE_STARTS = {
            new Pose(135, 72, Math.PI),
            new Pose(108, 9, Math.PI / 2),
            new Pose(108, 135, 3 * Math.PI / 2)
    };

    /*
     * Start-to-shoot routes. Each row matches START_NAMES:
     *   0 = alliance wall, 1 = audience wall, 2 = scoring wall.
     *
     * Inner lane: tile-column B/E center, 36 inches from the alliance wall.
     * Outer lane: tile-column A/F center, 12 inches from the alliance wall.
     *
     * The inner shot faces straight across the field from the middle.
     * The outer shot is angled 20 degrees toward the audience-side Hive cell.
     * Wall starts go directly to the selected lane. Audience/scoring starts
     * first move to the 24/120 inch tile seam, then turn into the lane.
     */
    public static final Pose[][] RED_INNER_START_TO_SHOOT = {
            { new Pose(36, 72, 0) },
            { new Pose(36, 24, Math.PI / 2), new Pose(36, 72, 0) },
            { new Pose(36, 120, 3 * Math.PI / 2), new Pose(36, 72, 0) }
    };

    public static final Pose[][] RED_OUTER_START_TO_SHOOT = {
            { new Pose(12, 72, Math.toRadians(340)) },
            { new Pose(36, 24, Math.PI / 2), new Pose(12, 24, 0),
                    new Pose(12, 72, Math.toRadians(340)) },
            { new Pose(36, 120, 3 * Math.PI / 2), new Pose(12, 120, Math.PI),
                    new Pose(12, 72, Math.toRadians(340)) }
    };

    public static final Pose[][] BLUE_INNER_START_TO_SHOOT = {
            { new Pose(108, 72, Math.PI) },
            { new Pose(108, 24, Math.PI / 2), new Pose(108, 72, Math.PI) },
            { new Pose(108, 120, 3 * Math.PI / 2), new Pose(108, 72, Math.PI) }
    };

    public static final Pose[][] BLUE_OUTER_START_TO_SHOOT = {
            { new Pose(132, 72, Math.toRadians(200)) },
            { new Pose(108, 24, Math.PI / 2), new Pose(132, 24, Math.PI),
                    new Pose(132, 72, Math.toRadians(200)) },
            { new Pose(108, 120, 3 * Math.PI / 2), new Pose(132, 120, 0),
                    new Pose(132, 72, Math.toRadians(200)) }
    };

    /*
     * Flower approach routes, in FLOWER_NAMES order.
     *
     * Alliance-side Flower:
     *   Red wall Flower is at y=48; Blue wall Flower is at y=96.
     * Far wall = Scoring wall, reached along y=120.
     * Close wall = Audience wall, reached along y=24.
     *
     * The robot stops 19 inches from the wall so its front can reach the
     * Flower's lower retrieval opening.
     */
    public static final Pose[][] RED_INNER_FLOWER_PATHS = {
            { new Pose(36, 48, 3 * Math.PI / 2), new Pose(19, 48, Math.PI) },
            { new Pose(36, 120, Math.PI / 2), new Pose(48, 120, 0),
                    new Pose(48, 125, Math.PI / 2) },
            { new Pose(36, 24, 3 * Math.PI / 2), new Pose(96, 24, 0),
                    new Pose(96, 19, 3 * Math.PI / 2) }
    };

    public static final Pose[][] RED_OUTER_FLOWER_PATHS = {
            { new Pose(12, 48, 3 * Math.PI / 2), new Pose(19, 48, Math.PI) },
            { new Pose(12, 120, Math.PI / 2), new Pose(48, 120, 0),
                    new Pose(48, 125, Math.PI / 2) },
            { new Pose(12, 24, 3 * Math.PI / 2), new Pose(96, 24, 0),
                    new Pose(96, 19, 3 * Math.PI / 2) }
    };

    public static final Pose[][] BLUE_INNER_FLOWER_PATHS = {
            { new Pose(108, 96, Math.PI / 2), new Pose(125, 96, 0) },
            { new Pose(108, 120, Math.PI / 2), new Pose(48, 120, Math.PI),
                    new Pose(48, 125, Math.PI / 2) },
            { new Pose(108, 24, 3 * Math.PI / 2), new Pose(96, 24, Math.PI),
                    new Pose(96, 19, 3 * Math.PI / 2) }
    };

    public static final Pose[][] BLUE_OUTER_FLOWER_PATHS = {
            { new Pose(132, 96, Math.PI / 2), new Pose(125, 96, 0) },
            { new Pose(132, 120, Math.PI / 2), new Pose(48, 120, Math.PI),
                    new Pose(48, 125, Math.PI / 2) },
            { new Pose(132, 24, 3 * Math.PI / 2), new Pose(96, 24, Math.PI),
                    new Pose(96, 19, 3 * Math.PI / 2) }
    };

    // Alliance Loading Zone park poses from the official field layout.
    public static final Pose RED_PARK = new Pose(9, 108, 0);
    public static final Pose BLUE_PARK = new Pose(135, 36, Math.PI);
}
