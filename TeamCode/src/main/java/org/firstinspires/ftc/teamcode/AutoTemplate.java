package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import static com.pedropathing.api.Paths.line;

@Autonomous(name = "Autonomous")
public class AutoTemplate extends OpMode {

    private Robot robot;

    private Path driveForward;

    @Override
    public void init() {
        robot = new Robot(hardwareMap, telemetry, gamepad1, gamepad2, false);

        Pose startPose = Pose.zero(); // TODO: set to this auto's real starting pose
        robot.follower.setPose(startPose);

        driveForward = line(startPose, new Pose(startPose.x() + 24, startPose.y()))
                .constant(startPose.heading());
    }

    @Override
    public void init_loop() {
        if (gamepad1.xWasPressed()) {
            robot.redAlliance = !robot.redAlliance;
        }

        telemetry.addData("alliance (x to toggle)", robot.redAlliance ? "RED" : "BLUE");
        telemetry.update();
    }

    @Override
    public void start() {
        robot.start();
        robot.follower.follow(driveForward);
    }

    @Override
    public void loop() {
        robot.follower.update();
        robot.updateTelemetry();

        // If TeleOp needs to pick up from here, stash the pose somewhere
        // shared once the follower reaches the end, e.g.:
        // if (robot.follower.atParametricEnd()) AutoToTeleOpStorage.endPose = robot.follower.pose();
    }

    @Override
    public void stop() {
        robot.stop();
    }
}