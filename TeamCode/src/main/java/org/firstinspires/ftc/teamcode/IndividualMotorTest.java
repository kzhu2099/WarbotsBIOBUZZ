package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.pedro.Constants;

/** Select one motor, hold A, and move the left stick to test only that motor. */
@TeleOp(name = "Motor - Individual Test", group = "BIOBUZZ Tests")
public class IndividualMotorTest extends OpMode {
    private final String[] names = {
            Constants.FRONT_LEFT_NAME,
            Constants.FRONT_RIGHT_NAME,
            Constants.BACK_LEFT_NAME,
            Constants.BACK_RIGHT_NAME,
            RobotConfig.INTAKE,
            RobotConfig.TRANSFER,
            RobotConfig.OL,
            RobotConfig.OR
    };

    private final DcMotorSimple.Direction[] directions = {
            Constants.FRONT_LEFT_DIRECTION,
            Constants.FRONT_RIGHT_DIRECTION,
            Constants.BACK_LEFT_DIRECTION,
            Constants.BACK_RIGHT_DIRECTION,
            RobotConfig.INTAKE_DIRECTION,
            RobotConfig.TRANSFER_DIRECTION,
            RobotConfig.OL_DIRECTION,
            RobotConfig.OR_DIRECTION
    };

    private final DcMotorEx[] motors = new DcMotorEx[names.length];
    private int selected;
    private String error;

    @Override
    public void init() {
        for (int i = 0; i < names.length; i++) {
            if (names[i] == null || names[i].trim().isEmpty()) continue;
            try {
                motors[i] = hardwareMap.get(DcMotorEx.class, names[i]);
                motors[i].setPower(0);
                motors[i].setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                motors[i].setDirection(directions[i]);
                motors[i].setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            } catch (RuntimeException e) {
                motors[i] = null;
            }
        }
    }

    @Override
    public void loop() {
        if (gamepad1.dpadRightWasPressed()) selected = (selected + 1) % names.length;
        if (gamepad1.dpadLeftWasPressed()) selected = (selected + names.length - 1) % names.length;

        // Stop every motor first. Only the selected motor may be powered below.
        stopAll();

        DcMotorEx motor = motors[selected];
        if (error == null && motor != null && gamepad1.a) {
            try {
                motor.setPower(-Math.max(-1.0, Math.min(1.0, gamepad1.left_stick_y)));
            } catch (RuntimeException e) {
                error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                stopAll();
            }
        }

        telemetry.addData("Selected", names[selected].trim().isEmpty() ? "TRANSFER (disabled)" : names[selected]);
        telemetry.addData("Motor", motor == null
                ? (names[selected].trim().isEmpty() ? "disabled in RobotConfig" : "not found")
                : "ready");
        telemetry.addData("Power enabled", gamepad1.a && motor != null);
        telemetry.addLine("D-pad left/right: choose motor. Hold A + left stick Y: power it.");
        telemetry.addLine("All other motors are commanded to zero.");
        if (error != null) telemetry.addData("ERROR", error);
        telemetry.update();
    }

    private void stopAll() {
        for (DcMotorEx motor : motors) {
            if (motor == null) continue;
            try {
                motor.setPower(0);
            } catch (RuntimeException ignored) {
                // Continue trying to stop the other motors.
            }
        }
    }

    @Override
    public void stop() {
        stopAll();
    }
}
