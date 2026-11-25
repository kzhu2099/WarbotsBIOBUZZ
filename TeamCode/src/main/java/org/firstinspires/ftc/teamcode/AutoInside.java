package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import static org.firstinspires.ftc.teamcode.AutoBase.Job.BACK;
import static org.firstinspires.ftc.teamcode.AutoBase.Job.FLOWER;
import static org.firstinspires.ftc.teamcode.AutoBase.Job.FRONT;

/**
 * Inside lane (tile column 3, under the hive), straight shots.
 * Drives under the hive whenever the next job is on the other side.
 */
@Autonomous(name = "Inside - straight", group = "Match")
public class AutoInside extends AutoBase {

    // ===== EDIT THE PLAN HERE =====
    // Runs in order, then parks. Allowed: FRONT, BACK, FLOWER (the scoring-wall flower).
    // Default: fire the 4 preloads into the front cell, refill behind the hive, fire into the back cell.
    static final Job[] PLAN = { FRONT, FLOWER, BACK };

    @Override
    protected Job[] plan() {
        return PLAN;
    }

    @Override
    protected void run(Job job) {
        switch (job) {
            case FRONT:
                goToSide(false);
                shootFrom(Spots.INSIDE_FRONT_SHOT);
                break;
            case BACK:
                goToSide(true);
                shootFrom(Spots.INSIDE_BACK_SHOT);
                break;
            case FLOWER:
                goToSide(true);
                collectFrom("BACK FLOWER", Spots.BACK_FLOWER_LINEUP, Spots.BACK_FLOWER);
                break;
        }
    }

    /** The loading zone is on the scoring side of the hive. */
    @Override
    protected void beforePark() {
        goToSide(true);
    }

    /** Drive straight down the lane under the hive. The robot never turns under the hive. */
    private void goToSide(boolean back) {
        if (onBackSide() == back) return;
        if (back) {
            driveTo("LANE FRONT END", Spots.INSIDE_LANE_FRONT);
            driveTo("UNDER HIVE", Spots.INSIDE_LANE_BACK);
        } else {
            driveTo("LANE BACK END", Spots.INSIDE_LANE_BACK);
            driveTo("UNDER HIVE", Spots.INSIDE_LANE_FRONT);
        }
    }

    @Override
    protected Pose startSpot() {
        return Spots.INSIDE_START;
    }

    @Override
    protected Pose parkSpot() {
        return Spots.INSIDE_PARK;
    }
}
