package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.StringJoiner;

public final class RobotHardware {
    private final DcMotorEx frontLeft;
    private final DcMotorEx frontRight;
    private final DcMotorEx backLeft;
    private final DcMotorEx backRight;

    public RobotHardware(HardwareMap hardwareMap) {
        frontLeft = findMotor(hardwareMap, Constants.FRONT_LEFT_NAME, Constants.FRONT_LEFT_DIRECTION);
        frontRight = findMotor(hardwareMap, Constants.FRONT_RIGHT_NAME, Constants.FRONT_RIGHT_DIRECTION);
        backLeft = findMotor(hardwareMap, Constants.BACK_LEFT_NAME, Constants.BACK_LEFT_DIRECTION);
        backRight = findMotor(hardwareMap, Constants.BACK_RIGHT_NAME, Constants.BACK_RIGHT_DIRECTION);
    }

    public boolean driveReady() {
        return frontLeft != null && frontRight != null && backLeft != null && backRight != null;
    }

    public String missingDriveMotors() {
        StringJoiner missing = new StringJoiner(", ");
        addMissing(missing, Constants.FRONT_LEFT_NAME, frontLeft);
        addMissing(missing, Constants.FRONT_RIGHT_NAME, frontRight);
        addMissing(missing, Constants.BACK_LEFT_NAME, backLeft);
        addMissing(missing, Constants.BACK_RIGHT_NAME, backRight);
        return missing.toString();
    }

