package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/** Direct motor TeleOp. No Pedro, Pinpoint, or path-following code. */
@TeleOp(name = "TeleOp - Manual (No Pedro)", group = "BIOBUZZ Tests")
public class TeleOpManual extends OpMode {
    private RobotHardware robot;
    private AprilTagCamera camera;

    @Override
    public void init() {
        robot = new RobotHardware(hardwareMap, true);
        camera = new AprilTagCamera(hardwareMap);
    }

    @Override
    public void loop() {
        double scale = gamepad1.right_trigger > 0.1
                ? RobotConfig.DRIVE_PRECISION_SCALE : 1.0;
        robot.drive(
                -gamepad1.left_stick_y,
                gamepad1.left_stick_x,
                gamepad1.right_stick_x,
                scale
        );

        robot.mechanisms.update(getRuntime(), gamepad2);

        telemetry.addData("Drive", robot.driveReady() ? "manual" : "missing drive motors");
        String missingDrive = robot.missingDriveMotors();
        if (!missingDrive.isEmpty()) telemetry.addData("DRIVE WARNING", missingDrive);
        telemetry.addData("Mechanism", robot.mechanisms.state());
        String missingMechanism = robot.mechanisms.missingMotors();
        telemetry.addData(missingMechanism.isEmpty() ? "Mechanism motors" : "MECHANISM WARNING",
                missingMechanism.isEmpty() ? "all found" : "missing: " + missingMechanism);
        camera.addTelemetry(telemetry);
        telemetry.update();
    }

    @Override
    public void stop() {
        if (robot != null) robot.stop();
        if (camera != null) camera.close();
    }
}
