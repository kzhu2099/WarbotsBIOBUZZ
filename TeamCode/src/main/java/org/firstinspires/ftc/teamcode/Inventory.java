package org.firstinspires.ftc.teamcode;

/**
 * Tracks how many Pollen and Nectar pieces the robot expects to be carrying.
 * The four-piece limit applies to the combined total. Since the mechanism
 * has no piece sensors, counts may reflect timed assumptions rather than
 * confirmed pickups or scores.
 */
public class Inventory {

    /** Maximum combined number of Pollen and Nectar pieces. */
    public static final int MAX_TOTAL_BALLS = 4;

    private int pollenCount = 0;
    private int nectarCount = 0;

    private BallType lastAcquiredType;
    private BallType lastScoredType;

    public int pollenCount() {
        return pollenCount;
    }

    public int nectarCount() {
        return nectarCount;
    }

    public int totalCount() {
        return pollenCount + nectarCount;
    }

    public int remainingCapacity() {
        return MAX_TOTAL_BALLS - totalCount();
    }

    public boolean isFull() {
        return remainingCapacity() <= 0;
    }

    /** Returns whether one more piece fits within capacity. */
    public boolean canAcquire(BallType type) {
        return remainingCapacity() > 0;
    }

    /** Adds one piece to the estimated inventory if capacity allows it. */
    public boolean acquire(BallType type) {
        if (!canAcquire(type)) {
            return false;
        }
        if (type == BallType.POLLEN) {
            pollenCount++;
        } else {
            nectarCount++;
        }
        lastAcquiredType = type;
        return true;
    }

    /** Removes one piece from the inventory estimate, if present. */
    public void remove(BallType type) {
        if (type == BallType.POLLEN) {
            pollenCount = Math.max(0, pollenCount - 1);
        } else {
            nectarCount = Math.max(0, nectarCount - 1);
        }
    }

    /**
     * Clears the estimated load after a scoring cycle. The code assumes the
     * outtake worked; the robot has no sensor to confirm that the pieces scored.
     */
    public void scoreAll() {
        if (pollenCount > 0) {
            lastScoredType = BallType.POLLEN;
        } else if (nectarCount > 0) {
            lastScoredType = BallType.NECTAR;
        }
        pollenCount = 0;
        nectarCount = 0;
    }

    public BallType lastAcquiredType() {
        return lastAcquiredType;
    }

    public BallType lastScoredType() {
        return lastScoredType;
    }

    /** Restores counts saved when switching from autonomous to TeleOp. */
    void restoreFrom(int pollenCount, int nectarCount) {
        this.pollenCount = Math.max(0, Math.min(pollenCount, MAX_TOTAL_BALLS));
        this.nectarCount = Math.max(0, Math.min(nectarCount, MAX_TOTAL_BALLS - this.pollenCount));
    }

    @Override
    public String toString() {
        return "pollen=" + pollenCount + " nectar=" + nectarCount
                + " total=" + totalCount() + "/" + MAX_TOTAL_BALLS;
    }
}
