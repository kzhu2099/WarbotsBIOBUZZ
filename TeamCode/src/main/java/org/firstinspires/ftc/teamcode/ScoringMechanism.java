package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Controls the physical intake -> transfer -> outtake mechanism.
 *
 * Physical hardware used here:
 *
 *   1x intake motor
 *   1x transfer motor
 *   1x left outtake motor
 *   1x right outtake motor
 *
 * No beam-break, color sensor, distance sensor, encoder sensor,
 * or other mechanism sensor is required.
 *
 * Cameras are handled elsewhere by the vision system.
 *
 * Both outtake motors fire simultaneously for every scoring cycle.
 *
 * This class does NOT own Inventory or BallMap.
 * It only reports when a ball has been acquired, transferred,
 * and scored so that the caller can update those systems.
 */
public class ScoringMechanism {

    // ================================================================
    // TIMING VALUES
    // ================================================================

    /**
     * How long the intake runs before the code assumes that a ball
     * has successfully entered the mechanism.
     *
     * Increase this if the intake needs more time to pull a ball in.
     * Decrease this if the intake spends too long waiting before transfer.
     *
     * Units: seconds
     */
    private static final double INTAKE_ACQUIRE_TIME_SECONDS = 2.5;

    /**
     * How long the transfer motor runs before the code assumes that
     * the ball has reached the scoring position.
     *
     * Increase this if the ball is not consistently reaching the outtake.
     * Decrease this if the transfer is unnecessarily slow.
     *
     * Units: seconds
     */
    private static final double TRANSFER_TIME_SECONDS = 1.5;

    /**
     * How long both outtake motors run during a scoring cycle.
     *
     * Increase this if the ball is not reliably expelled.
     * Decrease this if the mechanism is unnecessarily running after
     * the ball has already been scored.
     *
     * Units: seconds
     */
    private static final double OUTTAKE_DURATION_SECONDS = 1.0;

    /**
     * Short delay after the outtake stops before the mechanism becomes
     * available for another cycle.
     *
     * Increase this if the mechanism needs additional time to clear.
     * Decrease this if you want faster consecutive cycles.
     *
     * Units: seconds
     */
    private static final double CLEARING_DURATION_SECONDS = 0.3;


    // ================================================================
    // MOTORS
    // ================================================================

    private final DcMotorEx intakeMotor;
    private final DcMotorEx transferMotor;
    private final DcMotorEx outtakeLeftMotor;
    private final DcMotorEx outtakeRightMotor;


    // ================================================================
    // STATE
    // ================================================================

    private ScoringState state = ScoringState.INTAKE_OFF;
    private ScoringState previousState = ScoringState.INTAKE_OFF;

    private double stateEnteredAtSeconds = 0.0;

    /**
     * Ball type expected by the planner/camera system.
     *
     * This is informational for classification/inventory purposes.
     * It does NOT determine which outtake motor fires because both
     * outtake motors fire together.
     */
    private BallType expectedType;

    /**
     * Type assigned to the most recently transferred ball.
     */
    private BallType lastTransferredType;

    /**
     * True because, without physical mechanism sensors, acquisition
     * is determined by timing rather than physically verified.
     */
    private boolean lastAcquisitionWasAssumed = false;

    /**
     * Manual TeleOp override.
     */
    private boolean manualOverrideActive = false;


    // ================================================================
    // CONSTRUCTOR
    // ================================================================

    public ScoringMechanism(HardwareMap hardwareMap) {

        intakeMotor = getMotor(
                hardwareMap,
                HardwareConfig.INTAKE_MOTOR
        );

        transferMotor = getMotor(
                hardwareMap,
                HardwareConfig.TRANSFER_MOTOR
        );

        outtakeLeftMotor = getMotor(
                hardwareMap,
                HardwareConfig.OUTTAKE_LEFT_MOTOR
        );

        outtakeRightMotor = getMotor(
                hardwareMap,
                HardwareConfig.OUTTAKE_RIGHT_MOTOR
        );
    }


    // ================================================================
    // HARDWARE LOOKUP
    // ================================================================

    /**
     * Gets a motor from the Robot Configuration.
     *
     * Returning null instead of crashing allows the rest of the code
     * to remain robust if a motor has not been configured yet.
     */
    private static DcMotorEx getMotor(
            HardwareMap hardwareMap,
            String name
    ) {
        try {
            return hardwareMap.get(DcMotorEx.class, name);
        } catch (Exception e) {
            return null;
        }
    }


    // ================================================================
    // INTAKE
    // ================================================================

    /**
     * Starts intake without specifying a ball type.
     */
    public void startIntake(double nowSeconds) {
        startIntake(null, nowSeconds);
    }

