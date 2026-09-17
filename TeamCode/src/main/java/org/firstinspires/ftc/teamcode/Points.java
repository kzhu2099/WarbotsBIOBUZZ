package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;

import java.util.HashMap;
import java.util.Map;

public class Points {

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
}