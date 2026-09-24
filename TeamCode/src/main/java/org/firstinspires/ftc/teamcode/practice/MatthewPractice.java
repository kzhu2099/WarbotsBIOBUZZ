package org.firstinspires.ftc.teamcode.practice;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.pedropathing.follower.Follower;

// AUDITED, UNCHANGED: same reasoning as AutoTemplatePractice.java.
@Disabled
@TeleOp(name = "TeleOp Practice")
public class MatthewPractice extends OpMode {

    public DcMotorEx frontLeft;
    public DcMotorEx frontRight;
    public DcMotorEx backLeft;
    public DcMotorEx backRight;

    public Follower follower;

    @Override
    public void init() {
        frontLeft = hardwareMap.get(DcMotorEx.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotorEx.class, "frontRight");
        backLeft = hardwareMap.get(DcMotorEx.class, "backLeft");
        backRight = hardwareMap.get(DcMotorEx.class, "backRight");

        // HINT: Initialize the PedroPathing Follower here.
    }

    @Override
    public void init_loop() {
        // HINT: Update telemetry here to confirm initialization before the driver hits PLAY.
    }

    @Override
    public void start() {
        // HINT: Code here runs exactly once when the match starts.
    }

    @Override
    public void loop() {
        // HINT: Implement your control change logic here.
        // IF a specific button (like gamepad1.y) is pressed:
        //      Tell the PedroPathing follower to hold a specific heading or drive to a point.
        // ELSE:
        //      Apply standard mecanum joystick math to the motor powers.

        // HINT: If PedroPathing is actively controlling the robot, remember to call follower.update()!
    }

    @Override
    public void stop() {
        // HINT: Safely stop all drivetrain motors here by setting powers to 0.
    }
}
