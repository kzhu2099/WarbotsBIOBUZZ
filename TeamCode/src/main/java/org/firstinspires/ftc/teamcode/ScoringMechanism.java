package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Controls intake, transfer, and the two outtake motors. There are no
 * sensors to confirm that a game piece moved, so each state change is timed.
 */
public class ScoringMechanism {

    // Times are in seconds. Tune one stage at a time on the real mechanism.
    public static double INTAKE_ACQUIRE_TIME_SECONDS = 2.5;
    public static double TRANSFER_TIME_SECONDS = 1.5;
    public static double OUTTAKE_DURATION_SECONDS = 1.0;
    public static double CLEARING_DURATION_SECONDS = 0.3;

    // Powers are clamped to [-1, 1]. A negative value reverses that motor.
    public static double INTAKE_POWER = 1.0;
    public static double TRANSFER_POWER = 1.0;
    public static double OUTTAKE_LEFT_POWER = 1.0;
    public static double OUTTAKE_RIGHT_POWER = 1.0;

    // MOTORS

    private final DcMotorEx intakeMotor;
    private final DcMotorEx transferMotor;
    private final DcMotorEx outtakeLeftMotor;
    private final DcMotorEx outtakeRightMotor;

    // STATE

    private ScoringState state = ScoringState.INTAKE_OFF;
    private ScoringState previousState = ScoringState.INTAKE_OFF;

    private double stateEnteredAtSeconds = 0.0;

    private BallType expectedType;

    private BallType lastTransferredType;

    private boolean lastAcquisitionWasAssumed = false;

    private boolean manualOverrideActive = false;

    // CONSTRUCTOR

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

    public boolean hasAllMotors() {
        return intakeMotor != null
                && transferMotor != null
                && outtakeLeftMotor != null
                && outtakeRightMotor != null;
    }

    /** Names of configured mechanism motors that are missing, for Driver Station warnings. */
    public String missingMotorNames() {
        StringBuilder missing = new StringBuilder();
        addMissing(missing, HardwareConfig.INTAKE_MOTOR, intakeMotor);
        addMissing(missing, HardwareConfig.TRANSFER_MOTOR, transferMotor);
        addMissing(missing, HardwareConfig.OUTTAKE_LEFT_MOTOR, outtakeLeftMotor);
        addMissing(missing, HardwareConfig.OUTTAKE_RIGHT_MOTOR, outtakeRightMotor);
        return missing.toString();
    }

    private static void addMissing(StringBuilder missing, String name, DcMotorEx motor) {
        if (motor == null) {
            if (missing.length() > 0) missing.append(", ");
            missing.append(name);
        }
    }

    // HARDWARE LOOKUP

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

    // INTAKE

    public void startIntake(double nowSeconds) {
        startIntake(null, nowSeconds);
    }

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

        setMotor(intakeMotor, INTAKE_POWER);

