package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

import java.util.HashMap;
import java.util.Map;

/**
 * The one place that knows what every AprilTag ID on the field means: is
 * it a hive tag (and which alliance's hive), a fixed navigation tag usable
 * to correct localization, or irrelevant. Per the project brief, "the
 * robot should not simply say 'AprilTag detected' - it should determine
 * what the tag means" - this registry is how it does that, in one place
 * instead of scattered ID comparisons.
 *
 * TODO: VERIFY every tag ID and field pose below against the official
 * BIOBUZZ field/tag appendix once fully published, and against the actual
 * printed tags used at your events (numbering can vary by field set).
 * Field poses are in the same inches/heading-radians convention as
 * everywhere else in this project (see Points.FIELD_SIZE).
 */
public final class FieldTagLibrary {

    private FieldTagLibrary() {}

    private static final class Entry {
        final AprilTagRole role;
        final HiveSide hiveSide; // null unless role == HIVE_TAG
        final Pose fieldPose;    // known/expected field pose, or null if not pre-mapped

        Entry(AprilTagRole role, HiveSide hiveSide, Pose fieldPose) {
            this.role = role;
            this.hiveSide = hiveSide;
            this.fieldPose = fieldPose;
        }
    }

    private static final Map<Integer, Entry> TAGS = new HashMap<>();

    static {
        // Hive tags: mounted on each alliance's hive, facing outward along
        // the direction a scored ball would travel (heading = outward
        // scoring normal). Placeholder poses near each hive's expected
        // field location - TODO: VERIFY exact mounting position/height/angle.
        register(20, AprilTagRole.HIVE_TAG, HiveSide.RED, new Pose(132, 12, Math.toRadians(180)));
        register(21, AprilTagRole.HIVE_TAG, HiveSide.BLUE, new Pose(12, 132, Math.toRadians(90)));

        // Fixed navigation tags usable for localization correction.
        // TODO: VERIFY IDs/poses against the field's official tag map.
        register(1, AprilTagRole.NAVIGATION_TAG, null, new Pose(0, 72, Math.toRadians(90)));
        register(2, AprilTagRole.NAVIGATION_TAG, null, new Pose(144, 72, Math.toRadians(-90)));
    }

    private static void register(int id, AprilTagRole role, HiveSide side, Pose pose) {
        TAGS.put(id, new Entry(role, side, pose));
    }

    public static AprilTagRole roleOf(int tagId) {
        Entry e = TAGS.get(tagId);
        return e == null ? AprilTagRole.IRRELEVANT : e.role;
    }

    /** Null if this tag isn't a hive tag. */
    public static HiveSide hiveSideOf(int tagId) {
        Entry e = TAGS.get(tagId);
        return e == null ? null : e.hiveSide;
    }

    /** Null if this tag has no pre-mapped field position (e.g. unknown tag,
     *  or a hive tag we intentionally only trust live sightings for). */
    public static Pose knownFieldPoseOf(int tagId) {
        Entry e = TAGS.get(tagId);
        return e == null ? null : e.fieldPose;
    }

    public static int redHiveTagId() {
        return 20;
    }

    public static int blueHiveTagId() {
        return 21;
    }
}
