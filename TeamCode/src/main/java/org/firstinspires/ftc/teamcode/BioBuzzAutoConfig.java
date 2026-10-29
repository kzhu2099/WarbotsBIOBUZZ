package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

import java.util.ArrayList;
import java.util.List;

public final class BioBuzzAutoConfig {

    private BioBuzzAutoConfig() {}

    public static final double FIELD_SIZE_INCHES = Points.FIELD_SIZE;

    // Replace these with the Flower centers measured from the official field CAD.
    public static final double FLOWER_LEFT_Y_INCHES = 48.0;
    public static final double FLOWER_TOP_X_INCHES = 48.0;
    public static final double FLOWER_RIGHT_Y_INCHES = 96.0;
    public static final double FLOWER_BOTTOM_X_INCHES = 96.0;

    // Red's Loading Zone is on the left wall; blue's is on the right wall.
    // Measure the center from the closest corner on the actual field.
    public static final double LOADING_ZONE_CENTER_OFFSET_INCHES = 36.0;

    public static final double LEAVE_DISTANCE_INCHES = 8.0;
    public static final double LEAVE_TIMEOUT_SECONDS = 4.0;
    public static final double PARK_START_SECONDS = 21.0;
    public static final double PARK_LANE_TIMEOUT_SECONDS = 5.0;
    public static final double PARK_TIMEOUT_SECONDS = 9.0;
    public static final double AUTO_SAFETY_CUTOFF_SECONDS = 29.5;
    public static final double PARK_LANE_CLEARANCE_INCHES = 18.0;
    public static final double AIM_TOLERANCE_RADIANS = Math.toRadians(3.0);
    public static final double AIM_TIMEOUT_SECONDS = 2.0;
    public static final double FLOWER_LEG_TIMEOUT_SECONDS = 7.0;

    public enum Flower {
        LEFT_WALL(new Pose(0.0, FLOWER_LEFT_Y_INCHES, Math.PI)),
        TOP_WALL(new Pose(FLOWER_TOP_X_INCHES, FIELD_SIZE_INCHES, Math.PI / 2.0)),
        RIGHT_WALL(new Pose(FIELD_SIZE_INCHES, FLOWER_RIGHT_Y_INCHES, 0.0)),
        BOTTOM_WALL(new Pose(FLOWER_BOTTOM_X_INCHES, 0.0, -Math.PI / 2.0));

        public final Pose fieldPose;

        Flower(Pose fieldPose) {
            this.fieldPose = fieldPose;
        }
    }

    public static Pose leavePose(Pose start) {
        return new Pose(
                start.x() + Math.cos(start.heading()) * LEAVE_DISTANCE_INCHES,
                start.y() + Math.sin(start.heading()) * LEAVE_DISTANCE_INCHES,
                start.heading()
        );
    }

    public static Pose loadingZonePose(boolean redAlliance) {
        double x = redAlliance
                ? RobotGeometry.HALF_LENGTH_INCHES + 1.0
                : FIELD_SIZE_INCHES - RobotGeometry.HALF_LENGTH_INCHES - 1.0;
        double y = redAlliance
                ? FIELD_SIZE_INCHES - LOADING_ZONE_CENTER_OFFSET_INCHES
                : LOADING_ZONE_CENTER_OFFSET_INCHES;
        return new Pose(x, y, redAlliance ? Math.PI : 0.0);
    }

    // Park on the edge instead of cutting across the Hive or the Flowers.
    public static List<Pose> parkingLaneRoute(boolean redAlliance, Pose from) {
        Pose zone = loadingZonePose(redAlliance);
        double low = laneInset();
        double high = FIELD_SIZE_INCHES - low;
        double x = redAlliance ? low : high;
        Pose laneTarget = new Pose(x, clamp(zone.y(), low, high), 0.0);
        return perimeterRoute(from, laneTarget);
    }

    // Go to the nearest edge, follow it to the selected Flower, then approach the wall.
    public static List<Pose> flowerRoute(Pose from, Flower flower) {
        Pose approach = flowerApproachPose(flower);
        double low = laneInset();
        double high = FIELD_SIZE_INCHES - low;

        double leftGap = Math.abs(approach.x() - low);
        double rightGap = Math.abs(approach.x() - high);
        double bottomGap = Math.abs(approach.y() - low);
        double topGap = Math.abs(approach.y() - high);

        Pose laneTarget;
        if (leftGap <= rightGap && leftGap <= bottomGap && leftGap <= topGap) {
            laneTarget = new Pose(low, clamp(approach.y(), low, high), 0.0);
        } else if (rightGap <= bottomGap && rightGap <= topGap) {
            laneTarget = new Pose(high, clamp(approach.y(), low, high), 0.0);
        } else if (bottomGap <= topGap) {
            laneTarget = new Pose(clamp(approach.x(), low, high), low, 0.0);
        } else {
            laneTarget = new Pose(clamp(approach.x(), low, high), high, 0.0);
        }

        List<Pose> route = perimeterRoute(from, laneTarget);
        addPoint(route, approach);
        return route;
    }

    public static Flower closestFlower(Pose from) {
        Flower closest = Flower.LEFT_WALL;
        double bestDistance = Double.POSITIVE_INFINITY;

        for (Flower flower : Flower.values()) {
            Pose approach = flowerApproachPose(flower);
            double distance = Math.hypot(approach.x() - from.x(), approach.y() - from.y());
            if (distance < bestDistance) {
                bestDistance = distance;
                closest = flower;
            }
        }
        return closest;
    }

