package org.firstinspires.ftc.teamcode;

/** Where a game piece is in its tracking lifecycle. Ownership is tracked separately in {@link BallOwner}. */
public enum BallState {
    /** Visible in the current camera frame. */
    VISIBLE,

    /** Seen recently and still worth considering as a target. */
    REMEMBERED,

    /** The robot assumes it picked this piece up. */
    COLLECTED,

    /** The piece was not at the expected location or could not be collected. */
    MISSING,

    /** Duplicate or outdated detection that should no longer be considered. */
    INVALID
}
