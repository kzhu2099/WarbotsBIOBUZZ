package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

/** The current pose, inventory, tracked game pieces, and remaining match time. */
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