    public static Pose flowerApproachPose(Flower flower) {
        double wallDistance = Math.max(
                RobotGeometry.HALF_LENGTH_INCHES + 1.0,
                RobotGeometry.INTAKE_REACH_INCHES - 1.0
        );
        Pose flowerPose = flower.fieldPose;

        return new Pose(
                flowerPose.x() - Math.cos(flowerPose.heading()) * wallDistance,
                flowerPose.y() - Math.sin(flowerPose.heading()) * wallDistance,
                flowerPose.heading()
        );
    }

    private static double laneInset() {
        return Math.max(
                RobotGeometry.HALF_WIDTH_INCHES,
                RobotGeometry.HALF_LENGTH_INCHES
        ) + PARK_LANE_CLEARANCE_INCHES;
    }

    private static List<Pose> perimeterRoute(Pose from, Pose laneTarget) {
        double low = laneInset();
        double high = FIELD_SIZE_INCHES - low;
        double side = high - low;
        double perimeter = 4.0 * side;

        Pose target = new Pose(clamp(laneTarget.x(), low, high),
                clamp(laneTarget.y(), low, high), laneTarget.heading());
        double targetS = perimeterPosition(target, low, high);

        Pose[] entries = {
                new Pose(low, clamp(from.y(), low, high), 0.0),
                new Pose(high, clamp(from.y(), low, high), 0.0),
                new Pose(clamp(from.x(), low, high), low, 0.0),
                new Pose(clamp(from.x(), low, high), high, 0.0)
        };

        Pose entry = entries[0];
        double entryDistance = distance(from, entry);
        double startS = perimeterPosition(entry, low, high);
        double forwardDistance = wrap(targetS - startS, perimeter);
        double backwardDistance = wrap(startS - targetS, perimeter);
        double bestCost = entryDistance + Math.min(forwardDistance, backwardDistance);

        for (int i = 1; i < entries.length; i++) {
            Pose candidate = entries[i];
            double candidateDistance = distance(from, candidate);
            double candidateS = perimeterPosition(candidate, low, high);
            double candidateForward = wrap(targetS - candidateS, perimeter);
            double candidateBackward = wrap(candidateS - targetS, perimeter);
            double cost = candidateDistance + Math.min(candidateForward, candidateBackward);

            if (cost < bestCost) {
                entry = candidate;
                entryDistance = candidateDistance;
                startS = candidateS;
                forwardDistance = candidateForward;
                backwardDistance = candidateBackward;
                bestCost = cost;
            }
        }

        boolean forward = forwardDistance <= backwardDistance;
        double routeDistance = forward ? forwardDistance : backwardDistance;
        double endS = startS + (forward ? routeDistance : -routeDistance);

        List<Pose> route = new ArrayList<>();
        double entryHeading = entryDistance > 1.0
                ? Math.atan2(entry.y() - from.y(), entry.x() - from.x())
                : perimeterHeading(startS, forward, low, high);
        addPoint(route, new Pose(entry.x(), entry.y(), entryHeading));

        double corner;
        if (forward) {
            corner = (Math.floor(startS / side) + 1.0) * side;
            while (corner < endS - 0.5) {
                addPoint(route, lanePointAt(corner, forward, low, high));
                corner += side;
            }
        } else {
            corner = (Math.ceil(startS / side) - 1.0) * side;
            while (corner > endS + 0.5) {
                addPoint(route, lanePointAt(corner, forward, low, high));
                corner -= side;
            }
        }

        addPoint(route, lanePointAt(targetS, forward, low, high));
        return route;
    }

    private static double perimeterPosition(Pose pose, double low, double high) {
        double side = high - low;
        if (Math.abs(pose.y() - low) < 0.01) return pose.x() - low;
        if (Math.abs(pose.x() - high) < 0.01) return side + pose.y() - low;
        if (Math.abs(pose.y() - high) < 0.01) return 2.0 * side + high - pose.x();
        return 3.0 * side + high - pose.y();
    }

    private static Pose lanePointAt(double s, boolean forward, double low, double high) {
        double side = high - low;
        double p = wrap(s, 4.0 * side);
        double x;
        double y;

        if (p < side) {
            x = low + p;
            y = low;
        } else if (p < 2.0 * side) {
            x = high;
            y = low + p - side;
        } else if (p < 3.0 * side) {
            x = high - (p - 2.0 * side);
            y = high;
        } else {
            x = low;
            y = high - (p - 3.0 * side);
        }

        return new Pose(x, y, perimeterHeading(s, forward, low, high));
    }

    private static double perimeterHeading(double s, boolean forward, double low, double high) {
        double side = high - low;
        double p = wrap(s + (forward ? 0.1 : -0.1), 4.0 * side);
        if (p < side) return forward ? 0.0 : Math.PI;
        if (p < 2.0 * side) return forward ? Math.PI / 2.0 : -Math.PI / 2.0;
        if (p < 3.0 * side) return forward ? Math.PI : 0.0;
        return forward ? -Math.PI / 2.0 : Math.PI / 2.0;
    }

    private static void addPoint(List<Pose> points, Pose next) {
        if (points.isEmpty() || distance(points.get(points.size() - 1), next) > 1.0) {
            points.add(next);
        }
    }

    private static double distance(Pose a, Pose b) {
        return Math.hypot(a.x() - b.x(), a.y() - b.y());
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static double wrap(double value, double period) {
        double result = value % period;
        return result < 0.0 ? result + period : result;
    }
}
