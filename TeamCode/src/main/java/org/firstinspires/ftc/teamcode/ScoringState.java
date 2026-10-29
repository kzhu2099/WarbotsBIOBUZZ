package org.firstinspires.ftc.teamcode;

/**
 * State of the intake, transfer, and outtake sequence. There are no piece
 * sensors, so reaching READY_TO_SCORE means the transfer timer finished,
 * not that a game piece was physically confirmed in place.
 */
public enum ScoringState {
    /** Intake is off and ready to start. */
    INTAKE_OFF,

    /** Intake motor is running. */
    INTAKING,

    /** Transfer motor is moving a piece toward the shooter. */
    TRANSFERRING,

    /** The timed transfer finished; the driver can shoot. */
    READY_TO_SCORE,

    /** Outtake motors are running. */
    OUTTAKING,

    /** Brief pause after outtake before the next cycle. */
    CLEARING,

    /** Mechanism stopped after an error and needs recovery. */
    FAULT
}
