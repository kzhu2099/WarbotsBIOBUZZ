package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * LEAVE + PARK only, no mechanisms needed. D-pad left/right in INIT picks the
 * outside or inside start.
 */
@Autonomous(name = "Practice - leave and park", group = "Practice")
public class AutoPractice extends AutoBase {
    private boolean inside = false;

    @Override
    protected void readExtraInitButtons() {
        if (gamepad1.dpadLeftWasPressed()) inside = false;
        if (gamepad1.dpadRightWasPressed()) inside = true;
    }

    @Override
    protected String describe() {
        return "Leave + park from " + (inside ? "INSIDE" : "OUTSIDE") + " start (dpad L/R)";
    }

    @Override
    protected Job[] plan() {
        return new Job[0];  // no shooting
    }

    @Override
    protected void run(Job job) {}

    /** LEAVE, then (inside only) drive down the lane under the hive to reach the loading zone. */
    @Override
    protected void beforePark() {
        Pose start = startSpot();
        // 15 in straight off the starting wall = LEAVE.
        double forward = 15;
        driveTo("LEAVE", new Pose(start.x() + forward * Math.cos(start.heading()),
                start.y() + forward * Math.sin(start.heading()), start.heading()));
        // Inside drives its real lane through the hive to reach the loading zone.
        if (inside) driveTo("UNDER HIVE", Spots.INSIDE_LANE_BACK);
    }

    @Override
    protected Pose startSpot() {
        return inside ? Spots.INSIDE_START : Spots.OUTSIDE_START;
    }

    @Override
    protected Pose parkSpot() {
        return inside ? Spots.INSIDE_PARK : Spots.OUTSIDE_PARK;
    }
}
