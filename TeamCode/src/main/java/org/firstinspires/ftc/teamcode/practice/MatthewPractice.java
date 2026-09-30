package org.firstinspires.ftc.teamcode.practice;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.pedropathing.follower.Follower;

// AUDITED, UNCHANGED: same reasoning as AutoTemplatePractice.java.
@Disabled
@TeleOp(name = "Matthew TeleOp Practice")
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

        float gamepadJoystick1Y = -this.gamepad1.left_stick_y;
        boolean gamepadButtonB = this.gamepad1.b;

        if (gamepadButtonB) {
            gamepadJoystick1Y /= 0.5f;
        }
        telemetry.addData("Button B Pressed (slow)", gamepadButtonB);
        telemetry.addData("Joystick 1 Power", gamepadJoystick1Y);
        telemetry.update();

        frontLeft.setPower(gamepadJoystick1Y);
        frontRight.setPower(gamepadJoystick1Y);

        backLeft.setPower(gamepadJoystick1Y);
        backRight.setPower(gamepadJoystick1Y);



        // HINT: Implement your control change logic here.
        // IF a specific button (like gamepad1.y) is pressed:
        //      Tell the PedroPathing follower to hold a specific heading or drive to a point.
        // ELSE:
        //      Apply standard mecanum joystick math to the motor powers.

        // HINT: If PedroPathing is actively controlling the robot, remember to call follower.update()!
    }

    @Override
    public void stop() {

        frontLeft.setPower(0f);
        frontRight.setPower(0f);
        backLeft.setPower(0f);
        backRight.setPower(0f);


        // HINT: Safely stop all drivetrain motors here by setting powers to 0.
    }
}
