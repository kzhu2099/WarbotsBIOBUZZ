package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Intake -> transfer -> outtake flywheel, plus the shot-angle servo.
 * TeleOp feeds only when the flywheel is at speed. Missing parts are skipped,
 * so this runs on the drive-only robot too.
 */
public final class Mechanisms {
    private final DcMotorEx intake, transfer, outtake;
    private final Servo angleServo;

    private double targetRpm = 0;
    private double angle = -1; // -1 = the servo has not been told to move yet

    public Mechanisms(HardwareMap hardwareMap) {
        intake = Find.motor(hardwareMap, Settings.INTAKE, Settings.INTAKE_DIR);
        transfer = Find.motor(hardwareMap, Settings.TRANSFER, Settings.TRANSFER_DIR);
        outtake = Find.motor(hardwareMap, Settings.OUTTAKE, Settings.OUTTAKE_DIR);
        angleServo = Find.servo(hardwareMap, Settings.ANGLE_SERVO);

        // Velocity control; coast when stopped.
        if (outtake != null) {
            outtake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            outtake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        }
        // Servo is not commanded until a shot is chosen.
    }

    // ---------- intake and transfer ----------

    /** +1 pulls balls in, -1 spits them out, 0 stops. */
    public void setIntake(double direction) {
        if (intake != null) intake.setPower(direction * Settings.INTAKE_POWER);
    }

    /** +1 carries balls toward the flywheel, -1 backs them out, 0 stops. */
    public void setTransfer(double direction) {
        if (transfer != null) transfer.setPower(direction * Settings.TRANSFER_POWER);
    }

    /** Push balls into the flywheel, but only once it is up to speed. Returns true if feeding. */
    public boolean feedIfReady() {
        boolean ready = isReady();
        setTransfer(ready ? 1 : 0);
        setIntake(ready ? 1 : 0);
        return ready;
    }

    // ---------- flywheel ----------

    public void spinUp(double rpm) {
        targetRpm = rpm;
        if (outtake != null) outtake.setVelocity(rpm * Settings.OUTTAKE_TICKS_PER_REV / 60.0);
    }

    public double rpm() {
        if (outtake == null) return 0;
        return outtake.getVelocity() * 60.0 / Settings.OUTTAKE_TICKS_PER_REV;
    }

    public double targetRpm() {
        return targetRpm;
    }

    /** True when the flywheel is spinning within OUTTAKE_READY_RPM of its target. */
    public boolean isReady() {
        return outtake != null && targetRpm > 0
                && Math.abs(rpm() - targetRpm) < Settings.OUTTAKE_READY_RPM;
    }

    // ---------- shot angle ----------

    /** Moves the angle servo, kept inside the safe limits in Settings. */
    public void setAngle(double position) {
        angle = Math.max(Settings.ANGLE_LOWEST, Math.min(Settings.ANGLE_HIGHEST, position));
        if (angleServo != null) angleServo.setPosition(angle);
    }

    /** Move the angle a little. The first nudge starts from the middle of the safe limits. */
    public void nudgeAngle(double change) {
        double from = angle < 0 ? (Settings.ANGLE_LOWEST + Settings.ANGLE_HIGHEST) / 2 : angle;
        setAngle(from + change);
    }

    /** The servo position, or -1 if it has not been told to move yet. */
    public double angle() {
        return angle;
    }

    /** Set up for a shot: angle first, then start the flywheel. */
    public void aimFor(Spots.Shot shot) {
        setAngle(shot.angle);
        spinUp(shot.rpm);
    }

    // ---------- everything ----------

    public void stopAll() {
        setIntake(0);
        setTransfer(0);
        targetRpm = 0;
        if (outtake != null) outtake.setPower(0);
    }

    public boolean hasShooter() {
        return outtake != null;
    }

    /** "" when every named part was found, otherwise the missing names. */
    public String missing() {
        StringBuilder missing = new StringBuilder();
        Find.noteIfMissing(missing, Settings.INTAKE, intake);
        Find.noteIfMissing(missing, Settings.TRANSFER, transfer);
        Find.noteIfMissing(missing, Settings.OUTTAKE, outtake);
        Find.noteIfMissing(missing, Settings.ANGLE_SERVO, angleServo);
        return missing.toString();
    }
}
