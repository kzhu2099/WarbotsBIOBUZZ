package org.firstinspires.ftc.teamcode;

/**
 * Why {@link AutonomousController} is (re)invoking the planner. Kept as an
 * explicit, named set of triggers rather than recomputing a fresh route
 * every loop iteration - both so the robot doesn't churn plans on
 * meaningless noise, and so telemetry can tell the driver *why* the robot
 * just changed its mind (see section 33 of the project brief: "REPLAN
 * REASON").
 */
public enum ReplanReason {
    NONE,
    INITIAL_PLAN,
    TARGET_MISSING,
    TARGET_ALREADY_COLLECTED,
    NEW_BALL_DETECTED,
    OPPONENT_BALL_BLOCKING_PATH,
    ACTIVE_HIVE_CHANGED,
    POSE_DEVIATION,
    INTAKE_FAILED,
    TRANSFER_FAILED,
    SCORING_FAILED,
    CAPACITY_CHANGED,
    TIME_THRESHOLD_REACHED,
    NO_VIABLE_ROUTE,
    MANUAL_SKIP
}
