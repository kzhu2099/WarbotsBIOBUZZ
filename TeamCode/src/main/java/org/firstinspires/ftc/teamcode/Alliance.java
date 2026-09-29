package org.firstinspires.ftc.teamcode;

/**
 * Which alliance owns something (a Cell, a Ball's Nectar, a starting
 * position). Pulled out as its own plain concept - not named after Cell or
 * any other structure - since alliance color is a property lots of
 * different things need to carry independently.
 *
 * (Named HiveSide in an earlier pass of this project, back when "Hive" was
 * assumed to be the primary scoring-structure concept. Per the official
 * BIOBUZZ glossary: CELL is "a three-dimensional structure that can hold
 * NECTAR and POLLEN" - the thing the robot actually scores into and reads
 * AprilTags off of. HIVE is a different, more specific thing - "a
 * bi-stable structure made up of two CELLS and a connecting assembly that
 * rotates on a pivot" (see TIP/TIPPED in the glossary) - which is not
 * currently modeled by this project; see Cell's Javadoc for what a future
 * Hive-pivot/tipping feature would need to add.)
 */
public enum Alliance {
    RED,
    BLUE;

    public static Alliance of(boolean redAlliance) {
        return redAlliance ? RED : BLUE;
    }

    public Alliance opposite() {
        return this == RED ? BLUE : RED;
    }
}
