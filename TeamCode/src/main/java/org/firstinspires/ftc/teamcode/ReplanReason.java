package org.firstinspires.ftc.teamcode;

/** The reason the autonomous controller decides to choose a new route. */
public enum ReplanReason {
    NONE,
    INITIAL_PLAN,
    TARGET_MISSING,
    TARGET_ALREADY_COLLECTED,
    NEW_BALL_DETECTED,
    OPPONENT_BALL_BLOCKING_PATH,
    ACTIVE_CELL_CHANGED,
    POSE_DEVIATION,
    INTAKE_FAILED,
    TRANSFER_FAILED,
    SCORING_FAILED,
    CAPACITY_CHANGED,
    TIME_THRESHOLD_REACHED,
    NO_VIABLE_ROUTE,
    MANUAL_SKIP
}
