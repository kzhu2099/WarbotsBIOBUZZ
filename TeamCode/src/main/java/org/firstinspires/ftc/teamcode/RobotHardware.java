package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/** Direct motor control used by the manual and timed-movement tests. */
public final class RobotHardware {
    private final DcMotorEx frontLeft;
    private final DcMotorEx frontRight;
    private final DcMotorEx backLeft;
    private final DcMotorEx backRight;

    public final Mechanisms mechanisms;

    public RobotHardware(HardwareMap hardwareMap, boolean useMechanisms) {
        frontLeft = getMotor(hardwareMap, RobotConfig.FRONT_LEFT);
        frontRight = getMotor(hardwareMap, RobotConfig.FRONT_RIGHT);
        backLeft = getMotor(hardwareMap, RobotConfig.BACK_LEFT);
        backRight = getMotor(hardwareMap, RobotConfig.BACK_RIGHT);

        configureDrive(frontLeft, RobotConfig.FRONT_LEFT_DIRECTION);
        configureDrive(frontRight, RobotConfig.FRONT_RIGHT_DIRECTION);
        configureDrive(backLeft, RobotConfig.BACK_LEFT_DIRECTION);
        configureDrive(backRight, RobotConfig.BACK_RIGHT_DIRECTION);

        mechanisms = useMechanisms ? new Mechanisms(hardwareMap) : null;
    }

    public boolean driveReady() {
        return frontLeft != null && frontRight != null && backLeft != null && backRight != null;
    }

    public String missingDriveMotors() {
        StringBuilder missing = new StringBuilder();
        addMissing(missing, RobotConfig.FRONT_LEFT, frontLeft);
        addMissing(missing, RobotConfig.FRONT_RIGHT, frontRight);
        addMissing(missing, RobotConfig.BACK_LEFT, backLeft);
        addMissing(missing, RobotConfig.BACK_RIGHT, backRight);
        return missing.toString();
    }

    /** Same wheel-power mix used by Pedro's Mecanum drivetrain. */
    public void drive(double forward, double strafe, double turn, double scale) {
        if (!driveReady()) {
            stopDrive();
            return;
        }

        double fl = forward - strafe - turn;
        double fr = forward + strafe + turn;
        double bl = forward + strafe - turn;
        double br = forward - strafe + turn;
        double max = Math.max(1.0, Math.max(
                Math.max(Math.abs(fl), Math.abs(fr)),
                Math.max(Math.abs(bl), Math.abs(br))));
        double powerScale = Math.max(0.0, Math.min(1.0, scale)) / max;

        frontLeft.setPower(fl * powerScale);
        frontRight.setPower(fr * powerScale);
        backLeft.setPower(bl * powerScale);
        backRight.setPower(br * powerScale);
    }

    public void stopDrive() {
        setPower(frontLeft, 0);
        setPower(frontRight, 0);
        setPower(backLeft, 0);
        setPower(backRight, 0);
    }

    public void stop() {
        stopDrive();
        if (mechanisms != null) mechanisms.stop();
    }