    /**
     * Starts intake.
     *
     * expectedType may be supplied by the autonomous planner or
     * vision system. It is retained for Inventory/classification
     * purposes.
     */
    public void startIntake(
            BallType expectedType,
            double nowSeconds
    ) {
        if (manualOverrideActive) {
            return;
        }

        if (state != ScoringState.INTAKE_OFF) {
            return;
        }

        this.expectedType = expectedType;
        this.lastAcquisitionWasAssumed = false;

        setMotor(intakeMotor, 1.0);

        transition(
                ScoringState.INTAKING,
                nowSeconds
        );
    }

    /**
     * Stops an active intake.
     */
    public void stopIntake(double nowSeconds) {
        if (state == ScoringState.INTAKING) {

            setMotor(intakeMotor, 0);

            transition(
                    ScoringState.INTAKE_OFF,
                    nowSeconds
            );
        }
    }


    // ================================================================
    // OUTTAKE / SCORING
    // ================================================================

    /**
     * Starts a scoring cycle.
     *
     * BOTH outtake motors fire simultaneously.
     *
     * The BallType does not select the motor. This is intentional:
     * left and right outtake motors are both activated to maximize
     * scoring reliability.
     *
     * Returns true if the scoring cycle was successfully started.
     */
    public boolean triggerOuttake(double nowSeconds) {

        if (manualOverrideActive) {
            return false;
        }

        if (state != ScoringState.READY_TO_SCORE) {
            return false;
        }

        // Both outtake motors fire.
        setMotor(outtakeLeftMotor, 1.0);
        setMotor(outtakeRightMotor, 1.0);

        transition(
                ScoringState.OUTTAKING,
                nowSeconds
        );

        return true;
    }


    // ================================================================
    // FAULT HANDLING
    // ================================================================

    /**
     * Clears a fault and returns the mechanism to idle.
     */
    public void clearFault(double nowSeconds) {

        if (state == ScoringState.FAULT) {

            stopAll();

            transition(
                    ScoringState.INTAKE_OFF,
                    nowSeconds
            );
        }
    }


    // ================================================================
    // MAIN STATE-MACHINE UPDATE
    // ================================================================

    /**
     * Call exactly once per control loop.
     *
     * Because the robot has no physical mechanism sensors,
     * progression through intake and transfer is time-based.
     */
    public void update(double nowSeconds) {

        previousState = state;

        if (manualOverrideActive) {
            return;
        }

        double elapsed =
                nowSeconds - stateEnteredAtSeconds;


        switch (state) {

            // --------------------------------------------------------
            // IDLE
            // --------------------------------------------------------

            case INTAKE_OFF:

                // Waiting for startIntake().
                break;


            // --------------------------------------------------------
            // INTAKING
            // --------------------------------------------------------

            case INTAKING:

                /*
                 * No beam-break exists.
                 *
                 * Therefore the code assumes the ball has entered
                 * after the configured intake dwell time.
                 */
                if (elapsed >= INTAKE_ACQUIRE_TIME_SECONDS) {

                    lastAcquisitionWasAssumed = true;

                    setMotor(intakeMotor, 0);

                    setMotor(transferMotor, 1.0);

                    transition(
                            ScoringState.TRANSFERRING,
                            nowSeconds
                    );
                }

                break;


            // --------------------------------------------------------
            // TRANSFERRING
            // --------------------------------------------------------

            case TRANSFERRING:

                /*
                 * No distance/color sensor exists.
                 *
                 * Therefore the code assumes the ball has reached
                 * the scoring position after the configured transfer
                 * time.
                 */
                if (elapsed >= TRANSFER_TIME_SECONDS) {

                    setMotor(transferMotor, 0);

                    lastTransferredType = classify();

                    transition(
                            ScoringState.READY_TO_SCORE,
                            nowSeconds
                    );
                }

                break;


            // --------------------------------------------------------
            // READY TO SCORE
            // --------------------------------------------------------

            case READY_TO_SCORE:

                /*
                 * Wait here until triggerOuttake() is called.
                 */
                break;


            // --------------------------------------------------------
            // OUTTAKING
            // --------------------------------------------------------

            case OUTTAKING:

                if (elapsed >= OUTTAKE_DURATION_SECONDS) {

                    setMotor(outtakeLeftMotor, 0);
                    setMotor(outtakeRightMotor, 0);

                    transition(
                            ScoringState.CLEARING,
                            nowSeconds
                    );
                }

                break;


            // --------------------------------------------------------
            // CLEARING
            // --------------------------------------------------------

            case CLEARING:

                if (elapsed >= CLEARING_DURATION_SECONDS) {

                    transition(
                            ScoringState.INTAKE_OFF,
                            nowSeconds
                    );
                }

                break;


            // --------------------------------------------------------
            // FAULT
            // --------------------------------------------------------

            case FAULT:

                /*
                 * Holds until clearFault() is explicitly called.
                 */
                break;
        }
    }


