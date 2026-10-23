package org.firstinspires.ftc.teamcode;

/** States handled by AutonomousController. */
public enum AutoState {
    INITIALIZE,
    SCAN,
    PLAN,
    DRIVE_TO_BALL,
    ACQUIRE_BALL,
    VERIFY_ACQUISITION,
    DRIVE_TO_CELL,
    AIM_AT_CELL,
    DRIVE_TO_SCORE,
    SCORE,
    VERIFY_SCORE,
    DECIDE_NEXT_CYCLE,
    PARK,
    FINISHED,
    EMERGENCY_STOP
}
