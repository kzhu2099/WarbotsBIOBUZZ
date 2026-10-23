package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

import java.util.ArrayList;
import java.util.List;

public final class BioBuzzAutoConfig {

    private BioBuzzAutoConfig() {}

    public static final double FIELD_SIZE_INCHES = Points.FIELD_SIZE;

    // Measure the four Flower centers from the current field CAD.
    public static final double FLOWER_LEFT_Y_INCHES = 48.0;
    public static final double FLOWER_TOP_X_INCHES = 48.0;
    public static final double FLOWER_RIGHT_Y_INCHES = 96.0;
    public static final double FLOWER_BOTTOM_X_INCHES = 96.0;

    // Red's zone is 36 in below the top-left corner; blue's is 36 in above the bottom-right.
    public static final double LOADING_ZONE_CENTER_OFFSET_INCHES = 36.0;
    public static final double LEAVE_DISTANCE_INCHES = 8.0;
    public static final double LEAVE_TIMEOUT_SECONDS = 4.0;
    public static final double PARK_START_SECONDS = 18.0;
    public static final double PARK_LANE_TIMEOUT_SECONDS = 5.0;
    public static final double PARK_TIMEOUT_SECONDS = 9.0;
    public static final double AUTO_SAFETY_CUTOFF_SECONDS = 29.5;
    public static final double PARK_LANE_CLEARANCE_INCHES = 18.0;
    public static final double AIM_TOLERANCE_RADIANS = Math.toRadians(3.0);
    public static final double AIM_TIMEOUT_SECONDS = 2.0;

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

    // Head to the nearest inset edge, then follow the perimeter toward our zone.
    // If parking starts in the field, the first leg heads to the nearest edge.
    public static List<Pose> parkingLaneRoute(boolean redAlliance, Pose currentPose) {
        double inset = Math.max(
                RobotGeometry.HALF_WIDTH_INCHES,
                RobotGeometry.HALF_LENGTH_INCHES
        ) + PARK_LANE_CLEARANCE_INCHES;
        double low = inset;
        double high = FIELD_SIZE_INCHES - inset;
        double side = high - low;
        double perimeter = 4.0 * side;

        Pose zone = loadingZonePose(redAlliance);
        double targetX = redAlliance ? low : high;
        double targetY = clamp(zone.y(), low, high);
        Pose laneTarget = new Pose(targetX, targetY, 0.0);
        double targetS = perimeterPosition(laneTarget, low, high);

        Pose[] entries = {
                new Pose(low, clamp(currentPose.y(), low, high), 0.0),
                new Pose(high, clamp(currentPose.y(), low, high), 0.0),
                new Pose(clamp(currentPose.x(), low, high), low, 0.0),
                new Pose(clamp(currentPose.x(), low, high), high, 0.0)
        };

        Pose entry = entries[0];
        double entryDistance = distance(currentPose, entry);
        for (int i = 1; i < entries.length; i++) {
            double candidateDistance = distance(currentPose, entries[i]);
            if (candidateDistance < entryDistance) {
                entry = entries[i];
                entryDistance = candidateDistance;
            }
        }

        double startS = perimeterPosition(entry, low, high);
        double forwardDistance = wrap(targetS - startS, perimeter);
        double backwardDistance = wrap(startS - targetS, perimeter);
        boolean forward = forwardDistance <= backwardDistance;
        double routeDistance = forward ? forwardDistance : backwardDistance;
        double endS = startS + (forward ? routeDistance : -routeDistance);

        List<Pose> route = new ArrayList<>();
        double entryHeading = distance(currentPose, entry) > 1.0
                ? Math.atan2(entry.y() - currentPose.y(), entry.x() - currentPose.x())
                : perimeterHeading(startS, forward, low, high);
        addPoint(route, new Pose(entry.x(), entry.y(), entryHeading));

        double nextCorner;
        if (forward) {
            nextCorner = (Math.floor(startS / side) + 1.0) * side;
            while (nextCorner < endS - 0.5) {
                addPoint(route, lanePointAt(nextCorner, forward, low, high));
                nextCorner += side;
            }
        } else {
            nextCorner = (Math.ceil(startS / side) - 1.0) * side;
            while (nextCorner > endS + 0.5) {
                addPoint(route, lanePointAt(nextCorner, forward, low, high));
                nextCorner -= side;
            }
        }

        addPoint(route, lanePointAt(targetS, forward, low, high));
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
