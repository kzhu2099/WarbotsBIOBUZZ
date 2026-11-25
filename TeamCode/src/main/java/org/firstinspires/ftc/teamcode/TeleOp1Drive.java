package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * Robot-centric drive with no odometry. Backup when Pinpoint is down.
 * gp1: left stick = drive, right stick X = turn, LB = slow.
 */
@TeleOp(name = "1 Drive - no odometry", group = "Match")
public class TeleOp1Drive extends OpMode {
    private Drive drive;

    @Override
    public void init() {
        drive = new Drive(hardwareMap);
    }

    @Override
    public void init_loop() {
        telemetry.addData("Drive motors missing", AutoBase.show(drive.missing()));
        telemetry.update();
    }

    @Override
    public void loop() {
        double speed = gamepad1.left_bumper ? Settings.SLOW_MODE_POWER : 1.0;

        double forward = -Find.stick(gamepad1.left_stick_y);
        double strafeRight = Find.stick(gamepad1.left_stick_x);
        double turnRight = Find.stick(gamepad1.right_stick_x);
        drive.drive(forward * speed, strafeRight * speed, turnRight * speed);

        telemetry.addData("Forward / strafe / turn", "%.2f  %.2f  %.2f", forward, strafeRight, turnRight);
        telemetry.addData("Slow mode", gamepad1.left_bumper ? "ON" : "off");
        telemetry.addData("Drive motors missing", AutoBase.show(drive.missing()));
        telemetry.update();
    }

    @Override
    public void stop() {
        drive.stop();
    }
}
