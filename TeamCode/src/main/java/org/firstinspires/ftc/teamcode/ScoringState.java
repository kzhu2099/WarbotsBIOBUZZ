package org.firstinspires.ftc.teamcode;

/**
 * State of the physical intake -> transfer -> outtake pipeline, owned and
 * driven by {@link ScoringMechanism}. This is the "actual subsystem state
 * model" the project brief asks for in place of bare intake()/outtake()
 * methods that just set motor power.
 */
public enum ScoringState {
    /** Intake is off. Idle, ready to be commanded on. */
    INTAKE_OFF,

    /** Intake is running, actively trying to pick up a ball. */
    INTAKING,

    /** A ball was detected entering the mechanism; moving it to the ready
     *  position via the transfer stage. */
    TRANSFERRING,

    /** A ball (or balls) has reached the ready/stored position and the
     *  mechanism is prepared to outtake on command. */
    READY_TO_SCORE,

    /** Outtake motors are actively running to score into the hive. */
    OUTTAKING,

    /** Outtake just ran; briefly continuing to make sure the mechanism
     *  path is fully clear before accepting the next ball. */
    CLEARING,

    /** Something did not behave as expected (e.g. a ball entered intake
     *  but never reached the ready position within a reasonable time, or
     *  a sensor reports an impossible state). Mechanism stops moving balls
     *  automatically and waits for manual/automatic recovery. */
    FAULT
}
