package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

public final class RobotHardware {
    private final DcMotorEx frontLeft;
    private final DcMotorEx frontRight;
    private final DcMotorEx backLeft;
    private final DcMotorEx backRight;

    public RobotHardware(HardwareMap hardwareMap) {
        frontLeft = configure(getMotor(hardwareMap, RobotConfig.FRONT_LEFT), RobotConfig.FRONT_LEFT_DIRECTION);
        frontRight = configure(getMotor(hardwareMap, RobotConfig.FRONT_RIGHT), RobotConfig.FRONT_RIGHT_DIRECTION);
        backLeft = configure(getMotor(hardwareMap, RobotConfig.BACK_LEFT), RobotConfig.BACK_LEFT_DIRECTION);
        backRight = configure(getMotor(hardwareMap, RobotConfig.BACK_RIGHT), RobotConfig.BACK_RIGHT_DIRECTION);
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

    public void drive(double forward, double strafe, double turn) {
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

        frontLeft.setPower(fl / max);
        frontRight.setPower(fr / max);
        backLeft.setPower(bl / max);
        backRight.setPower(br / max);
    }

    public void stopDrive() {
        stopMotor(frontLeft);
        stopMotor(frontRight);
        stopMotor(backLeft);
        stopMotor(backRight);
    }

    public static final class Mechanisms {
        private enum Action { OFF, INTAKE, TRANSFER, SHOOT, MANUAL }

        private final DcMotorEx intake;
        private final DcMotorEx transfer;
        private final DcMotorEx outtakeLeft;
        private final DcMotorEx outtakeRight;
        private final boolean shootingEnabled;

        private Action action = Action.OFF;
        private double startedAt;
        private boolean lastIntakeButton;
        private boolean lastShootButton;

        public Mechanisms(HardwareMap hardwareMap, boolean shootingEnabled) {
            this.shootingEnabled = shootingEnabled;

            intake = configure(getMotor(hardwareMap, RobotConfig.INTAKE), RobotConfig.INTAKE_DIRECTION);
            transfer = configure(getMotor(hardwareMap, RobotConfig.TRANSFER), RobotConfig.TRANSFER_DIRECTION);
            outtakeLeft = shootingEnabled
                    ? configure(getMotor(hardwareMap, RobotConfig.OUTTAKE_LEFT), RobotConfig.OUTTAKE_LEFT_DIRECTION)
                    : null;
            outtakeRight = shootingEnabled
                    ? configure(getMotor(hardwareMap, RobotConfig.OUTTAKE_RIGHT), RobotConfig.OUTTAKE_RIGHT_DIRECTION)
                    : null;
        }

        public void update(double now, Gamepad gamepad) {
            boolean intakePressed = gamepad.left_bumper && !lastIntakeButton;
            boolean shootPressed = gamepad.right_bumper && !lastShootButton;
            lastIntakeButton = gamepad.left_bumper;
            lastShootButton = gamepad.right_bumper;

            if (gamepad.back) {
                if (action != Action.MANUAL) stopOutputs();
                action = Action.MANUAL;
                set(intake, -gamepad.left_stick_y * RobotConfig.INTAKE_POWER);
                set(transfer, -gamepad.right_stick_y * RobotConfig.TRANSFER_POWER);
                if (shootingEnabled) {
                    set(outtakeLeft, gamepad.left_trigger * RobotConfig.OUTTAKE_LEFT_POWER);
                    set(outtakeRight, gamepad.right_trigger * RobotConfig.OUTTAKE_RIGHT_POWER);
                }
                return;
            }

            if (action == Action.MANUAL) {
                stopOutputs();
                action = Action.OFF;
            }

            // A shoot press can interrupt intake/transfer, but cannot restart an active shot.
            if (shootingEnabled && shootPressed && action != Action.SHOOT) {
                startShoot(now);
            } else if (intakePressed && action == Action.OFF) {
                startIntake(now);
            }

            if (action == Action.INTAKE && now - startedAt >= RobotConfig.INTAKE_SECONDS) {
                set(intake, 0);
                set(transfer, RobotConfig.TRANSFER_POWER);
                action = Action.TRANSFER;
                startedAt = now;
            } else if (action == Action.TRANSFER
                    && now - startedAt >= RobotConfig.TRANSFER_SECONDS) {
                stopOutputs();
                action = Action.OFF;
            } else if (action == Action.SHOOT
                    && now - startedAt >= RobotConfig.SHOOT_SECONDS) {
                stopOutputs();
                action = Action.OFF;
            }
        }

        public String action() {
            return action.name();
        }

        public String missingMotors() {
            StringBuilder missing = new StringBuilder();
            addMissing(missing, RobotConfig.INTAKE, intake);
            addMissing(missing, RobotConfig.TRANSFER, transfer);
            if (shootingEnabled) {
                addMissing(missing, RobotConfig.OUTTAKE_LEFT, outtakeLeft);
                addMissing(missing, RobotConfig.OUTTAKE_RIGHT, outtakeRight);
            }
            return missing.toString();
        }

        public void stop() {
            stopOutputs();
            action = Action.OFF;
        }

        private void startIntake(double now) {
            if (intake == null || transfer == null) {
                stopOutputs();
                action = Action.OFF;
                return;
            }
            stopOutputs();
            set(intake, RobotConfig.INTAKE_POWER);
            action = Action.INTAKE;
            startedAt = now;
        }

        private void startShoot(double now) {
            if (outtakeLeft == null || outtakeRight == null) {
                stopOutputs();
                action = Action.OFF;
                return;
            }
            stopOutputs();
            set(outtakeLeft, RobotConfig.OUTTAKE_LEFT_POWER);
            set(outtakeRight, RobotConfig.OUTTAKE_RIGHT_POWER);
            action = Action.SHOOT;
            startedAt = now;
        }

        private void stopOutputs() {
            stopMotor(intake);
            stopMotor(transfer);
            stopMotor(outtakeLeft);
            stopMotor(outtakeRight);
        }
    }

    private static DcMotorEx getMotor(HardwareMap map, String name) {
        try {
            return map.get(DcMotorEx.class, name);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static DcMotorEx configure(DcMotorEx motor, DcMotorSimple.Direction direction) {
        if (motor == null) return null;
        try {
            motor.setPower(0);
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            motor.setDirection(direction);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            return motor;
        } catch (RuntimeException e) {
            stopMotor(motor);
            return null;
        }
    }

    private static void set(DcMotorEx motor, double power) {
        if (motor != null) motor.setPower(Math.max(-1.0, Math.min(1.0, power)));
    }

    private static void stopMotor(DcMotorEx motor) {
        if (motor == null) return;
        try {
            motor.setPower(0);
        } catch (RuntimeException ignored) {
            // Still attempt to stop the other motors.
        }
    }

    private static void addMissing(StringBuilder out, String name, DcMotorEx motor) {
        if (motor == null) {
            if (out.length() > 0) out.append(", ");
            out.append(name);
        }
    }
}
