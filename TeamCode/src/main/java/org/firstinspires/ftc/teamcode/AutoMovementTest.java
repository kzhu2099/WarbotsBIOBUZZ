package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name = "Auto - Forward Test", group = "BIOBUZZ Tests")
public class AutoMovementTest extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        RobotHardware robot = new RobotHardware(hardwareMap);

        telemetry.addData("Drive", robot.driveReady() ? "ready" : "missing motors");
        if (!robot.missingDriveMotors().isEmpty()) {
            telemetry.addData("Missing", robot.missingDriveMotors());
        }
        telemetry.addData("Power", RobotConfig.AUTO_FORWARD_POWER);
        telemetry.addData("Seconds", RobotConfig.AUTO_FORWARD_SECONDS);
        telemetry.addLine("This test only drives forward. It does not run a scoring route.");
        telemetry.update();

        try {
            waitForStart();
            if (!opModeIsActive() || !robot.driveReady()) return;

            double endTime = getRuntime() + RobotConfig.AUTO_FORWARD_SECONDS;
            while (opModeIsActive() && getRuntime() < endTime) {
                robot.drive(RobotConfig.AUTO_FORWARD_POWER, 0, 0, 1.0);
                idle();
            }
        } finally {
            robot.stopDrive();
        }
    }
}
