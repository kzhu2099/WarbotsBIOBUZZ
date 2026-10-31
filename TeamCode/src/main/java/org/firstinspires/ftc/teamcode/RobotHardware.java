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
        frontLeft = getMotor(hardwareMap, RobotConfig.FRONT_LEFT);
        frontRight = getMotor(hardwareMap, RobotConfig.FRONT_RIGHT);
        backLeft = getMotor(hardwareMap, RobotConfig.BACK_LEFT);
        backRight = getMotor(hardwareMap, RobotConfig.BACK_RIGHT);

        configure(frontLeft, RobotConfig.FRONT_LEFT_DIRECTION);
        configure(frontRight, RobotConfig.FRONT_RIGHT_DIRECTION);
        configure(backLeft, RobotConfig.BACK_LEFT_DIRECTION);
        configure(backRight, RobotConfig.BACK_RIGHT_DIRECTION);
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
        double power = Math.max(0.0, Math.min(1.0, scale)) / max;

        frontLeft.setPower(fl * power);
        frontRight.setPower(fr * power);
        backLeft.setPower(bl * power);
        backRight.setPower(br * power);
    }

    public void stopDrive() {
        set(frontLeft, 0);
        set(frontRight, 0);
        set(backLeft, 0);
        set(backRight, 0);
    }

    public static final class Mechanisms {
        private enum Action { OFF, INTAKE, TRANSFER, SHOOT, MANUAL }

        private final DcMotorEx intake;
        private final DcMotorEx transfer;
        private final DcMotorEx outtakeLeft;
        private final DcMotorEx outtakeRight;

        private Action action = Action.OFF;
        private double actionStarted;
        private boolean lastIntakeButton;
        private boolean lastShootButton;

        public Mechanisms(HardwareMap hardwareMap) {
            intake = getMotor(hardwareMap, RobotConfig.INTAKE);
            transfer = getMotor(hardwareMap, RobotConfig.TRANSFER);
            outtakeLeft = getMotor(hardwareMap, RobotConfig.OUTTAKE_LEFT);
            outtakeRight = getMotor(hardwareMap, RobotConfig.OUTTAKE_RIGHT);

            configure(intake, RobotConfig.INTAKE_DIRECTION);
            configure(transfer, RobotConfig.TRANSFER_DIRECTION);
            configure(outtakeLeft, RobotConfig.OUTTAKE_LEFT_DIRECTION);
            configure(outtakeRight, RobotConfig.OUTTAKE_RIGHT_DIRECTION);
        }

        // Left bumper collects then transfers. Right bumper shoots immediately.
        // Hold Back to control each mechanism motor separately.
        public void update(double now, Gamepad gamepad) {
            boolean intakePressed = gamepad.left_bumper && !lastIntakeButton;
            boolean shootPressed = gamepad.right_bumper && !lastShootButton;
            lastIntakeButton = gamepad.left_bumper;
            lastShootButton = gamepad.right_bumper;

            if (gamepad.back) {
                action = Action.MANUAL;
                set(intake, -gamepad.left_stick_y * RobotConfig.INTAKE_POWER);
                set(transfer, -gamepad.right_stick_y * RobotConfig.TRANSFER_POWER);
                set(outtakeLeft, gamepad.left_trigger * RobotConfig.OUTTAKE_LEFT_POWER);
                set(outtakeRight, gamepad.right_trigger * RobotConfig.OUTTAKE_RIGHT_POWER);
                return;
            }

            if (action == Action.MANUAL) {
                stopOutputs();
                action = Action.OFF;
            }

            if (shootPressed) {
                startShoot(now);
            } else if (intakePressed) {
                startIntake(now);
            }

            if (action == Action.INTAKE && now - actionStarted >= RobotConfig.INTAKE_SECONDS) {
                set(intake, 0);
                set(transfer, RobotConfig.TRANSFER_POWER);
                action = Action.TRANSFER;
                actionStarted = now;
            } else if (action == Action.TRANSFER
                    && now - actionStarted >= RobotConfig.TRANSFER_SECONDS) {
                stopOutputs();
                action = Action.OFF;
            } else if (action == Action.SHOOT
                    && now - actionStarted >= RobotConfig.SHOOT_SECONDS) {
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
            addMissing(missing, RobotConfig.OUTTAKE_LEFT, outtakeLeft);
            addMissing(missing, RobotConfig.OUTTAKE_RIGHT, outtakeRight);
            return missing.toString();
        }

        public void stop() {
            stopOutputs();
            action = Action.OFF;
        }

        private void startIntake(double now) {
            stopOutputs();
            set(intake, RobotConfig.INTAKE_POWER);
            action = Action.INTAKE;
            actionStarted = now;
        }

        private void startShoot(double now) {
            stopOutputs();
            set(outtakeLeft, RobotConfig.OUTTAKE_LEFT_POWER);
            set(outtakeRight, RobotConfig.OUTTAKE_RIGHT_POWER);
            action = Action.SHOOT;
            actionStarted = now;
        }

        private void stopOutputs() {
            set(intake, 0);
            set(transfer, 0);
            set(outtakeLeft, 0);
            set(outtakeRight, 0);
        }
    }

    private static DcMotorEx getMotor(HardwareMap map, String name) {
        try {
            return map.get(DcMotorEx.class, name);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static void configure(DcMotorEx motor, DcMotorSimple.Direction direction) {
        if (motor == null) return;
        motor.setDirection(direction);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setPower(0);
    }

    private static void set(DcMotorEx motor, double power) {
        if (motor != null) motor.setPower(Math.max(-1.0, Math.min(1.0, power)));
    }

    private static void addMissing(StringBuilder out, String name, DcMotorEx motor) {
        if (motor == null) {
            if (out.length() > 0) out.append(", ");
            out.append(name);
        }
    }
}
