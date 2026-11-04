package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.StringJoiner;

public final class RobotHardware {
    private final DcMotorEx fl;
    private final DcMotorEx fr;
    private final DcMotorEx bl;
    private final DcMotorEx br;

    public RobotHardware(HardwareMap map) {
        fl = setup(map, Constants.FRONT_LEFT_NAME, Constants.FRONT_LEFT_DIRECTION);
        fr = setup(map, Constants.FRONT_RIGHT_NAME, Constants.FRONT_RIGHT_DIRECTION);
        bl = setup(map, Constants.BACK_LEFT_NAME, Constants.BACK_LEFT_DIRECTION);
        br = setup(map, Constants.BACK_RIGHT_NAME, Constants.BACK_RIGHT_DIRECTION);
    }

    public boolean driveReady() {
        return fl != null && fr != null && bl != null && br != null;
    }

    public String missingDriveMotors() {
        StringJoiner missing = new StringJoiner(", ");
        addMissing(missing, Constants.FRONT_LEFT_NAME, fl);
        addMissing(missing, Constants.FRONT_RIGHT_NAME, fr);
        addMissing(missing, Constants.BACK_LEFT_NAME, bl);
        addMissing(missing, Constants.BACK_RIGHT_NAME, br);
        return missing.toString();
    }

    // Robot-centric mecanum: forward, strafe, turn.
    public void drive(double forward, double strafe, double turn) {
        if (!driveReady()) {
            stopDrive();
            return;
        }

        double leftFront = forward - strafe - turn;
        double rightFront = forward + strafe + turn;
        double leftBack = forward + strafe - turn;
        double rightBack = forward - strafe + turn;
        double scale = Math.max(1.0, Math.max(
                Math.max(Math.abs(leftFront), Math.abs(rightFront)),
                Math.max(Math.abs(leftBack), Math.abs(rightBack))));

        fl.setPower(leftFront / scale);
        fr.setPower(rightFront / scale);
        bl.setPower(leftBack / scale);
        br.setPower(rightBack / scale);
    }

    public void stopDrive() {
        stopMotor(fl);
        stopMotor(fr);
        stopMotor(bl);
        stopMotor(br);
    }

    public static final class Mechanisms {
        private enum Action { OFF, INTAKE, TRANSFER, SHOOT, MANUAL }

        private final DcMotorEx intake;
        private final DcMotorEx transfer;
        private final DcMotorEx ol;
        private final DcMotorEx or;
        private final boolean shootingEnabled;

        private Action action = Action.OFF;
        private double startedAt;
        private boolean lastIntake;
        private boolean lastShoot;

        public Mechanisms(HardwareMap map, boolean shootingEnabled) {
            this.shootingEnabled = shootingEnabled;
            intake = setup(map, RobotConfig.INTAKE, RobotConfig.INTAKE_DIRECTION);
            transfer = RobotConfig.transferEnabled()
                    ? setup(map, RobotConfig.TRANSFER, RobotConfig.TRANSFER_DIRECTION) : null;
            ol = setup(map, RobotConfig.OL, RobotConfig.OL_DIRECTION);
            or = setup(map, RobotConfig.OR, RobotConfig.OR_DIRECTION);
        }

        public void update(double now, Gamepad gamepad) {
            boolean intakePressed = gamepad.left_bumper && !lastIntake;
            boolean shootPressed = gamepad.right_bumper && !lastShoot;
            lastIntake = gamepad.left_bumper;
            lastShoot = gamepad.right_bumper;

            if (gamepad.back) {
                action = Action.MANUAL;
                set(intake, -gamepad.left_stick_y * RobotConfig.INTAKE_POWER);
                set(transfer, -gamepad.right_stick_y * RobotConfig.TRANSFER_POWER);
                if (shootingEnabled) {
                    set(ol, gamepad.left_trigger * RobotConfig.OL_POWER);
                    set(or, gamepad.right_trigger * RobotConfig.OR_POWER);
                }
                return;
            }

            if (action == Action.MANUAL) {
                stopMechanisms();
                action = Action.OFF;
            }

            // A new shot interrupts intake. A second press does not restart an active shot.
            if (shootingEnabled && shootPressed && action != Action.SHOOT) {
                stopMechanisms();
                action = Action.OFF;
                if (ol != null && or != null) {
                    set(ol, RobotConfig.OL_POWER);
                    set(or, RobotConfig.OR_POWER);
                    action = Action.SHOOT;
                    startedAt = now;
                }
            } else if (intakePressed && action == Action.OFF) {
                stopMechanisms();
                if (intake != null && (!RobotConfig.transferEnabled() || transfer != null)) {
                    set(intake, RobotConfig.INTAKE_POWER);
                    action = Action.INTAKE;
                    startedAt = now;
                }
            }

            if (action == Action.INTAKE && now - startedAt >= RobotConfig.INTAKE_SECONDS) {
                set(intake, 0);
                if (transfer != null) {
                    set(transfer, RobotConfig.TRANSFER_POWER);
                    action = Action.TRANSFER;
                    startedAt = now;
                } else {
                    action = Action.OFF;
                }
            } else if (action == Action.TRANSFER
                    && now - startedAt >= RobotConfig.TRANSFER_SECONDS) {
                set(transfer, 0);
                action = Action.OFF;
            } else if (action == Action.SHOOT
                    && now - startedAt >= RobotConfig.SHOOT_SECONDS) {
                set(ol, 0);
                set(or, 0);
                action = Action.OFF;
            }
        }

        public String action() {
            return action.name();
        }

        public String missingMotors() {
            StringJoiner missing = new StringJoiner(", ");
            addMissing(missing, RobotConfig.INTAKE, intake);
            if (RobotConfig.transferEnabled()) addMissing(missing, RobotConfig.TRANSFER, transfer);
            if (shootingEnabled) {
                addMissing(missing, RobotConfig.OL, ol);
                addMissing(missing, RobotConfig.OR, or);
            }
            return missing.toString();
        }

        public String missingOuttakeMotors() {
            StringJoiner missing = new StringJoiner(", ");
            addMissing(missing, RobotConfig.OL, ol);
            addMissing(missing, RobotConfig.OR, or);
            return missing.toString();
        }

        public void stop() {
            stopMechanisms();
            action = Action.OFF;
        }

        private void stopMechanisms() {
            stopMotor(intake);
            stopMotor(transfer);
            stopMotor(ol);
            stopMotor(or);
        }
    }

    private static DcMotorEx setup(HardwareMap map, String name, DcMotorSimple.Direction direction) {
        if (name == null || name.trim().isEmpty()) return null;
        try {
            DcMotorEx motor = map.get(DcMotorEx.class, name);
            motor.setPower(0);
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            motor.setDirection(direction);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            return motor;
        } catch (RuntimeException e) {
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
            // Try to stop the remaining motors too.
        }
    }

    private static void addMissing(StringJoiner missing, String name, DcMotorEx motor) {
        if (motor == null) missing.add(name);
    }
}
