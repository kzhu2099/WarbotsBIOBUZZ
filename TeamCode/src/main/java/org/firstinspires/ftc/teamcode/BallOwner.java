package org.firstinspires.ftc.teamcode;

/**
 * Who a detected/remembered ball belongs to. This is the classification the
 * planner uses to decide "seek" vs. "avoid":
 *
 *   OURS      - Pollen (usable by anyone) or our-alliance-colored Nectar.
 *               Safe / desirable to collect.
 *   OPPONENT  - The other alliance's colored Nectar. Must be avoided, both
 *               as a target and as an obstacle in the path.
 *   UNKNOWN   - Detected but not classified with enough confidence to
 *               safely treat as OURS. Treated conservatively (never a
 *               collection target, but not necessarily a hard obstacle
 *               either - see AutonomousPlanner).
 */
public enum BallOwner {
    OURS,
    OPPONENT,
    UNKNOWN
}
