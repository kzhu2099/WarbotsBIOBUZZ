package org.firstinspires.ftc.teamcode;

/**
 * The robot's actual game-element inventory. Per the current rule
 * clarification: the robot can carry a COMBINATION of up to four
 * Pollen/Nectar balls total - not four of each independently. Every
 * capacity check in the project should go through this class rather than
 * comparing raw counts against a magic number somewhere else.
 */
public class Inventory {

    /** pollen + nectar can never exceed this. */
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

    /** Would acquiring one more ball of this type fit within capacity? */
    public boolean canAcquire(BallType type) {
        return remainingCapacity() > 0;
    }

    /**
     * Records that a ball of the given type was actually, verifiably
     * acquired (see ScoringMechanism / AutonomousController - this should
     * only be called after acquisition is confirmed, never just because
     * intake was commanded on).
     *
     * @return true if it was recorded, false if capacity did not allow it
     *         (which would itself indicate a mechanism/sensor problem,
     *         since the mechanism should refuse to intake past capacity).
     */
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

    /** Removes one ball of the given type, e.g. if it was lost/ejected. */
    public void remove(BallType type) {
        if (type == BallType.POLLEN) {
            pollenCount = Math.max(0, pollenCount - 1);
        } else {
            nectarCount = Math.max(0, nectarCount - 1);
        }
    }

    /**
     * Records that the entire current load was scored successfully. Only
     * call this once scoring is actually verified (see AutoState.VERIFY_SCORE) -
     * never assume a score just because outtake ran.
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

    /** Used only when resuming state saved from a previous OpMode (see
     *  AutoToTeleOp) - sets raw counts directly rather than going through
     *  acquire(), since this isn't a new acquisition event. */
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