        transition(
                ScoringState.INTAKING,
                nowSeconds
        );
    }

    public void stopIntake(double nowSeconds) {
        if (state == ScoringState.INTAKING) {

            setMotor(intakeMotor, 0);

            transition(
                    ScoringState.INTAKE_OFF,
                    nowSeconds
            );
        }
    }

    // OUTTAKE / SCORING

    public boolean triggerOuttake(double nowSeconds) {

        if (manualOverrideActive) {
            return false;
        }

        if (state != ScoringState.READY_TO_SCORE) {
            return false;
        }

        // Both outtake motors fire.
        setMotor(outtakeLeftMotor, OUTTAKE_LEFT_POWER);
        setMotor(outtakeRightMotor, OUTTAKE_RIGHT_POWER);

        transition(
                ScoringState.OUTTAKING,
                nowSeconds
        );

        return true;
    }

    // FAULT HANDLING

    public void clearFault(double nowSeconds) {

        if (state == ScoringState.FAULT) {

            stopAll();

            transition(
                    ScoringState.INTAKE_OFF,
                    nowSeconds
            );
        }
    }

    // MAIN STATE-MACHINE UPDATE

    public void update(double nowSeconds) {

        previousState = state;

        if (manualOverrideActive) {
            return;
        }

        double elapsed =
                nowSeconds - stateEnteredAtSeconds;

        switch (state) {

            // IDLE

            case INTAKE_OFF:

                // Waiting for startIntake().
                break;

            // INTAKING

            case INTAKING:

                if (elapsed >= INTAKE_ACQUIRE_TIME_SECONDS) {

                    lastAcquisitionWasAssumed = true;

                    setMotor(intakeMotor, 0);

                    setMotor(transferMotor, TRANSFER_POWER);

                    transition(
                            ScoringState.TRANSFERRING,
                            nowSeconds
                    );
                }

                break;

            // TRANSFERRING

            case TRANSFERRING:

                if (elapsed >= TRANSFER_TIME_SECONDS) {

                    setMotor(transferMotor, 0);

                    lastTransferredType = classify();

                    transition(
                            ScoringState.READY_TO_SCORE,
                            nowSeconds
                    );
                }

                break;

            // READY TO SCORE

            case READY_TO_SCORE:

                break;

            // OUTTAKING

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

            // CLEARING

            case CLEARING:

                if (elapsed >= CLEARING_DURATION_SECONDS) {

                    transition(
                            ScoringState.INTAKE_OFF,
                            nowSeconds
                    );
                }

                break;

            // FAULT

            case FAULT:

                break;
        }
    }

    // BALL CLASSIFICATION

    private BallType classify() {

        if (expectedType != null) {
            return expectedType;
        }

        return BallType.POLLEN;
    }

    // STATE TRANSITION

    private void transition(
            ScoringState next,
            double nowSeconds
    ) {
        state = next;
        stateEnteredAtSeconds = nowSeconds;
    }

    // MOTOR CONTROL

    private void setMotor(
            DcMotorEx motor,
            double power
    ) {
        if (motor != null) {
            motor.setPower(Math.max(-1.0, Math.min(1.0, power)));
        }
    }

    // MANUAL TELEOP OVERRIDE

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

    public void manualIntake(double power) {

        if (manualOverrideActive) {
            setMotor(intakeMotor, power);
        }
    }

    public void manualTransfer(double power) {

        if (manualOverrideActive) {
            setMotor(transferMotor, power);
        }
    }

    public void manualOuttake(double power) {

        if (manualOverrideActive) {

            setMotor(outtakeLeftMotor, power);
            setMotor(outtakeRightMotor, power);
        }
    }

    // STATE / EVENT QUERIES

    public ScoringState state() {
        return state;
    }

    public boolean justReachedReadyToScore() {

        return previousState != ScoringState.READY_TO_SCORE
                && state == ScoringState.READY_TO_SCORE;
    }

    public boolean justCompletedScoreCycle() {

        return previousState == ScoringState.CLEARING
                && state == ScoringState.INTAKE_OFF;
    }

    public boolean justFailedIntake() {

        return false;
    }

    public boolean justFaulted() {

        return previousState != ScoringState.FAULT
                && state == ScoringState.FAULT;
    }

    public BallType lastTransferredType() {

        return lastTransferredType;
    }

    public boolean lastAcquisitionWasAssumed() {

        return lastAcquisitionWasAssumed;
    }

    public boolean isBusy() {

        return state == ScoringState.INTAKING
                || state == ScoringState.TRANSFERRING
                || state == ScoringState.OUTTAKING
                || state == ScoringState.CLEARING;
    }

    // EMERGENCY STOP

    public void stopAll() {

        setMotor(intakeMotor, 0);
        setMotor(transferMotor, 0);
        setMotor(outtakeLeftMotor, 0);
        setMotor(outtakeRightMotor, 0);
    }
}
