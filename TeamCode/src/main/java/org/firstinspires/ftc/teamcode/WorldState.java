package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/**
 * Everything the planner/controller need to know about the world right
 * now, in one place - see project brief section 30. This is intentionally
 * a thin facade over the actual live components (Localization, Inventory,
 * BallMap, CellMap), NOT a copy of their data: there is exactly one
 * BallMap, one Inventory, one Localization for the whole robot, and every
 * subsystem reads through this facade rather than keeping its own version.
 * Building a fresh WorldState each tick is just bundling references + the
 * per-tick fields (time), which is cheap - it is not a snapshot/clone.
 */
public class WorldState {

    public final Localization localization;
    public final Inventory inventory;
    public final BallMap ballMap;
    public final CellMap cellMap;

    public final boolean ballCameraAvailable;
    public final boolean cellCameraAvailable;

    public final double timeRemainingSeconds;
    public final double nowSeconds;

    public WorldState(Localization localization, Inventory inventory, BallMap ballMap, CellMap cellMap,
                       boolean ballCameraAvailable, boolean cellCameraAvailable,
                       double timeRemainingSeconds, double nowSeconds) {
        this.localization = localization;
        this.inventory = inventory;
        this.ballMap = ballMap;
        this.cellMap = cellMap;
        this.ballCameraAvailable = ballCameraAvailable;
        this.cellCameraAvailable = cellCameraAvailable;
        this.timeRemainingSeconds = timeRemainingSeconds;
        this.nowSeconds = nowSeconds;
    }

    public Pose robotPose() {
        return localization.pose();
    }
}