    // ================================================================
    // BALL CLASSIFICATION
    // ================================================================

    /**
     * Determines the type assigned to the transferred ball.
     *
     * Since there is no color sensor in the robot, this cannot
     * physically classify the ball inside the mechanism.
     *
     * If the planner/vision system supplied an expected type,
     * that type is retained.
     *
     * Otherwise the existing default is POLLEN.
     */
    private BallType classify() {

        if (expectedType != null) {
            return expectedType;
        }

        return BallType.POLLEN;
    }


    // ================================================================
    // STATE TRANSITION
    // ================================================================

    private void transition(
            ScoringState next,
            double nowSeconds
    ) {
        state = next;
        stateEnteredAtSeconds = nowSeconds;
    }


    // ================================================================
    // MOTOR CONTROL
    // ================================================================

    private void setMotor(
            DcMotorEx motor,
            double power
    ) {
        if (motor != null) {
            motor.setPower(power);
        }
    }


    // ================================================================
    // MANUAL TELEOP OVERRIDE
    // ================================================================

    /**
     * Enables or disables manual control.
     */
    public void setManualOverride(
            boolean active,
            double nowSeconds
    ) {

        if (active == manualOverrideActive) {
            return;
        }

        manualOverrideActive = active;

        if (active) {

            stopAll();

        } else {

            transition(
                    ScoringState.INTAKE_OFF,
                    nowSeconds
            );
        }
    }


    /**
     * Manually controls the intake motor.
     */
    public void manualIntake(double power) {

        if (manualOverrideActive) {
            setMotor(intakeMotor, power);
        }
    }


    /**
     * Manually controls the transfer motor.
     */
    public void manualTransfer(double power) {

        if (manualOverrideActive) {
            setMotor(transferMotor, power);
        }
    }


    /**
     * Manually controls BOTH outtake motors.
     */
    public void manualOuttake(double power) {

        if (manualOverrideActive) {

            setMotor(outtakeLeftMotor, power);
            setMotor(outtakeRightMotor, power);
        }
    }


    // ================================================================
    // STATE / EVENT QUERIES
    // ================================================================

    /**
     * Returns the current mechanism state.
     */
    public ScoringState state() {
        return state;
    }


    /**
     * True for exactly one update() call when a ball reaches
     * the scoring position.
     *
     * Caller can use this to update Inventory / BallMap.
     */
    public boolean justReachedReadyToScore() {

        return previousState != ScoringState.READY_TO_SCORE
                && state == ScoringState.READY_TO_SCORE;
    }


    /**
     * True for exactly one update() call when the complete
     * outtake + clearing cycle has finished.
     *
     * Caller can use this to update Inventory.
     */
    public boolean justCompletedScoreCycle() {

        return previousState == ScoringState.CLEARING
                && state == ScoringState.INTAKE_OFF;
    }


    /**
     * With no intake sensor, an ordinary timed intake does not
     * generate a genuine failure event.
     *
     * This method is retained so existing caller code still compiles.
     */
    public boolean justFailedIntake() {

        return false;
    }


    /**
     * Returns true for one update() call when the mechanism
     * enters FAULT.
     *
     * The current sensorless implementation does not generate
     * an automatic transfer fault because there is no sensor
     * capable of detecting a jam.
     */
    public boolean justFaulted() {

        return previousState != ScoringState.FAULT
                && state == ScoringState.FAULT;
    }


    /**
     * Returns the type assigned to the most recently transferred ball.
     */
    public BallType lastTransferredType() {

        return lastTransferredType;
    }


    /**
     * Returns whether the most recent acquisition was assumed
     * rather than physically sensor-confirmed.
     *
     * With this robot configuration, this will be true after
     * a normal timed acquisition.
     */
    public boolean lastAcquisitionWasAssumed() {

        return lastAcquisitionWasAssumed;
    }


    /**
     * Returns true while the mechanism is performing a cycle.
     */
    public boolean isBusy() {

        return state == ScoringState.INTAKING
                || state == ScoringState.TRANSFERRING
                || state == ScoringState.OUTTAKING
                || state == ScoringState.CLEARING;
    }


    // ================================================================
    // EMERGENCY STOP
    // ================================================================

    /**
     * Immediately stops all four mechanism motors.
     */
    public void stopAll() {

        setMotor(intakeMotor, 0);
        setMotor(transferMotor, 0);
        setMotor(outtakeLeftMotor, 0);
        setMotor(outtakeRightMotor, 0);
    }
}