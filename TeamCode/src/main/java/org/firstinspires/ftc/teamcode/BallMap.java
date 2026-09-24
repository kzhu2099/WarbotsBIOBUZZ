package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The robot's memory of every ball it has ever seen this match - the
 * "memory map" (formerly MemoryPalace). This is one of the most important
 * pieces of the autonomous system: it lets the robot reason about balls
 * that are not currently in the camera frame, reconcile new sightings with
 * old ones instead of duplicating them, and notice when an expected ball
 * has actually disappeared (collected by an opponent, knocked away, etc.)
 * so the planner can replan instead of driving to an empty spot forever.
 *
 * Reconciliation rule, per ball type:
 *   - A fresh detection within MATCH_DISTANCE_INCHES of an existing ball of
 *     the same type is treated as the same physical ball: its position,
 *     owner, and confidence are refreshed instead of creating a duplicate.
 *   - A fresh detection that doesn't match anything existing becomes a new
 *     Ball.
 *   - Any remembered ball that was NOT matched this cycle, but that
 *     *should* have been visible (it falls inside the camera's current
 *     field of view), is aged: its state moves toward MISSING the longer
 *     it goes unseen while supposedly in view. A ball that goes unseen
 *     while it is NOT in view (robot is looking somewhere else, or too far
 *     away) is left alone - that's expected, not evidence it's gone.
 */
public class BallMap {

    /** How close a fresh detection has to be to an existing ball (of the
     *  same type) to be treated as a re-observation instead of a new ball. */
    static final double MATCH_DISTANCE_INCHES = 6.0;

    /** Position uncertainty assigned to any freshly (re-)observed ball. */
    static final double FRESH_SIGHTING_UNCERTAINTY_INCHES = 1.5;

    /** How fast a remembered ball's position uncertainty grows while unseen. */
    static final double UNCERTAINTY_GROWTH_INCHES_PER_SECOND = 3.0;

    /** How many consecutive "should have been visible but wasn't" checks
     *  before a REMEMBERED ball is declared MISSING. */
    private static final int MISSING_AFTER_CONSECUTIVE_MISSES = 5;

    /** How long (seconds) a MISSING/INVALID ball is retained for telemetry
     *  before being dropped from the map entirely. */
    private static final double FORGET_AFTER_SECONDS = 3.0;

    /** Ball is no longer treated as a valid target once its uncertainty
     *  grows this large - it's not "gone," but it's not precise enough to
     *  drive to blindly either; a fresh sighting is needed first. */
    private static final double MAX_USABLE_UNCERTAINTY_INCHES = 18.0;

    public interface FieldOfView {
        boolean canSee(Pose robotPose, Pose fieldPose);
    }

    /** A simple forward-facing cone-shaped field of view model, good
     *  enough for a fixed, forward-mounted floor camera. */
    public static class ConeFieldOfView implements FieldOfView {
        private final double halfAngleRadians;
        private final double maxRangeInches;

        public ConeFieldOfView(double halfAngleRadians, double maxRangeInches) {
            this.halfAngleRadians = halfAngleRadians;
            this.maxRangeInches = maxRangeInches;
        }

        @Override
        public boolean canSee(Pose robotPose, Pose fieldPose) {
            double dx = fieldPose.x() - robotPose.x();
            double dy = fieldPose.y() - robotPose.y();
            double distance = Math.hypot(dx, dy);
            if (distance > maxRangeInches) return false;

            double bearing = normalize(Math.atan2(dy, dx) - robotPose.heading());
            return Math.abs(bearing) <= halfAngleRadians;
        }

        private static double normalize(double angle) {
            while (angle > Math.PI) angle -= 2 * Math.PI;
            while (angle < -Math.PI) angle += 2 * Math.PI;
            return angle;
        }
    }

    private final List<Ball> balls = new ArrayList<>();

    // Per-ball miss counters, keyed by Ball id.
    private final java.util.Map<Integer, Integer> missCounts = new java.util.HashMap<>();
    private final java.util.Map<Integer, Double> becameTerminalAt = new java.util.HashMap<>();

