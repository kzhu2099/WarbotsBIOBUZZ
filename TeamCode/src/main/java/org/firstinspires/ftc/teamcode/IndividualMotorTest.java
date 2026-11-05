package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.ArrayList;
import java.util.List;

@TeleOp(name = "TEST - Individual Motor", group = "BIOBUZZ Tests")
public class IndividualMotorTest extends OpMode {
    private static final class MotorChoice {
        final String physicalPosition;
        final String hardwareName;
        DcMotorEx motor;

        MotorChoice(String physicalPosition, String hardwareName) {
            this.physicalPosition = physicalPosition;
            this.hardwareName = hardwareName;
        }
    }

    private final List<MotorChoice> motors = new ArrayList<>();
    private int selectedMotor;
    private String error;

    @Override
    public void init() {
        addMotor("Front left wheel", Constants.FRONT_LEFT_NAME, Constants.FRONT_LEFT_DIRECTION);
        addMotor("Front right wheel", Constants.FRONT_RIGHT_NAME, Constants.FRONT_RIGHT_DIRECTION);
        addMotor("Back left wheel", Constants.BACK_LEFT_NAME, Constants.BACK_LEFT_DIRECTION);
        addMotor("Back right wheel", Constants.BACK_RIGHT_NAME, Constants.BACK_RIGHT_DIRECTION);
        addMotor("Intake", RobotConfig.INTAKE_MOTOR_NAME, RobotConfig.INTAKE_MOTOR_DIRECTION);
        if (RobotConfig.transferEnabled()) {
            addMotor("Transfer", RobotConfig.TRANSFER_MOTOR_NAME,
                    RobotConfig.TRANSFER_MOTOR_DIRECTION);
        }
        addMotor("Left outtake", RobotConfig.LEFT_OUTTAKE_MOTOR_NAME,
                RobotConfig.LEFT_OUTTAKE_MOTOR_DIRECTION);
        addMotor("Right outtake", RobotConfig.RIGHT_OUTTAKE_MOTOR_NAME,
                RobotConfig.RIGHT_OUTTAKE_MOTOR_DIRECTION);
    }

    @Override
    public void loop() {
        if (motors.isEmpty()) {
            telemetry.addLine("No motors are configured.");
            telemetry.update();
            return;
        }

        if (gamepad1.dpadRightWasPressed()) {
            selectedMotor = (selectedMotor + 1) % motors.size();
        }
        if (gamepad1.dpadLeftWasPressed()) {
            selectedMotor = (selectedMotor + motors.size() - 1) % motors.size();
        }

        stopAllMotors();

        MotorChoice selected = motors.get(selectedMotor);
        if (error == null && selected.motor != null && gamepad1.a) {
            try {
                double power = Math.max(-RobotConfig.MOTOR_TEST_MAX_POWER,
                        Math.min(RobotConfig.MOTOR_TEST_MAX_POWER, -gamepad1.left_stick_y));
                selected.motor.setPower(power);
            } catch (RuntimeException exception) {
                error = exception.getMessage() == null
                        ? exception.getClass().getSimpleName() : exception.getMessage();
                stopAllMotors();
            }
        }

        telemetry.addData("Selected motor", (selectedMotor + 1) + " of " + motors.size());
        telemetry.addData("Physical position", selected.physicalPosition);
        telemetry.addData("Driver Station name", selected.hardwareName);
        telemetry.addData("Status", selected.motor == null ? "NOT FOUND" : "ready");
        telemetry.addData("Power", gamepad1.a && selected.motor != null
                ? "enabled (max " + RobotConfig.MOTOR_TEST_MAX_POWER + ")" : "off");
        telemetry.addLine("D-pad left/right selects the motor.");
        telemetry.addLine("Hold A and move the left stick Y to test it.");
        telemetry.addLine("All other motors are commanded to zero.");
        if (error != null) telemetry.addData("ERROR", error);
        telemetry.update();
    }

    private void addMotor(
            String physicalPosition,
            String hardwareName,
            DcMotorSimple.Direction direction) {
        if (hardwareName == null || hardwareName.trim().isEmpty()) return;

        MotorChoice choice = new MotorChoice(physicalPosition, hardwareName);
        try {
            choice.motor = hardwareMap.get(DcMotorEx.class, hardwareName);
            choice.motor.setPower(0);
            choice.motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            choice.motor.setDirection(direction);
            choice.motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        } catch (RuntimeException ignored) {
            choice.motor = null;
        }
        motors.add(choice);
    }

    private void stopAllMotors() {
        for (MotorChoice choice : motors) {
            if (choice.motor == null) continue;

            try {
                choice.motor.setPower(0);
            } catch (RuntimeException ignored) {
                // Continue stopping the other motors.
            }
        }
    }

    @Override
    public void stop() {
        stopAllMotors();
    }
}
