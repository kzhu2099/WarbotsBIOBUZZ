package org.firstinspires.ftc.teamcode.practice;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.pedro.Constants;

// AUDITED, UNCHANGED: a from-scratch, no-Robot-class exercise (raw
// Follower only), unrelated to the Jar/Tipper/MemoryPalace terminology
// that needed fixing elsewhere in this project, so it needed no rewrite.
@Disabled
@Autonomous(name = "Matthew Auto Testing")
public class MatthewAutoTesting extends LinearOpMode {

    public Follower follower;
    public Path mainPath;
    private final PoseFactory poseFactory = PoseFactory.degrees();

    private Pose start;
    private Pose end;
    private Pose controlPoint1;
    private Pose controlPoint2;

    private DcMotorEx leftOutake;
    private DcMotorEx rightOutake;

    public Path initalizePath() throws RuntimeException {
        if (PathNumber == 1) {
            // red close to audience
            start = poseFactory.of(56, 8, 90);
            end = poseFactory.of(12, 91, 0);
            controlPoint1 = poseFactory.of(19, 52, 0);
            controlPoint2 = null;
            return Paths.curve(start, controlPoint1, end).linear(start, end);
        } else if (PathNumber == 2) {
            // red far from audience
            start = poseFactory.of(80.5, 8, 90);
            end = poseFactory.of(127.7, 19.75, 180);
            controlPoint1 = poseFactory.of(77.9, 34.5, 0);
            controlPoint2 = poseFactory.of(105.65, 10.8, 0);
            return Paths.curve(start, controlPoint1, controlPoint2, end).linear(start, end);
        } else if (PathNumber == 3) {
            // blue close to audience
            start = poseFactory.of(60, 133.5, 270);
            end = poseFactory.of(11.6374, 116.5716, 15);
            controlPoint1 = poseFactory.of(63.9876, 98.2467, 0);
            controlPoint2 = poseFactory.of(31.3502, 132.7199, 0);
            return Paths.curve(start, controlPoint1, controlPoint2, end).linear(start, end);
        } else if (PathNumber == 4) {
            // blue far from audience
            start = poseFactory.of(86, 133.5, 270);
            end = poseFactory.of(130.3939, 44.8529, 160);
            controlPoint1 = poseFactory.of(123.3333, 110.6289, 0);
            controlPoint2 = null;

            return Paths.curve(start, controlPoint1, end).linear(start, end);
        }

        throw new RuntimeException("Path Failed to Init");
    }

    public int PathNumber;
    public String PathName;

    @Override
    public void runOpMode() {
        // red close
        PathNumber = 1;
        PathName = "Red-CloseToAudience";

//        boolean attemptShooting = true;
//        boolean outtakeMotorCheck = true;
//
//        try {
//            leftOutake = hardwareMap.get(DcMotorEx.class, "outtakeLeft");
//            rightOutake = hardwareMap.get(DcMotorEx.class, "outtakeRight");
//        } catch(Exception e) {
//            attemptShooting = false;
//            outtakeMotorCheck = false;
//        }

        boolean initializedPathSuccess = false;

        follower = Constants.create(hardwareMap);

        try {
            mainPath = initalizePath();
            initializedPathSuccess = true;
        } catch (RuntimeException e) {
            initializedPathSuccess = false;
        }

//        telemetry.addData("Start Pos", true);
//        telemetry.addData("Outtake Motor Test Passed", outtakeMotorCheck);
//        telemetry.addData("Will attempt shooting (Gamepad1 X)", attemptShooting);
        telemetry.addData("PathName (Y to cycle)", PathName);
        telemetry.addData("PathNumber", PathNumber);
        telemetry.addData("Path Initialized", initializedPathSuccess);
        telemetry.update();

        if (gamepad1.yWasPressed()) {
            PathNumber++;
            if (PathNumber == 2) {
                PathName = "Red-FarFromAudience";
            } else if (PathNumber == 3) {
                PathName = "Blue-CloseToAudience";
            } else if (PathNumber == 4) {
                PathName = "Blue-FarFromAudience";
            } else {
                PathNumber = 1;
                PathName = "Red-CloseToAudience";
            }

            try {
                mainPath = initalizePath();
                initializedPathSuccess = true;
            } catch (RuntimeException e) {
                initializedPathSuccess = false;
            }

            telemetry.update();
        }

//        if (gamepad1.xWasPressed() && outtakeMotorCheck) {
//            if (attemptShooting) {
//                attemptShooting = false;
//            } else {
//                attemptShooting = true;
//            }
//            telemetry.update();
//        }

        waitForStart();
        if (isStopRequested()) {
            follower.stop();
            return;
        }

        follower.setPose(start);
        follower.follow(mainPath);

        boolean isDoneWithPath = false;
        telemetry.addData("Done With Path", isDoneWithPath);

        while (opModeIsActive()) {
            follower.update();
            isDoneWithPath = !follower.isBusy();
            telemetry.update();
        }

        follower.stop();
    }
}
