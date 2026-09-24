package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * All of the actual decision-making for autonomous: which balls are worth
 * collecting, in what order, while steering clear of opponent balls, and
 * whether the currently-executing plan is still valid. This class has NO
 * hardware or FTC SDK dependency - it only touches {@link WorldState} and
 * the plain data model (Ball/Hive/Inventory/Pose), specifically so it can
 * be unit-tested with simulated world state (project brief section 34)
 * without needing a robot, a camera, or even the FTC SDK.
 *
 * "Target selection" and "route planning" are not two independently
 * hardcoded balls-picked-one-at-a-time steps - see planRoute(): it
 * evaluates whole candidate SEQUENCES (up to remaining capacity) and picks
 * the best one, per project brief section 17 ("optimize a sequence of
 * targets, not independently choose the nearest ball every time").
 */
public class AutonomousPlanner {

    // ---- Tunable weights (all TODO: VERIFY/tune against real matches) ---
    private static final double COST_PER_INCH = 1.0;
    private static final double POLLEN_VALUE = 40.0;
    private static final double NECTAR_VALUE = 60.0;
    private static final double CONFIDENCE_BONUS_SCALE = 20.0;
    private static final double OPPONENT_PROXIMITY_PENALTY = 500.0;

    /** Cap on how many candidate balls enter the combinatorial route
     *  search - keeps worst-case search size tiny (see planRoute) even if
     *  many balls are remembered at once. The cheapest/most promising ones
     *  by a fast single-ball heuristic are kept. */
    private static final int MAX_CANDIDATES_CONSIDERED = 8;

    public static class Route {
        public static final Route EMPTY = new Route(new ArrayList<Ball>(), Double.POSITIVE_INFINITY);

        public final List<Ball> order;
        public final double cost;

        Route(List<Ball> order, double cost) {
            this.order = order;
            this.cost = cost;
        }

        public boolean isEmpty() {
            return order.isEmpty();
        }

        public Ball first() {
            return order.isEmpty() ? null : order.get(0);
        }
    }

    /**
     * Builds the best available multi-ball collection route given the
     * current world state, respecting remaining capacity and steering
     * around opponent balls. Returns Route.EMPTY if nothing is worth
     * collecting right now (e.g. no viable balls, or capacity is already
     * full - the caller should treat that as "go score" or "go park").
     */
    public Route planRoute(WorldState world) {
        int capacity = world.inventory.remainingCapacity();
        if (capacity <= 0) return Route.EMPTY;

        List<Ball> candidates = prefilterCandidates(world);
        if (candidates.isEmpty()) return Route.EMPTY;

        Hive hive = resolveScoringHive(world);
        Pose hiveAnchor = hive != null ? hive.computeTarget().approachPose : world.robotPose();
        List<Ball> opponents = world.ballMap.opponentBalls();

        int maxLength = Math.min(capacity, candidates.size());
        Route best = Route.EMPTY;

        boolean[] used = new boolean[candidates.size()];
        List<Ball> current = new ArrayList<>();
        RouteSearchState searchState = new RouteSearchState();
        searchState.best = best;

        search(candidates, used, current, world.robotPose(), hiveAnchor, opponents, maxLength, 0.0, searchState);

        return searchState.best;
    }

    private static class RouteSearchState {
        Route best = Route.EMPTY;
    }

    /**
     * Depth-first search over ordered subsets of candidates, up to
     * maxLength long. With candidates capped at MAX_CANDIDATES_CONSIDERED
     * (8) and maxLength capped at the robot's own capacity (4), the worst
     * case is a few thousand cost evaluations - trivial for a Control Hub
     * to do once per replan (this is NOT re-run every loop tick, only when
     * AutonomousController decides a replan is warranted).
     */
    private void search(List<Ball> candidates, boolean[] used, List<Ball> current,
                         Pose fromPose, Pose hiveAnchor, List<Ball> opponents,
                         int maxLength, double costSoFar, RouteSearchState searchState) {
        if (!current.isEmpty()) {
            double totalCost = costSoFar + legCost(fromPose, hiveAnchor, opponents);
            if (totalCost < searchState.best.cost) {
                searchState.best = new Route(new ArrayList<>(current), totalCost);
            }
        }

        if (current.size() >= maxLength) return;

        for (int i = 0; i < candidates.size(); i++) {
            if (used[i]) continue;
            Ball candidate = candidates.get(i);

            double leg = legCost(fromPose, candidate.fieldPose(), opponents);
            double valueBonus = valueOf(candidate);
            double newCost = costSoFar + leg - valueBonus;

            used[i] = true;
            current.add(candidate);
            search(candidates, used, current, candidate.fieldPose(), hiveAnchor, opponents, maxLength, newCost, searchState);
            current.remove(current.size() - 1);
            used[i] = false;
        }
    }

    private double legCost(Pose from, Pose to, List<Ball> opponents) {
        double distance = Math.hypot(to.x() - from.x(), to.y() - from.y());
        double cost = distance * COST_PER_INCH;

        double hazardRadius = RobotGeometry.totalHazardRadiusInches();
        for (Ball opponent : opponents) {
            double distanceToSegment = distancePointToSegment(opponent.fieldPose(), from, to);
            if (distanceToSegment < hazardRadius) {
                cost += OPPONENT_PROXIMITY_PENALTY;
            }
        }
        return cost;
    }

    private double valueOf(Ball ball) {
        double base = ball.type == BallType.NECTAR ? NECTAR_VALUE : POLLEN_VALUE;
        return base + ball.confidence() * CONFIDENCE_BONUS_SCALE;
    }

    private static double distancePointToSegment(Pose point, Pose segA, Pose segB) {
        double ax = segA.x(), ay = segA.y();
        double bx = segB.x(), by = segB.y();
        double px = point.x(), py = point.y();

        double abx = bx - ax, aby = by - ay;
        double lengthSquared = abx * abx + aby * aby;
        double t = lengthSquared <= 1e-9 ? 0 : ((px - ax) * abx + (py - ay) * aby) / lengthSquared;
        t = Math.max(0, Math.min(1, t));

        double closestX = ax + t * abx;
        double closestY = ay + t * aby;
        return Math.hypot(px - closestX, py - closestY);
    }

    /** Keeps the search space small by discarding all but the most
     *  promising candidates using a cheap single-ball heuristic first. */
    private List<Ball> prefilterCandidates(WorldState world) {
        List<Ball> all = new ArrayList<>(world.ballMap.collectibleTargets());
        Pose robotPose = world.robotPose();

        all.sort(Comparator.comparingDouble(ball ->
                Math.hypot(ball.fieldPose().x() - robotPose.x(), ball.fieldPose().y() - robotPose.y())
                        - valueOf(ball)));

        if (all.size() > MAX_CANDIDATES_CONSIDERED) {
            return all.subList(0, MAX_CANDIDATES_CONSIDERED);
        }
        return all;
    }

    /**
     * Which hive we should currently be scoring into. In BIOBUZZ each
     * alliance scores into its own hive, so this mostly just resolves the
     * HiveMap's active-hive flag - but it is kept as an explicit planner
     * responsibility (rather than the controller reaching directly into
     * HiveMap) so a more complex "which scoring location is best right
     * now" rule can be dropped in here later without touching the
     * controller, if the full BIOBUZZ manual turns out to call for one.
     * TODO: VERIFY against the full official manual once available.
     */
    public Hive resolveScoringHive(WorldState world) {
        return world.hiveMap.activeHive();
    }

    /**
     * Checks whether whatever AutonomousController is currently doing is
     * still valid given the latest world state, and if not, says why (see
     * project brief section 19 - explicit replan triggers instead of
     * rebuilding the whole plan every loop tick).
     */
    public ReplanReason detectReplanReason(WorldState world, Ball currentTarget, Hive committedHive) {
        if (currentTarget != null) {
            if (currentTarget.state() == BallState.MISSING) return ReplanReason.TARGET_MISSING;
            if (currentTarget.state() == BallState.INVALID) return ReplanReason.TARGET_MISSING;
            if (currentTarget.state() == BallState.COLLECTED) return ReplanReason.TARGET_ALREADY_COLLECTED;
        }

        if (committedHive != null) {
            Hive active = world.hiveMap.activeHive();
            if (active != null && active != committedHive) {
                return ReplanReason.ACTIVE_HIVE_CHANGED;
            }
        }

        if (world.localization.hasDeviatedSignificantly()) {
            return ReplanReason.POSE_DEVIATION;
        }

        return ReplanReason.NONE;
    }

    /**
     * Whether it is safe/worthwhile to start (or continue) another
     * collect-and-score cycle given the time remaining, per project brief
     * section 24. Deliberately conservative: requires enough time to
     * finish a cycle AND still park afterward.
     */
    public boolean worthAnotherCycle(WorldState world, double estimatedCollectSeconds,
                                      double estimatedScoreSeconds, double estimatedParkSeconds,
                                      double safeReserveSeconds) {
        double required = estimatedCollectSeconds + estimatedScoreSeconds + estimatedParkSeconds + safeReserveSeconds;
        return world.timeRemainingSeconds >= required && !world.ballMap.collectibleTargets().isEmpty();
    }
}
