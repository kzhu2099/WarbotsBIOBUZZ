package org.firstinspires.ftc.teamcode;

/**
 * How much we currently trust a {@link Hive}'s recorded field position.
 *
 *   UNKNOWN   - Never observed this match. fieldPose is only a rough,
 *               pre-configured guess (from Points/field constants).
 *   DETECTED  - Its AprilTag is in view right now (or was within the last
 *               fraction of a second). Highest confidence.
 *   CONFIRMED - Not currently visible, but was DETECTED recently enough
 *               that we still trust the recorded pose for approach/aim
 *               planning.
 *   STALE     - Was seen previously but not recently enough to trust for
 *               precision scoring; still better than UNKNOWN as a fallback,
 *               but the controller should treat aiming as lower-confidence
 *               and telemetry should say so.
 */
public enum HiveState {
    UNKNOWN,
    DETECTED,
    CONFIRMED,
    STALE
}
