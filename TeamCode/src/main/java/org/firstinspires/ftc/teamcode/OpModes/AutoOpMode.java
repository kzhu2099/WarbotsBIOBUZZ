package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.AutonomousController;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.StartPosition;

/** Competition autonomous wrapper. AutonomousController handles the state machine. */
@Autonomous(name = "Auto (Dynamic)")
public class AutoOpMode extends OpMode {

    private Robot robot;
    private AutonomousController controller;

    private final StartPosition[] startOptions = StartPosition.values();
    private int startIndex = 0;

    @Override
    public void init() {
        robot = new Robot(hardwareMap, telemetry, gamepad1, gamepad2, false);
    }

    @Override
    public void init_loop() {
        robot.toggleAlliance();

        if (gamepad1.dpadRightWasPressed()) {
            startIndex = (startIndex + 1) % startOptions.length;
        } else if (gamepad1.dpadLeftWasPressed()) {
            startIndex = (startIndex - 1 + startOptions.length) % startOptions.length;
        }

        StartPosition start = startOptions[startIndex];
        robot.setStartPose(start.pose(robot.redAlliance));
        telemetry.addData("Start option (dpad)", start);
        telemetry.addData("Start pose", robot.follower.pose());
        robot.updateTelemetry();
    }

    @Override
    public void start() {
        StartPosition startPosition = startOptions[startIndex];
        robot.setStartPose(startPosition.pose(robot.redAlliance));
        controller = new AutonomousController(robot, startPosition);
    }

    @Override
    public void loop() {
        controller.update(getRuntime());
        controller.telemetry(telemetry);
    }

    @Override
    public void stop() {
        robot.saveForTeleOp();
        robot.stop();
    }
}
