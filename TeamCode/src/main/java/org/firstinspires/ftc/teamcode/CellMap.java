package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

import java.util.Arrays;
import java.util.List;

/**
 * Tracks all four Cell structures on the field and answers the question
 * the project brief poses directly: "which cell am I currently supposed to
 * score at?" - never a hardcoded pose, always derived from alliance +
 * AprilTag observations + (since each alliance has two valid cells) which
 * one is actually more useful right now.
 *
 * All four are tracked, not just "ours," because the vision system may see
 * any of the sixteen real tags, and because a team strategy might
 * eventually care about the opponent cells' state too.
 */
public class CellMap {

    // STANDOFF DISTANCES - what they actually are and how to get real
    // numbers, not just "TODO: verify":
    //
    // A cell's fieldPose faces outward along its scoring opening (see
    // Cell's Javadoc). "Standoff" is how far out along that direction the
    // robot should be - APPROACH_STANDOFF_INCHES for the lineup point
    // before committing, SCORING_STANDOFF_INCHES for where it actually
    // fires. These are NOT a geometric fact you can measure with a tape
    // measure on the field alone - they depend on THIS ROBOT's outtake
    // mechanism (this robot's outtake is two motors that fire together,
    // presumably rolling/launching a ball rather than placing it directly
    // into the opening), so the right distance is wherever that mechanism,
    // at whatever fixed power it runs at, reliably lands a ball in the
    // cell. That's an empirical question, answered like this:
    //   1. Put a ball in the mechanism, aim the robot at a real cell from
    //      a rough guessed distance, fire the outtake.
    //   2. Move the robot closer/farther and repeat until it reliably
    //      scores (several tries in a row, not one lucky shot).
    //   3. Measure that distance from the cell's opening to the robot's
    //      tracked center point (not to the bumper) - that's
    //      SCORING_STANDOFF_INCHES.
    //   4. APPROACH_STANDOFF_INCHES just needs to be a bit farther out
    //      than that - enough that the robot has room to square up
    //      without already being close enough to clip the structure while
    //      still turning - a few inches more than the scoring distance is
    //      usually enough; it doesn't need its own separate tuning pass.
    // Until that tuning happens, these are placeholder guesses - not
    // wrong exactly, just unverified.
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
     * Which of our two same-alliance cells is actually the better target
     * right now, given where the robot currently is. This is a live
     * decision, not a fixed flag - a more confidently-tracked cell always
     * beats a less-confident one regardless of distance, and among
     * equally-trusted cells the closer one wins. Never returns null: with
     * both cells always present (even at CellState.UNKNOWN, using their
     * placeholder nominal pose), there is always a "current best guess."
     */
    public Cell bestOurCell(Pose robotPose) {
        Cell audience = cell(ourAlliance, FieldSide.AUDIENCE);
        Cell opposite = cell(ourAlliance, FieldSide.OPPOSITE);
        return scoreOf(audience, robotPose) >= scoreOf(opposite, robotPose) ? audience : opposite;
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
     * change - the committed cell going stale, or the other one being
     * freshly (re)detected - is what should actually trigger a replan.
     */
    public boolean shouldReconsiderCell(Cell committed) {
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
        cell(alliance, fieldSide).updateFromSighting(observedFieldPose, observation.confidence, nowSeconds);
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
