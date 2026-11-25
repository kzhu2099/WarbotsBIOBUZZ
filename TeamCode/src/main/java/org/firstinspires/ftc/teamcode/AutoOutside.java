package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import static org.firstinspires.ftc.teamcode.AutoBase.Job.FLOWER;
import static org.firstinspires.ftc.teamcode.AutoBase.Job.FRONT;

/**
 * Outside lane (tile columns 1-2, between our wall and the hive support), angled shots.
 * Never enters the inside lane.
 */
@Autonomous(name = "Outside - angled", group = "Match")
public class AutoOutside extends AutoBase {

    // ===== EDIT THE PLAN HERE =====
    // Runs in order, then parks. Allowed: FRONT, FLOWER (the Red-wall flower).
    // BACK is not allowed: reaching the back cell from here would cross the inside lane.
    // Default: fire the 4 preloads into the front cell, refill at the wall flower, fire again.
    static final Job[] PLAN = { FRONT, FLOWER, FRONT };

    @Override
    protected Job[] plan() {
        return PLAN;
    }

    @Override
    protected void run(Job job) {
        switch (job) {
            case FRONT:
                shootFrom(Spots.OUTSIDE_FRONT_SHOT);
                break;
            case FLOWER:
                collectFrom("WALL FLOWER", Spots.WALL_FLOWER_LINEUP, Spots.WALL_FLOWER);
                break;
            default:
                unknownJob(job);
                break;
        }
    }

    @Override
    protected Pose startSpot() {
        return Spots.OUTSIDE_START;
    }

    @Override
    protected Pose parkSpot() {
        return Spots.OUTSIDE_PARK;
    }
}
