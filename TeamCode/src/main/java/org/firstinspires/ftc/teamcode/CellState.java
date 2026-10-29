package org.firstinspires.ftc.teamcode;

/** How reliable the robot considers a Cell's recorded field position. */
public enum CellState {
    /** No tag sighting; use the configured estimate. */
    UNKNOWN,

    /** A tag for this Cell is currently visible. */
    DETECTED,

    /** Seen recently enough that its last position is still trusted. */
    CONFIRMED,

    /** Seen before, but the position may be too old for accurate aiming. */
    STALE
}