    private static DcMotorEx getMotor(HardwareMap map, String name) {
        try {
            return map.get(DcMotorEx.class, name);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static void configureDrive(DcMotorEx motor, DcMotorSimple.Direction direction) {
        if (motor == null) return;
        motor.setDirection(direction);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setPower(0);
    }

    private static void setPower(DcMotorEx motor, double power) {
        if (motor != null) motor.setPower(Math.max(-1.0, Math.min(1.0, power)));
    }

    private static void addMissing(StringBuilder out, String name, DcMotorEx motor) {
        if (motor == null) {
            if (out.length() > 0) out.append(", ");
            out.append(name);
        }
    }

    public static final class Mechanisms {
        private enum State { OFF, INTAKING, TRANSFERRING, READY, OUTTAKING, CLEARING, MANUAL }

        private final DcMotorEx intake;
        private final DcMotorEx transfer;
        private final DcMotorEx outtakeLeft;
        private final DcMotorEx outtakeRight;

        private State state = State.OFF;
        private double stateStartedAt;
        private boolean manualOverride;
        private boolean lastIntakeButton;
        private boolean lastShootButton;

        public Mechanisms(HardwareMap hardwareMap) {
            intake = getMotor(hardwareMap, RobotConfig.INTAKE);
            transfer = getMotor(hardwareMap, RobotConfig.TRANSFER);
            outtakeLeft = getMotor(hardwareMap, RobotConfig.OUTTAKE_LEFT);
            outtakeRight = getMotor(hardwareMap, RobotConfig.OUTTAKE_RIGHT);

            configureMechanism(intake, RobotConfig.INTAKE_DIRECTION);
            configureMechanism(transfer, RobotConfig.TRANSFER_DIRECTION);
            configureMechanism(outtakeLeft, RobotConfig.OUTTAKE_LEFT_DIRECTION);
            configureMechanism(outtakeRight, RobotConfig.OUTTAKE_RIGHT_DIRECTION);
        }

        /** Bumpers start timed intake/transfer and shooting. Hold Back for direct motor control. */
        public void update(double now, Gamepad gamepad) {
            if (gamepad.back) {
                if (!manualOverride) stopOutputs();
                manualOverride = true;
                state = State.MANUAL;
                set(intake, -gamepad.left_stick_y * RobotConfig.INTAKE_POWER);
                set(transfer, -gamepad.right_stick_y * RobotConfig.TRANSFER_POWER);
                set(outtakeLeft, gamepad.left_trigger * RobotConfig.OUTTAKE_LEFT_POWER);
                set(outtakeRight, gamepad.right_trigger * RobotConfig.OUTTAKE_RIGHT_POWER);
                lastIntakeButton = gamepad.left_bumper;
                lastShootButton = gamepad.right_bumper;
                return;
            }

            if (manualOverride) {
                stopOutputs();
                state = State.OFF;
                manualOverride = false;
            }

            boolean intakePressed = gamepad.left_bumper && !lastIntakeButton;
            boolean shootPressed = gamepad.right_bumper && !lastShootButton;
            lastIntakeButton = gamepad.left_bumper;
            lastShootButton = gamepad.right_bumper;

            if (intakePressed && state == State.OFF) {
                set(intake, RobotConfig.INTAKE_POWER);
                state = State.INTAKING;
                stateStartedAt = now;
            }

            if (state == State.INTAKING && now - stateStartedAt >= RobotConfig.INTAKE_SECONDS) {
                set(intake, 0);
                set(transfer, RobotConfig.TRANSFER_POWER);
                state = State.TRANSFERRING;
                stateStartedAt = now;
            } else if (state == State.TRANSFERRING
                    && now - stateStartedAt >= RobotConfig.TRANSFER_SECONDS) {
                set(transfer, 0);
                state = State.READY;
            }

            if (shootPressed && state == State.READY) {
                set(outtakeLeft, RobotConfig.OUTTAKE_LEFT_POWER);
                set(outtakeRight, RobotConfig.OUTTAKE_RIGHT_POWER);
                state = State.OUTTAKING;
                stateStartedAt = now;
            }

            if (state == State.OUTTAKING && now - stateStartedAt >= RobotConfig.OUTTAKE_SECONDS) {
                set(outtakeLeft, 0);
                set(outtakeRight, 0);
                state = State.CLEARING;
                stateStartedAt = now;
            } else if (state == State.CLEARING && now - stateStartedAt >= RobotConfig.CLEAR_SECONDS) {
                state = State.OFF;
            }
        }

        public String state() {
            return state.name();
        }

        public String missingMotors() {
            StringBuilder missing = new StringBuilder();
            addMissing(missing, RobotConfig.INTAKE, intake);
            addMissing(missing, RobotConfig.TRANSFER, transfer);
            addMissing(missing, RobotConfig.OUTTAKE_LEFT, outtakeLeft);
            addMissing(missing, RobotConfig.OUTTAKE_RIGHT, outtakeRight);
            return missing.toString();
        }

        public void stop() {
            stopOutputs();
            state = State.OFF;
            manualOverride = false;
        }

        private void stopOutputs() {
            set(intake, 0);
            set(transfer, 0);
            set(outtakeLeft, 0);
            set(outtakeRight, 0);
        }

        private static void configureMechanism(DcMotorEx motor, DcMotorSimple.Direction direction) {
            if (motor == null) return;
            motor.setDirection(direction);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            motor.setPower(0);
        }

        private static void set(DcMotorEx motor, double power) {
            if (motor != null) motor.setPower(Math.max(-1.0, Math.min(1.0, power)));
        }
    }
}