    /**
     * Reconciles this cycle's fresh detections with everything already
     * remembered, and ages/expires anything that should have been seen
     * but wasn't.
     */
    public void observe(List<BallDetection> detections, Pose robotPose, FieldOfView fieldOfView, double nowSeconds) {
        List<Ball> matchedThisCycle = new ArrayList<>();

        for (BallDetection detection : detections) {
            Ball existing = closestOfType(detection.type, detection.fieldPose);
            if (existing != null) {
                existing.reobserve(detection.owner, detection.fieldPose, detection.confidence, nowSeconds);
                missCounts.put(existing.id, 0);
                matchedThisCycle.add(existing);
            } else {
                Ball fresh = new Ball(detection.type, detection.owner, detection.fieldPose, detection.confidence, nowSeconds);
                balls.add(fresh);
                matchedThisCycle.add(fresh);
            }
        }

        for (Ball ball : balls) {
            if (matchedThisCycle.contains(ball)) continue;
            if (ball.state() == BallState.COLLECTED || ball.state() == BallState.MISSING || ball.state() == BallState.INVALID) {
                continue;
            }

            boolean shouldHaveBeenVisible = fieldOfView.canSee(robotPose, ball.fieldPose());
            if (shouldHaveBeenVisible) {
                int misses = missCounts.getOrDefault(ball.id, 0) + 1;
                missCounts.put(ball.id, misses);
                if (misses >= MISSING_AFTER_CONSECUTIVE_MISSES) {
                    ball.markMissing();
                    becameTerminalAt.put(ball.id, nowSeconds);
                }
            }
            // Either way (should-have-seen-but-didn't, below miss threshold;
            // or wasn't in view at all), the position estimate ages.
            ball.ageOutOfView(nowSeconds);
        }

        expireOldTerminalBalls(nowSeconds);
    }

    /**
     * Explicitly declares that the robot drove to where a specific ball was
     * expected and it was not actually collectible (e.g. intake ran and
     * found nothing). Distinct from the passive camera-based MISSING
     * detection above, for the case where the robot's own physical
     * presence is the strongest evidence the ball is gone.
     */
    public void markMissing(Ball ball, double nowSeconds) {
        if (ball == null) return;
        ball.markMissing();
        becameTerminalAt.put(ball.id, nowSeconds);
    }

    public void markCollected(Ball ball) {
        if (ball == null) return;
        ball.markCollected();
    }

    private void expireOldTerminalBalls(double nowSeconds) {
        Iterator<Ball> it = balls.iterator();
        while (it.hasNext()) {
            Ball ball = it.next();
            boolean terminal = ball.state() == BallState.MISSING || ball.state() == BallState.INVALID;
            if (!terminal) continue;
            Double terminalAt = becameTerminalAt.get(ball.id);
            if (terminalAt != null && nowSeconds - terminalAt >= FORGET_AFTER_SECONDS) {
                it.remove();
                missCounts.remove(ball.id);
                becameTerminalAt.remove(ball.id);
            }
        }
    }

    private Ball closestOfType(BallType type, Pose fieldPose) {
        Ball best = null;
        double bestDistance = MATCH_DISTANCE_INCHES;

        for (Ball ball : balls) {
            if (ball.type != type) continue;
            if (ball.state() == BallState.COLLECTED) continue;
            double distance = Math.hypot(ball.fieldPose().x() - fieldPose.x(), ball.fieldPose().y() - fieldPose.y());
            if (distance <= bestDistance) {
                best = ball;
                bestDistance = distance;
            }
        }
        return best;
    }

    /** Every ball currently worth considering as a collection target:
     *  active state (VISIBLE/REMEMBERED), ours, and precise enough to
     *  actually drive to. */
    public List<Ball> collectibleTargets() {
        List<Ball> result = new ArrayList<>();
        for (Ball ball : balls) {
            if (!ball.isActiveTarget()) continue;
            if (ball.owner() != BallOwner.OURS) continue;
            if (ball.positionUncertaintyInches() > MAX_USABLE_UNCERTAINTY_INCHES) continue;
            result.add(ball);
        }
        return result;
    }

    /** Every ball currently believed to be the opponent's - used as
     *  obstacles/no-go zones, never as targets. */
    public List<Ball> opponentBalls() {
        List<Ball> result = new ArrayList<>();
        for (Ball ball : balls) {
            if (!ball.isActiveTarget()) continue;
            if (ball.owner() != BallOwner.OPPONENT) continue;
            result.add(ball);
        }
        return result;
    }

    public List<Ball> all() {
        return balls;
    }

    public int visibleCount() {
        int count = 0;
        for (Ball ball : balls) if (ball.state() == BallState.VISIBLE) count++;
        return count;
    }

    public int rememberedCount() {
        int count = 0;
        for (Ball ball : balls) if (ball.state() == BallState.REMEMBERED) count++;
        return count;
    }
}
