package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

/** Low-power, timed movement test. No localization, mechanisms, camera, or paths. */
@Autonomous(name = "Auto - Movement Test", group = "BIOBUZZ Tests")
public class AutoMovementTest extends OpMode {
    private static final class Step {
        final String name;
        final double forward;
        final double strafe;
        final double turn;
        final double seconds;

        Step(String name, double forward, double strafe, double turn, double seconds) {
            this.name = name;
            this.forward = forward;
            this.strafe = strafe;
            this.turn = turn;
            this.seconds = seconds;
        }
    }

    private final Step[] steps = {
            new Step("Forward", RobotConfig.AUTO_TEST_POWER, 0, 0, RobotConfig.AUTO_TEST_MOVE_SECONDS),
            new Step("Stop", 0, 0, 0, RobotConfig.AUTO_TEST_PAUSE_SECONDS),
            new Step("Right", 0, RobotConfig.AUTO_TEST_POWER, 0, RobotConfig.AUTO_TEST_MOVE_SECONDS),
            new Step("Stop", 0, 0, 0, RobotConfig.AUTO_TEST_PAUSE_SECONDS),
            new Step("Backward", -RobotConfig.AUTO_TEST_POWER, 0, 0, RobotConfig.AUTO_TEST_MOVE_SECONDS),
            new Step("Stop", 0, 0, 0, RobotConfig.AUTO_TEST_PAUSE_SECONDS)
    };

    private RobotHardware robot;
    private int stepIndex;
    private double stepStartedAt;
    private boolean done;

    @Override
    public void init() {
        robot = new RobotHardware(hardwareMap, false);
    }

    @Override
    public void start() {
        stepIndex = 0;
        stepStartedAt = getRuntime();
        done = false;
    }

    @Override
    public void loop() {
        if (!robot.driveReady()) {
            robot.stopDrive();
            telemetry.addLine("AUTO STOPPED: drive motors missing");
            telemetry.addData("Missing", robot.missingDriveMotors());
            telemetry.update();
            return;
        }

        if (done) {
            robot.stopDrive();
            telemetry.addLine("Movement test complete");
            telemetry.update();
            return;
        }

        Step step = steps[stepIndex];
        double elapsed = getRuntime() - stepStartedAt;

        if (elapsed < step.seconds) {
            robot.drive(step.forward, step.strafe, step.turn, 1.0);
        } else {
            robot.stopDrive();
            stepIndex++;
            stepStartedAt = getRuntime();
            if (stepIndex >= steps.length) done = true;
        }

        telemetry.addData("Step", step.name);
        telemetry.addData("Step number", Math.min(stepIndex + 1, steps.length) + " / " + steps.length);
        telemetry.addData("Power", "%.2f / %.2f / %.2f",
                step.forward, step.strafe, step.turn);
        telemetry.addLine("Run with robot raised for the first test.");
        telemetry.update();
    }

    @Override
    public void stop() {
        if (robot != null) robot.stopDrive();
    }
}
