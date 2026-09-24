package org.firstinspires.ftc.teamcode.practice;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

// AUDITED, UNCHANGED: a from-scratch, no-Robot-class exercise (raw
// Follower only), unrelated to the Jar/Tipper/MemoryPalace terminology
// that needed fixing elsewhere in this project, so it needed no rewrite.
@Disabled
@Autonomous(name = "Auto Practice")
public class AutoTemplatePractice extends LinearOpMode {

    public Follower follower;
    public Path leaveAndPark; // Example variable for your path

    @Override
    public void runOpMode() {
        // HINT: Initialize the Follower using hardwareMap.

        // HINT: Define your starting Pose (X, Y, and heading) using coordinates mapped from the PedroPathing Visualizer.
        Pose startPose = new Pose(0, 0, 0);

        // HINT: Set the follower's starting pose.

        // HINT: Update telemetry here to confirm initialization, then call telemetry.update()
        // so it actually shows up before you hit PLAY.

        waitForStart();
        if (isStopRequested()) return;

        // HINT: Build your path or spline here using the points you gathered from the visualizer,
        // and assign it to leaveAndPark.

        // HINT: Instruct the follower to begin following leaveAndPark.

        while (opModeIsActive()) {
            // HINT: Because this loop runs repeatedly, you MUST call the follower's update
            // method here to continuously calculate and adjust wheel powers along the path.
            // follower.update();
        }

        // HINT: Ensure all movement ceases here now that the loop above has exited.
    }
}
