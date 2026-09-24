package org.firstinspires.ftc.teamcode;

/**
 * Which alliance a {@link Hive} belongs to. There are two hives on the
 * field (one per alliance).
 *
 * NOTE ON RENAMING: an earlier version of this file used HiveSide to mean
 * CLOSE/FAR - which of two tag-marked positions a "tipper" mechanism was
 * currently tipped to. That concept, and the word "tipper," has been
 * removed entirely per the project's terminology requirements. HiveSide
 * now means the ordinary, load-bearing thing a name like that should mean:
 * which alliance's hive this is. If the real BIOBUZZ hive structure turns
 * out to physically reorient/tip during a match, model that as a field on
 * {@link Hive} (e.g. an orientation angle or a HiveState transition) -
 * TODO: VERIFY against the official BIOBUZZ manual once fully published
 * and reconcile with whatever the real mechanism turns out to be.
 */
public enum HiveSide {
    RED,
    BLUE;

    public static HiveSide ofAlliance(boolean redAlliance) {
        return redAlliance ? RED : BLUE;
    }

    public HiveSide opposite() {
        return this == RED ? BLUE : RED;
    }
}
