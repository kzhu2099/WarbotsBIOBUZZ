package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

/** Low-power, timed movement test. No localization, mechanisms, or path following. */
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

    private final RobotConfig.AutoPlan plan = new RobotConfig.AutoPlan();
    private RobotHardware robot;
    private AprilTagCamera camera;
    private int stepIndex;
    private double stepStartedAt;
    private boolean done;

    @Override
    public void init() {
        robot = new RobotHardware(hardwareMap, false);
        camera = new AprilTagCamera(hardwareMap);
        showPlan();
    }

    @Override
    public void init_loop() {
        handlePlanSelection();
        updateHiveReading();
        showPlan();
    }

    @Override
    public void start() {
        plan.freezeInitialState();
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
            showPlan();
            telemetry.update();
            return;
        }

        if (done) {
            robot.stopDrive();
            telemetry.addLine("Movement test complete");
            showPlan();
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
        telemetry.addLine("Movement test only: no preload shots or Flower route are run.");
        showPlan();
        telemetry.update();
    }

    private void handlePlanSelection() {
        if (gamepad1.xWasPressed()) plan.toggleAlliance();
        if (gamepad1.dpadLeftWasPressed()) plan.cycleStart(-1);
        if (gamepad1.dpadRightWasPressed()) plan.cycleStart(1);
        if (gamepad1.dpadDownWasPressed()) plan.cycleFlower(-1);
        if (gamepad1.dpadUpWasPressed()) plan.cycleFlower(1);
        if (gamepad1.aWasPressed()) plan.toggleLane();
        if (gamepad1.bWasPressed()) plan.toggleManualHiveSide();
        if (gamepad1.yWasPressed()) plan.togglePreloadTip();
    }

    private void updateHiveReading() {
        if (camera == null) return;
        AprilTagCamera.HiveReading reading = camera.updateHiveState(plan.alliance);
        if (reading != null) {
            plan.setCameraReading(reading.upwardCell, reading.clusterName);
        } else if (!camera.isAvailable()) {
            plan.clearCameraReading();
        }
    }

    private void showPlan() {
        telemetry.addLine("AUTO PLAN PREVIEW");
        telemetry.addData("Alliance", plan.alliance);
        telemetry.addData("Start", plan.start);
        if (RobotConfig.geometryMeasured()) {
            telemetry.addData("Start pose (robot center)", plan.startPose());
        } else {
            telemetry.addLine("Robot width/length not measured: start pose unavailable");
        }
        telemetry.addData("Flower", plan.flower);
        telemetry.addData("Our lane", plan.lane);
        telemetry.addData("Partner lane", plan.partnerLane());
        telemetry.addData("Manual Hive fallback", plan.manualStartingHiveSide);
        telemetry.addData("Hive at start", plan.startingHiveSide());
        telemetry.addData("Hive source", plan.hiveStateSource());
        telemetry.addData("Preload tips Hive", plan.preloadExpectedToTip ? "YES" : "NO (assume unchanged)");
        telemetry.addData("Hive after preload", plan.hiveSideAfterPreload());
        telemetry.addData("Lane rule", plan.lane == RobotConfig.RouteLane.INSIDE_HIVE
                ? "Stay in our half; do not cross field middle"
                : "Go around the outside of the Hive frame");
        telemetry.addLine("INIT: X alliance | D-pad L/R start | D-pad U/D Flower");
        telemetry.addLine("A lane | B manual Hive side | Y expected preload tip");
        if (camera != null) camera.addTelemetry(telemetry);
    }

    @Override
    public void stop() {
        if (robot != null) robot.stopDrive();
        if (camera != null) camera.close();
    }
}
