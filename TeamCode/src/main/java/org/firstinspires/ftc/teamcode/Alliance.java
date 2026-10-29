package org.firstinspires.ftc.teamcode;

/** The alliance that owns a Cell or colored Nectar. */
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