    public void drive(double forward, double strafe, double turn) {
        if (!driveReady()) {
            stopDrive();
            return;
        }

        double frontLeftPower = forward - strafe - turn;
        double frontRightPower = forward + strafe + turn;
        double backLeftPower = forward + strafe - turn;
        double backRightPower = forward - strafe + turn;

        double largestPower = Math.max(
                Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower)),
                Math.max(Math.abs(backLeftPower), Math.abs(backRightPower)));
        double scale = Math.max(1.0, largestPower);

        frontLeft.setPower(frontLeftPower / scale);
        frontRight.setPower(frontRightPower / scale);
        backLeft.setPower(backLeftPower / scale);
        backRight.setPower(backRightPower / scale);
    }

    public void stopDrive() {
        stopMotor(frontLeft);
        stopMotor(frontRight);
        stopMotor(backLeft);
        stopMotor(backRight);
    }

    public static final class MechanismController {
        private enum Action { STOPPED, INTAKING, TRANSFERRING, SHOOTING, MANUAL }

        private final DcMotorEx intakeMotor;
        private final DcMotorEx transferMotor;
        private final DcMotorEx leftOuttakeMotor;
        private final DcMotorEx rightOuttakeMotor;
        private final boolean shootingEnabled;

        private Action action = Action.STOPPED;
        private double actionStartedAt;
        private boolean lastIntakeButton;
        private boolean lastShootButton;

        public MechanismController(HardwareMap hardwareMap, boolean shootingEnabled) {
            this.shootingEnabled = shootingEnabled;
            intakeMotor = findMotor(hardwareMap,
                    RobotConfig.INTAKE_MOTOR_NAME, RobotConfig.INTAKE_MOTOR_DIRECTION);
            transferMotor = RobotConfig.transferEnabled()
                    ? findMotor(hardwareMap, RobotConfig.TRANSFER_MOTOR_NAME,
                    RobotConfig.TRANSFER_MOTOR_DIRECTION) : null;
            leftOuttakeMotor = findMotor(hardwareMap,
                    RobotConfig.LEFT_OUTTAKE_MOTOR_NAME, RobotConfig.LEFT_OUTTAKE_MOTOR_DIRECTION);
            rightOuttakeMotor = findMotor(hardwareMap,
                    RobotConfig.RIGHT_OUTTAKE_MOTOR_NAME, RobotConfig.RIGHT_OUTTAKE_MOTOR_DIRECTION);
        }

        public void update(double now, Gamepad gamepad) {
            boolean intakePressed = gamepad.left_bumper && !lastIntakeButton;
            boolean shootPressed = gamepad.right_bumper && !lastShootButton;
            lastIntakeButton = gamepad.left_bumper;
            lastShootButton = gamepad.right_bumper;

            if (gamepad.back) {
                runManualControls(gamepad);
                return;
            }

            if (action == Action.MANUAL) {
                stopMechanisms();
                action = Action.STOPPED;
            }

            if (shootingEnabled && shootPressed && action != Action.SHOOTING) {
                startShooting(now);
            } else if (intakePressed && action == Action.STOPPED) {
                startIntake(now);
            }

            updateTimedAction(now);
        }

        public String action() {
            return action.name();
        }

        public String missingMotors() {
            StringJoiner missing = new StringJoiner(", ");
            addMissing(missing, RobotConfig.INTAKE_MOTOR_NAME, intakeMotor);
            if (RobotConfig.transferEnabled()) {
                addMissing(missing, RobotConfig.TRANSFER_MOTOR_NAME, transferMotor);
            }
            if (shootingEnabled) {
                addMissing(missing, RobotConfig.LEFT_OUTTAKE_MOTOR_NAME, leftOuttakeMotor);
                addMissing(missing, RobotConfig.RIGHT_OUTTAKE_MOTOR_NAME, rightOuttakeMotor);
            }
            return missing.toString();
        }

        public String missingOuttakeMotors() {
            StringJoiner missing = new StringJoiner(", ");
            addMissing(missing, RobotConfig.LEFT_OUTTAKE_MOTOR_NAME, leftOuttakeMotor);
            addMissing(missing, RobotConfig.RIGHT_OUTTAKE_MOTOR_NAME, rightOuttakeMotor);
            return missing.toString();
        }

        public void stop() {
            stopMechanisms();
            action = Action.STOPPED;
        }

        private void runManualControls(Gamepad gamepad) {
            action = Action.MANUAL;
            setMotorPower(intakeMotor, -gamepad.left_stick_y * RobotConfig.INTAKE_POWER);
            setMotorPower(transferMotor, -gamepad.right_stick_y * RobotConfig.TRANSFER_POWER);

            if (shootingEnabled) {
                setMotorPower(leftOuttakeMotor,
                        gamepad.left_trigger * RobotConfig.LEFT_OUTTAKE_POWER);
                setMotorPower(rightOuttakeMotor,
                        gamepad.right_trigger * RobotConfig.RIGHT_OUTTAKE_POWER);
            }
        }

        private void startIntake(double now) {
            if (intakeMotor == null || (RobotConfig.transferEnabled() && transferMotor == null)) {
                return;
            }

            stopMechanisms();
            setMotorPower(intakeMotor, RobotConfig.INTAKE_POWER);
            action = Action.INTAKING;
            actionStartedAt = now;
        }

        private void startShooting(double now) {
            stopMechanisms();
            if (leftOuttakeMotor == null || rightOuttakeMotor == null) {
                action = Action.STOPPED;
                return;
            }

            setMotorPower(leftOuttakeMotor, RobotConfig.LEFT_OUTTAKE_POWER);
            setMotorPower(rightOuttakeMotor, RobotConfig.RIGHT_OUTTAKE_POWER);
            action = Action.SHOOTING;
            actionStartedAt = now;
        }

        private void updateTimedAction(double now) {
            if (action == Action.INTAKING && now - actionStartedAt >= RobotConfig.INTAKE_SECONDS) {
                setMotorPower(intakeMotor, 0);
                if (transferMotor != null) {
                    setMotorPower(transferMotor, RobotConfig.TRANSFER_POWER);
                    action = Action.TRANSFERRING;
                    actionStartedAt = now;
                } else {
                    action = Action.STOPPED;
                }
            } else if (action == Action.TRANSFERRING
                    && now - actionStartedAt >= RobotConfig.TRANSFER_SECONDS) {
                setMotorPower(transferMotor, 0);
                action = Action.STOPPED;
            } else if (action == Action.SHOOTING
                    && now - actionStartedAt >= RobotConfig.SHOOT_SECONDS) {
                setMotorPower(leftOuttakeMotor, 0);
                setMotorPower(rightOuttakeMotor, 0);
                action = Action.STOPPED;
            }
        }

        private void stopMechanisms() {
            stopMotor(intakeMotor);
            stopMotor(transferMotor);
            stopMotor(leftOuttakeMotor);
            stopMotor(rightOuttakeMotor);
        }
    }

    private static DcMotorEx findMotor(
            HardwareMap hardwareMap,
            String hardwareName,
            DcMotorSimple.Direction direction) {
        if (hardwareName == null || hardwareName.trim().isEmpty()) return null;

        try {
            DcMotorEx motor = hardwareMap.get(DcMotorEx.class, hardwareName);
            motor.setPower(0);
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            motor.setDirection(direction);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            return motor;
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static void setMotorPower(DcMotorEx motor, double power) {
        if (motor != null) {
            motor.setPower(Math.max(-1.0, Math.min(1.0, power)));
        }
    }

    private static void stopMotor(DcMotorEx motor) {
        if (motor == null) return;

        try {
            motor.setPower(0);
        } catch (RuntimeException ignored) {
            // Keep stopping the remaining motors.
        }
    }

    private static void addMissing(StringJoiner missing, String name, DcMotorEx motor) {
        if (motor == null) missing.add(name);
    }
}
