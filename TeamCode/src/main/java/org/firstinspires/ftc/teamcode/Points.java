package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * AUDITED, UNCHANGED: a generic named-pose registry, useful for whatever
 * field points a team wants to name (non-hive, non-ball - e.g. a submersible
 * entrance, a specific lineup spot). Hive-specific positioning now goes
 * through Hive/HiveMap/FieldTagLibrary instead of this, since a hive's pose
 * is something the robot actively tracks and computes targets from, not a
 * single static named point.
 */
public class Points {

    public static final double FIELD_SIZE = 144;

    private static final Map<String, Pose> points = new HashMap<>();

    public static void set(String name, Pose pose) {
        points.put(name, pose);
    }

    public static Pose get(String name) {
        Pose pose = points.get(name);
        if (pose == null) {
            throw new IllegalArgumentException("no point named " + name);
        }
        return pose;
    }

    public static boolean has(String name) {
        return points.containsKey(name);
    }

    public static void mirror(String from, String to, Function<Pose, Pose> mirrorFn) {
        set(to, mirrorFn.apply(get(from)));
    }

    public static Pose offset(double x, double y, double heading, double distance) {
        return new Pose(x + Math.cos(heading) * distance, y + Math.sin(heading) * distance, heading);
    }

    public static Pose relativeTo(Pose origin, double headingOffsetDegrees, double distance) {
        double heading = origin.heading() + Math.toRadians(headingOffsetDegrees);
        return offset(origin.x(), origin.y(), heading, distance);
    }

    public static void setRelative(String name, String originName, double headingOffsetDegrees, double distance) {
        set(name, relativeTo(get(originName), headingOffsetDegrees, distance));
    }

    public static void setRelative(String name, Pose origin, double headingOffsetDegrees, double distance) {
        set(name, relativeTo(origin, headingOffsetDegrees, distance));
    }
}
