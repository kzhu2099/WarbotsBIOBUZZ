package org.firstinspires.ftc.teamcode;

/**
 * States for {@link AutonomousController}. This replaces the previous
 * (nonfunctional) approach of one giant if/else chain, or a fixed Step
 * sequence pretending to be dynamic.
 *
 * A few states from the original design sketch have been intentionally
 * merged where splitting them would not have added real behavior:
 *   - SELECT_TARGETS + PLAN_COLLECTION_ROUTE + SELECT_NEXT_TARGET -> PLAN
 *     (all three are "ask the planner for a route given current world
 *     state," which is one operation; only the trigger differs, and the
 *     trigger is recorded separately as a ReplanReason for telemetry).
 *   - NAVIGATE_TO_HIVE -> DRIVE_TO_HIVE (naming consistency with
 *     DRIVE_TO_BALL).
 *   - UPDATE_MEMORY is not a discrete state - the world model (BallMap /
 *     HiveMap / Localization) is updated every loop tick regardless of
 *     which state we are in, since perception should never pause.
 *   - REPLAN_COLLECTION is not a separate visited state - any state can
 *     transition back to PLAN, carrying a ReplanReason.
 */
public enum AutoState {
    INITIALIZE,
    SCAN,
    PLAN,
    DRIVE_TO_BALL,
    ACQUIRE_BALL,
    VERIFY_ACQUISITION,
    DRIVE_TO_HIVE,
    AIM_AT_HIVE,
    SCORE,
    VERIFY_SCORE,
    DECIDE_NEXT_CYCLE,
    PARK,
    FINISHED,
    EMERGENCY_STOP
}
