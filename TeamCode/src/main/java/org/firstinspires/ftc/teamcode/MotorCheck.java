package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * One motor at a time, robot on a block. gp1:
 *   LB / RB = front-left / front-right wheel, LT / RT = back-left / back-right wheel
 *   (each must spin the robot forward; fix directions in Settings).
 *   dpad up / right / down = intake / transfer / flywheel, hold X to reverse.
 *   Y / A = angle servo up / down one step (within the safe limits).
 */
@TeleOp(name = "Motor Check", group = "Practice")
public class MotorCheck extends OpMode {
    private static final double TEST_POWER = 0.30;

    private Drive drive;
    private Mechanisms mechanisms;

    @Override
    public void init() {
        drive = new Drive(hardwareMap);
        mechanisms = new Mechanisms(hardwareMap);
    }

    @Override
    public void init_loop() {
        telemetry.addData("Drive motors missing", AutoBase.show(drive.missing()));
        telemetry.addData("Mechanisms missing", AutoBase.show(mechanisms.missing()));
        telemetry.update();
    }

    @Override
    public void loop() {
        drive.setPowers(
                gamepad1.left_bumper ? TEST_POWER : 0,
                gamepad1.right_bumper ? TEST_POWER : 0,
                gamepad1.left_trigger > 0.5 ? TEST_POWER : 0,
                gamepad1.right_trigger > 0.5 ? TEST_POWER : 0);

        double direction = gamepad1.x ? -1 : 1;
        mechanisms.setIntake(gamepad1.dpad_up ? TEST_POWER * direction : 0);
        mechanisms.setTransfer(gamepad1.dpad_right ? TEST_POWER * direction : 0);
        if (gamepad1.dpad_down) {
            mechanisms.spinUp(1000 * direction);
        } else if (mechanisms.targetRpm() != 0) {
            mechanisms.stopAll();
        }

        if (gamepad1.yWasPressed()) mechanisms.nudgeAngle(Settings.ANGLE_STEP);
        if (gamepad1.aWasPressed()) mechanisms.nudgeAngle(-Settings.ANGLE_STEP);

        telemetry.addData("Flywheel", "%.0f rpm (target %.0f)", mechanisms.rpm(), mechanisms.targetRpm());
        telemetry.addData("Angle servo", mechanisms.angle() < 0 ? "not moved yet (Y/A)"
                : String.format(java.util.Locale.US, "%.2f", mechanisms.angle()));
        telemetry.addData("Drive motors missing", AutoBase.show(drive.missing()));
        telemetry.addData("Mechanisms missing", AutoBase.show(mechanisms.missing()));
        telemetry.update();
    }

    @Override
    public void stop() {
        drive.stop();
        mechanisms.stopAll();
    }
}
