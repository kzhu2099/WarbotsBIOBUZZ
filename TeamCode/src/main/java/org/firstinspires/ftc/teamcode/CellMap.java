package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

import java.util.Arrays;
import java.util.List;

/** Tracks the four Cells and selects a scoring target for the chosen alliance. */
public class CellMap {

    // VERIFY THESE VALUES with repeated shots. The approach pose gives
    // room to turn; the scoring pose is the distance used to fire.
    private static final double APPROACH_STANDOFF_INCHES = 24.0;
    private static final double SCORING_STANDOFF_INCHES = 16.0;

    private final Cell redAudience;
    private final Cell redOpposite;
    private final Cell blueAudience;
    private final Cell blueOpposite;

    private Alliance ourAlliance = Alliance.BLUE;

    public CellMap() {
        redAudience = build(Alliance.RED, FieldSide.AUDIENCE);
        redOpposite = build(Alliance.RED, FieldSide.OPPOSITE);
        blueAudience = build(Alliance.BLUE, FieldSide.AUDIENCE);
        blueOpposite = build(Alliance.BLUE, FieldSide.OPPOSITE);
    }

    private static Cell build(Alliance alliance, FieldSide fieldSide) {
        return new Cell(alliance, fieldSide, FieldTagLibrary.primaryTagIdFor(alliance, fieldSide),
                FieldTagLibrary.nominalPoseFor(alliance, fieldSide),
                APPROACH_STANDOFF_INCHES, SCORING_STANDOFF_INCHES);
    }

    /** The one, precise way to get a specific one of the four cells. */
    public Cell cell(Alliance alliance, FieldSide fieldSide) {
        if (alliance == Alliance.RED) {
            return fieldSide == FieldSide.AUDIENCE ? redAudience : redOpposite;
        }
        return fieldSide == FieldSide.AUDIENCE ? blueAudience : blueOpposite;
    }

    /** Records which alliance is ours. Call this once alliance is known
     *  (init) and again any time it changes (e.g. driver toggles alliance
     *  during init_loop). */
    public void selectOurAlliance(boolean redAlliance) {
        ourAlliance = Alliance.of(redAlliance);
    }

    /** Which alliance is currently ours - used by AutonomousPlanner to
     *  detect the (rare, but real) case where a previously-committed cell
     *  belongs to an alliance we're no longer on, e.g. the driver toggled
     *  alliance after a route was already planned. */
    public Alliance ourAlliance() {
        return ourAlliance;
    }

    /** Both of our alliance's cells - audience-side and opposite-side. */
    public List<Cell> ourCells() {
        return Arrays.asList(cell(ourAlliance, FieldSide.AUDIENCE), cell(ourAlliance, FieldSide.OPPOSITE));
    }

    /**
     * The tag cluster is on the bottom face of each Cell, facing the tiles.
     * From below the Hive, it identifies the Cell whose opening faces up.
     * If neither alliance Cell has been seen, use the closer nominal pose.
     */
    public Cell bestOurCell(Pose robotPose) {
        Cell observedCell = mostRecentlyObservedOurCell();
        if (observedCell != null) return observedCell;

        Cell audience = cell(ourAlliance, FieldSide.AUDIENCE);
        Cell opposite = cell(ourAlliance, FieldSide.OPPOSITE);
        return scoreOf(audience, robotPose) >= scoreOf(opposite, robotPose) ? audience : opposite;
    }

    private Cell mostRecentlyObservedOurCell() {
        Cell audience = cell(ourAlliance, FieldSide.AUDIENCE);
        Cell opposite = cell(ourAlliance, FieldSide.OPPOSITE);
        boolean audienceSeen = audience.hasEverBeenDetected();
        boolean oppositeSeen = opposite.hasEverBeenDetected();

        if (!audienceSeen && !oppositeSeen) return null;
        if (!oppositeSeen) return audience;
        if (!audienceSeen) return opposite;
        return audience.lastDetectedAtSeconds() >= opposite.lastDetectedAtSeconds()
                ? audience : opposite;
    }

    private static double scoreOf(Cell cell, Pose robotPose) {
        double distance = Math.hypot(cell.fieldPose().x() - robotPose.x(), cell.fieldPose().y() - robotPose.y());
        return trustRank(cell.state()) * 1000.0 - distance;
    }

    private static int trustRank(CellState state) {
        switch (state) {
            case DETECTED: return 3;
            case CONFIRMED: return 2;
            case STALE: return 1;
            default: return 0;
        }
    }

    /**
     * Whether the OTHER same-alliance cell is now meaningfully more
     * trustworthy than the given (already-committed-to) one - a real
     * change in knowledge, not just "the robot happens to be a few inches
     * closer to it right now." Used as the replan trigger instead of
     * bestOurCell()'s full distance+trust score, specifically to avoid
     * replanning every time the robot's position nudges the distance
     * comparison one way or the other while driving around - that would
     * thrash between the two cells for no real reason. A meaningful
     * change - a fresh tag sighting identifies the other Cell as active -
     * is what should trigger a replan. A stale target is unsafe to score on
     * and must be handled by the controller rather than by guessing a side.
     */
    public boolean shouldReconsiderCell(Cell committed) {
        if (committed == null) return true;

        Cell observedCell = mostRecentlyObservedOurCell();
        if (observedCell != null) {
            return observedCell != committed;
        }

        Cell other = otherCellFor(committed);
        return other != null && trustRank(other.state()) > trustRank(committed.state());
    }

    private Cell otherCellFor(Cell cell) {
        if (cell == redAudience) return redOpposite;
        if (cell == redOpposite) return redAudience;
        if (cell == blueAudience) return blueOpposite;
        if (cell == blueOpposite) return blueAudience;
        return null;
    }

    /** Feeds one AprilTag observation in. Only observations already
     *  classified as CELL_TAG should be passed here - see Robot's
     *  perception update. */
    public void updateFromTag(AprilTagObservation observation, Pose robotFieldPose, double nowSeconds) {
        if (observation.role != AprilTagRole.CELL_TAG) return;

        Alliance alliance = FieldTagLibrary.allianceOf(observation.tagId);
        FieldSide fieldSide = FieldTagLibrary.fieldSideOf(observation.tagId);
        if (alliance == null || fieldSide == null) return;

        Pose observedFieldPose = observation.estimateFieldPose(robotFieldPose);
        Cell visibleCell = cell(alliance, fieldSide);
        visibleCell.updateFromSighting(observedFieldPose, observation.confidence, nowSeconds);

        FieldSide pairedSide = fieldSide == FieldSide.AUDIENCE
                ? FieldSide.OPPOSITE : FieldSide.AUDIENCE;
        Cell pairedCell = cell(alliance, pairedSide);

        // The official manual gives about 18.8 inches between a Hive's Cells.
        // VERIFY the direction and tag-to-Cell-center offset against field CAD.
        double dy = fieldSide == FieldSide.AUDIENCE ? 18.8 : -18.8;
        Pose nominalPaired = FieldTagLibrary.nominalPoseFor(alliance, pairedSide);
        pairedCell.setInferredFieldPose(new Pose(
                observedFieldPose.x(),
                observedFieldPose.y() + dy,
                nominalPaired.heading()
        ));
    }

    /** Ages all four cells' DETECTED/CONFIRMED/STALE state. Call once per
     *  loop regardless of whether a tag was seen this cycle. */
    public void tick(double nowSeconds) {
        redAudience.tick(nowSeconds);
        redOpposite.tick(nowSeconds);
        blueAudience.tick(nowSeconds);
        blueOpposite.tick(nowSeconds);
    }
}
