package org.firstinspires.ftc.teamcode.practice;

import static com.pedropathing.api.Paths.*;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.pedro.Constants;

// AUDITED, UNCHANGED: a from-scratch, no-Robot-class exercise (raw
// Follower only), unrelated to the Jar/Tipper/MemoryPalace terminology
// that needed fixing elsewhere in this project, so it needed no rewrite.
@Disabled
@Autonomous(name = "Matthew Auto Testing")
public class MatthewAutoTesting extends LinearOpMode {

    public Follower follower;
    public Path leaveAndPark; // Example variable for your path

    @Override
    public void runOpMode() {
        follower = Constants.create(hardwareMap);

        waitForStart();
        if (isStopRequested()) return;

        while (opModeIsActive()) {
            follower.update();
        }

        follower.stop();
    }
}
