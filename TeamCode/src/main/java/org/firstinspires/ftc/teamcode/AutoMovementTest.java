package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name = "Auto - Forward Test", group = "BIOBUZZ Tests")
public class AutoMovementTest extends LinearOpMode {
    @Override
    public void runOpMode() {
        RobotHardware robot = new RobotHardware(hardwareMap);

        telemetry.addData("Drive", robot.driveReady() ? "ready" : "MISSING MOTORS");
        if (!robot.driveReady()) telemetry.addData("Missing", robot.missingDriveMotors());
        telemetry.addData("Power", RobotConfig.AUTO_FORWARD_POWER);
        telemetry.addData("Time", RobotConfig.AUTO_FORWARD_TIME_MS + " ms");
        telemetry.update();

        try {
            waitForStart();
            if (!opModeIsActive() || !robot.driveReady()) return;

            robot.drive(RobotConfig.AUTO_FORWARD_POWER, 0, 0);
            sleep(RobotConfig.AUTO_FORWARD_TIME_MS);
        } finally {
            robot.stopDrive();
        }
    }
}
