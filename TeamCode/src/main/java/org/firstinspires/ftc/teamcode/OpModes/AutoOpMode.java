package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.AutonomousController;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.StartPosition;
import org.firstinspires.ftc.teamcode.pedro.Constants;

@Autonomous(name = "Auto (Dynamic)")
public class AutoOpMode extends OpMode {

    private Robot robot;
    private AutonomousController controller;
    private String initError;

    private final StartPosition[] startOptions = StartPosition.values();
    private int startIndex = 0;

    @Override
    public void init() {
        if (Constants.foresightConfig == null) {
            initError = "Foresight is not tuned. Run the Pedro tuners before using autonomous.";
            return;
        }

        try {
            robot = new Robot(hardwareMap, telemetry, gamepad1, gamepad2, false);
        } catch (RuntimeException e) {
            initError = "Robot setup failed: " + e.getMessage();
        }
    }

    @Override
    public void init_loop() {
        if (robot == null) {
            showInitError();
            return;
        }

        robot.toggleAlliance();

        if (gamepad1.dpadRightWasPressed()) {
            startIndex = (startIndex + 1) % startOptions.length;
        } else if (gamepad1.dpadLeftWasPressed()) {
            startIndex = (startIndex - 1 + startOptions.length) % startOptions.length;
        }

        StartPosition start = startOptions[startIndex];
        robot.setStartPose(start.pose(robot.redAlliance));
        robot.updateTelemetry();

        telemetry.addData("Start", start);
        telemetry.addData("Starting pose", robot.follower.pose());
        telemetry.update();
    }

    @Override
    public void start() {
        if (robot == null) return;

        StartPosition start = startOptions[startIndex];
        robot.setStartPose(start.pose(robot.redAlliance));
        controller = new AutonomousController(robot, start);
    }

    @Override
    public void loop() {
        if (robot == null || controller == null) {
            showInitError();
            return;
        }

        controller.update(getRuntime());
        controller.telemetry(telemetry);
    }

    @Override
    public void stop() {
        if (robot == null) return;

        robot.saveForTeleOp();
        robot.stop();
    }

    private void showInitError() {
        telemetry.addLine("AUTO NOT READY");
        telemetry.addLine(initError == null ? "Robot setup did not finish." : initError);
        telemetry.update();
    }
}
