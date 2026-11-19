package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

/**
 * Same preset route and controls as PathTestAuto, but runs the two outtake
 * motors at the configured RPM at each shooting position.
 */
@Autonomous(name = "AUTO - Two Lane Flower Cycle", group = "BIOBUZZ")
public class PathShootAuto extends BioBuzzPathAuto {
    private DcMotorEx leftOuttake;
    private DcMotorEx rightOuttake;

    @Override
    protected void initializeScoringHardware() {
        leftOuttake = hardwareMap.get(DcMotorEx.class, RobotConfig.LEFT_OUTTAKE_MOTOR_NAME);
        rightOuttake = hardwareMap.get(DcMotorEx.class, RobotConfig.RIGHT_OUTTAKE_MOTOR_NAME);

        leftOuttake.setDirection(RobotConfig.LEFT_OUTTAKE_MOTOR_DIRECTION);
        rightOuttake.setDirection(RobotConfig.RIGHT_OUTTAKE_MOTOR_DIRECTION);

        leftOuttake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightOuttake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        leftOuttake.setPower(0);
        rightOuttake.setPower(0);
        leftOuttake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightOuttake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    @Override
    protected void beginShooting() {
        double leftTicksPerSecond =
                RobotConfig.AUTO_OUTTAKE_RPM * leftOuttake.getMotorType().getTicksPerRev() / 60.0;
        double rightTicksPerSecond =
                RobotConfig.AUTO_OUTTAKE_RPM * rightOuttake.getMotorType().getTicksPerRev() / 60.0;

        leftOuttake.setVelocity(leftTicksPerSecond);
        rightOuttake.setVelocity(rightTicksPerSecond);
    }

    @Override
    protected void endShooting() {
        if (leftOuttake != null) {
            leftOuttake.setVelocity(0);
            leftOuttake.setPower(0);
        }
        if (rightOuttake != null) {
            rightOuttake.setVelocity(0);
            rightOuttake.setPower(0);
        }
    }

    @Override
    protected String shootingMode() {
        return "REAL OUTTAKES - " + RobotConfig.AUTO_OUTTAKE_RPM + " RPM";
    }
}
