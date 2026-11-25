package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

/** Hardware lookup that returns null for a blank or unconfigured name instead of crashing. */
public final class Find {
    private Find() {}

    public static DcMotorEx motor(HardwareMap hardwareMap, String name, DcMotorSimple.Direction direction) {
        if (name.isEmpty()) return null;
        try {
            DcMotorEx motor = hardwareMap.get(DcMotorEx.class, name);
            motor.setDirection(direction);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            motor.setPower(0);
            return motor;
        } catch (IllegalArgumentException notInConfiguration) {
            return null;
        }
    }

    public static Servo servo(HardwareMap hardwareMap, String name) {
        if (name.isEmpty()) return null;
        try {
            return hardwareMap.get(Servo.class, name);
        } catch (IllegalArgumentException notInConfiguration) {
            return null;
        }
    }

    /** Adds "name" to the list when the part was not found. */
    static void noteIfMissing(StringBuilder missing, String name, Object part) {
        if (part != null || name.isEmpty()) return;
        if (missing.length() > 0) missing.append(", ");
        missing.append(name);
    }

    /** Joystick value with a deadzone. */
    public static double stick(double value) {
        return Math.abs(value) < Settings.STICK_DEADZONE ? 0 : value;
    }
}
