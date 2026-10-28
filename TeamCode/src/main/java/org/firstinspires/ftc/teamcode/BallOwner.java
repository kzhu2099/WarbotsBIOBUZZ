package org.firstinspires.ftc.teamcode;

/** Whether a detected game piece is ours, the opponent's, or not yet classified. */
public enum BallOwner {
    /** Pollen or Nectar belonging to our alliance. */
    OURS,

    /** Opponent Nectar; do not collect it. */
    OPPONENT,

    /** The color or ownership could not be determined reliably. */
    UNKNOWN
}
