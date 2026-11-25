package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

/** Robot-centric mecanum drive without Pedro (TeleOp 1, Motor Check). */
public final class Drive {
    private final DcMotorEx frontLeft, frontRight, backLeft, backRight;

    public Drive(HardwareMap hardwareMap) {
        frontLeft = Find.motor(hardwareMap, Settings.FRONT_LEFT, Settings.FRONT_LEFT_DIR);
        frontRight = Find.motor(hardwareMap, Settings.FRONT_RIGHT, Settings.FRONT_RIGHT_DIR);
        backLeft = Find.motor(hardwareMap, Settings.BACK_LEFT, Settings.BACK_LEFT_DIR);
        backRight = Find.motor(hardwareMap, Settings.BACK_RIGHT, Settings.BACK_RIGHT_DIR);
    }

    /** Inputs -1..1. Positive = forward, strafe right, turn right (clockwise). gm0 mecanum mixing. */
    public void drive(double forward, double strafeRight, double turnRight) {
        double fl = forward + strafeRight + turnRight;
        double fr = forward - strafeRight - turnRight;
        double bl = forward - strafeRight + turnRight;
        double br = forward + strafeRight - turnRight;

        // Scale down together so direction is kept when a wheel exceeds 1.
        double biggest = Math.max(1, Math.max(Math.max(Math.abs(fl), Math.abs(fr)),
                Math.max(Math.abs(bl), Math.abs(br))));
        setPowers(fl / biggest, fr / biggest, bl / biggest, br / biggest);
    }

    /** Used by the Motor Check to spin wheels one at a time. */
    public void setPowers(double fl, double fr, double bl, double br) {
        if (frontLeft != null) frontLeft.setPower(fl);
        if (frontRight != null) frontRight.setPower(fr);
        if (backLeft != null) backLeft.setPower(bl);
        if (backRight != null) backRight.setPower(br);
    }

    public void stop() {
        setPowers(0, 0, 0, 0);
    }

    /** "" when all four wheels were found, otherwise the missing names. */
    public String missing() {
        StringBuilder missing = new StringBuilder();
        Find.noteIfMissing(missing, Settings.FRONT_LEFT, frontLeft);
        Find.noteIfMissing(missing, Settings.FRONT_RIGHT, frontRight);
        Find.noteIfMissing(missing, Settings.BACK_LEFT, backLeft);
        Find.noteIfMissing(missing, Settings.BACK_RIGHT, backRight);
        return missing.toString();
    }
}
