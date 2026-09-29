package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

import java.util.HashMap;
import java.util.Map;

/**
 * The one place that knows what every AprilTag ID on the field means: is
 * it a Cell tag (and which alliance's cell, and which side of the field),
 * or irrelevant. Per the project brief, "the robot should not simply say
 * 'AprilTag detected' - it should determine what the tag means" - this
 * registry is how it does that, in one place instead of scattered ID
 * comparisons.
 *
 * TERMINOLOGY (per the official BIOBUZZ glossary): CELL is "a
 * three-dimensional structure that can hold NECTAR and POLLEN" - this is
 * what these AprilTags are mounted on, and what this project scores into.
 * HIVE is a distinct, more specific concept - "a bi-stable structure made
 * up of two CELLS and a connecting assembly that rotates on a pivot" - not
 * currently modeled by this project (see Cell's Javadoc). An earlier pass
 * of this project used "Hive" for what should have been called "Cell";
 * that has been corrected throughout.
 *
 * REAL, CONFIRMED tag layout (not a guess):
 *   - IDs 30, 31, 32, 33: the RED cell on the side of the field OPPOSITE
 *     the audience.
 *   - IDs 34, 35, 36, 37: the RED cell on the AUDIENCE side.
 *   - IDs 38, 39, 40, 41: the BLUE cell on the AUDIENCE side.
 *   - IDs 42, 43, 44, 45: the BLUE cell on the side of the field OPPOSITE
 *     the audience.
 *
 * So there are FOUR physical Cells on the field, not two - each alliance
 * has one nearer the audience and one on the far side - and each one
 * carries FOUR AprilTags (almost certainly one per face, so it can be
 * identified/localized from whichever direction a robot approaches from).
 * All four IDs in a group are treated as equivalent identifiers for the
 * SAME Cell object below: whichever one is currently visible updates that
 * same Cell's position estimate (see CellMap).
 *
 * No separate navigation-only tags have been identified for this field -
 * if BIOBUZZ turns out to have dedicated localization tags elsewhere,
 * register them here with AprilTagRole.NAVIGATION_TAG and they'll flow
 * through Localization automatically. Until then, NAVIGATION_TAG is wired
 * up and ready, but empty.
 *
 * TODO: VERIFY the exact field pose (position + mounting height/angle) of
 * each tag against the official BIOBUZZ field/tag appendix and the actual
 * printed tags at your events - the IDs/groupings above are confirmed, but
 * exact coordinates below are still a placeholder layout on a generic
 * 144in x 144in field (audience along the y=0 wall, red alliance nearer
 * x=0, blue alliance nearer x=144 - adjust to match the real field).
 */
public final class FieldTagLibrary {

    private FieldTagLibrary() {}

    private static final class Entry {
        final AprilTagRole role;
        final Alliance alliance;  // null unless role == CELL_TAG
        final FieldSide fieldSide; // null unless role == CELL_TAG
        final Pose fieldPose;      // known/expected field pose, or null if unmapped

        Entry(AprilTagRole role, Alliance alliance, FieldSide fieldSide, Pose fieldPose) {
            this.role = role;
            this.alliance = alliance;
            this.fieldSide = fieldSide;
            this.fieldPose = fieldPose;
        }
    }

    private static final Map<Integer, Entry> TAGS = new HashMap<>();

    // Placeholder cell centers/headings - TODO: VERIFY against the real
    // field. Heading = the outward direction a scored ball travels (see
    // Cell's Javadoc), which for a wall-mounted cell is "away from that
    // wall, into the field."
    private static final Pose RED_AUDIENCE_POSE = new Pose(36, 6, Math.toRadians(90));
    private static final Pose RED_OPPOSITE_POSE = new Pose(36, 138, Math.toRadians(-90));
    private static final Pose BLUE_AUDIENCE_POSE = new Pose(108, 6, Math.toRadians(90));
    private static final Pose BLUE_OPPOSITE_POSE = new Pose(108, 138, Math.toRadians(-90));

    static {
        registerGroup(new int[]{30, 31, 32, 33}, Alliance.RED, FieldSide.OPPOSITE, RED_OPPOSITE_POSE);
        registerGroup(new int[]{34, 35, 36, 37}, Alliance.RED, FieldSide.AUDIENCE, RED_AUDIENCE_POSE);
        registerGroup(new int[]{38, 39, 40, 41}, Alliance.BLUE, FieldSide.AUDIENCE, BLUE_AUDIENCE_POSE);
        registerGroup(new int[]{42, 43, 44, 45}, Alliance.BLUE, FieldSide.OPPOSITE, BLUE_OPPOSITE_POSE);
    }

    private static void registerGroup(int[] ids, Alliance alliance, FieldSide fieldSide, Pose pose) {
        for (int id : ids) {
            TAGS.put(id, new Entry(AprilTagRole.CELL_TAG, alliance, fieldSide, pose));
        }
    }

    public static AprilTagRole roleOf(int tagId) {
        Entry e = TAGS.get(tagId);
        return e == null ? AprilTagRole.IRRELEVANT : e.role;
    }

    /** Null if this tag isn't a Cell tag. */
    public static Alliance allianceOf(int tagId) {
        Entry e = TAGS.get(tagId);
        return e == null ? null : e.alliance;
    }

    /** Null if this tag isn't a Cell tag. */
    public static FieldSide fieldSideOf(int tagId) {
        Entry e = TAGS.get(tagId);
        return e == null ? null : e.fieldSide;
    }

    /** Null if this tag has no pre-mapped field position. */
    public static Pose knownFieldPoseOf(int tagId) {
        Entry e = TAGS.get(tagId);
        return e == null ? null : e.fieldPose;
    }

    /** Every tag ID that identifies the given (alliance, field side) cell. */
    public static int[] tagIdsFor(Alliance alliance, FieldSide fieldSide) {
        if (alliance == Alliance.RED && fieldSide == FieldSide.OPPOSITE) return new int[]{30, 31, 32, 33};
        if (alliance == Alliance.RED && fieldSide == FieldSide.AUDIENCE) return new int[]{34, 35, 36, 37};
        if (alliance == Alliance.BLUE && fieldSide == FieldSide.AUDIENCE) return new int[]{38, 39, 40, 41};
        return new int[]{42, 43, 44, 45}; // BLUE, OPPOSITE
    }

    /** The first (lowest) tag ID for a given cell - handy as a stable,
     *  single representative ID for logging/telemetry. */
    public static int primaryTagIdFor(Alliance alliance, FieldSide fieldSide) {
        return tagIdsFor(alliance, fieldSide)[0];
    }

    public static Pose nominalPoseFor(Alliance alliance, FieldSide fieldSide) {
        if (alliance == Alliance.RED && fieldSide == FieldSide.OPPOSITE) return RED_OPPOSITE_POSE;
        if (alliance == Alliance.RED && fieldSide == FieldSide.AUDIENCE) return RED_AUDIENCE_POSE;
        if (alliance == Alliance.BLUE && fieldSide == FieldSide.AUDIENCE) return BLUE_AUDIENCE_POSE;
        return BLUE_OPPOSITE_POSE; // BLUE, OPPOSITE
    }
}
