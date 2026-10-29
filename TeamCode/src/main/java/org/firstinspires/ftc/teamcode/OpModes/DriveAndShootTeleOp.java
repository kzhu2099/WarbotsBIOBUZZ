package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.ManualDriveAndShoot;

/**
 * Manual scoring TeleOp for robot bring-up or whenever vision, odometry, or
 * Foresight tuning is unavailable. Drive and mechanism controls still work
 * without those optional systems. Missing motors are listed in telemetry.
 */
@TeleOp(name = "Drive + Shoot (No Camera)")
public class DriveAndShootTeleOp extends OpMode {

    private ManualDriveAndShoot controls;

    @Override
    public void init() {
        controls = new ManualDriveAndShoot(hardwareMap, telemetry);
        telemetry.addLine("Manual drive + shoot. Cameras, Pinpoint, and Foresight are not used.");
        controls.addTelemetry();
        telemetry.update();
    }

    @Override
    public void init_loop() {
        controls.addTelemetry();
        telemetry.update();
    }

    @Override
    public void loop() {
        controls.update(getRuntime(), gamepad1, gamepad2);
    }

    @Override
    public void stop() {
        if (controls != null) {
            controls.stop();
        }
    }
}
