package org.firstinspires.ftc.teamcode;

/**
 * Lifecycle status of a ball tracked in {@link BallMap}.
 *
 *   VISIBLE    - Currently in a camera frame this cycle.
 *   REMEMBERED - Not currently visible, but recently seen; still a valid
 *                candidate target (within its confidence/uncertainty).
 *   COLLECTED  - We drove here and a successful intake was verified. No
 *                longer a target.
 *   MISSING    - We drove to where this ball should be (or expected to
 *                still see it) and it was not there / not collectible.
 *                Removed from the active target set; triggers a replan.
 *   INVALID    - Failed reconciliation (e.g. a duplicate created from
 *                sensor noise, or aged out past the forget window).
 *                Kept only long enough for telemetry, then dropped.
 *
 * Design note: the original planning document also listed OPPONENT and
 * UNKNOWN as possible statuses. Those are represented instead by
 * {@link BallOwner} on this same object, rather than duplicated here as
 * additional lifecycle states - a ball does not stop being "remembered"
 * because it turned out to be the opponent's; it is simply a REMEMBERED
 * ball with owner == OPPONENT. Keeping ownership and lifecycle as two
 * independent fields instead of one combined enum avoids an explosion of
 * redundant states (VISIBLE_OPPONENT, REMEMBERED_UNKNOWN, ...) and matches
 * "avoid unnecessary abstraction" from the project brief.
 */
public enum BallState {
    VISIBLE,
    REMEMBERED,
    COLLECTED,
    MISSING,
    INVALID
}
