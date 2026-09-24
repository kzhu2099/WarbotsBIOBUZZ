package org.firstinspires.ftc.teamcode.codestorage;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.AprilTagObservation;
import org.firstinspires.ftc.teamcode.AprilTagRole;
import org.firstinspires.ftc.teamcode.FieldTagLibrary;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.ArrayList;
import java.util.List;

// ANSWER KEY for practice/HiveVisionPractice.java. Replaces the previous
// TipperReaderKey.java.
//
// AUDIT NOTE - two separate problems with the file this replaces:
//   1. It checked `detection instanceof AprilTagSingleDetection` to read a
//      tag's ID. That type is not part of the real FTC SDK - it was a
//      fabricated API that would not have compiled against the real
//      vision library. The real AprilTagDetection has .id directly.
//   2. It counted "balls in the jar" via three more color locators on this
//      same upward camera. There is no jar. Robot inventory is now
//      tracked through ScoringMechanism's actual intake/transfer sensors
//      (see project brief section 8/9/21) - a real physical signal, not a
//      webcam guess at how full a container looks from above.
//
// This camera now does exactly one job: read AprilTags, and say what they
// mean (hive tag vs. navigation tag vs. irrelevant) via FieldTagLibrary.

public class HiveVisionKey extends CameraKey {

    private final AprilTagProcessor tagProcessor;

    public HiveVisionKey(HardwareMap hardwareMap) {
        super(hardwareMap, "Webcam 2", new Size(320, 240), new AprilTagProcessor.Builder().build());
        tagProcessor = processor(0);
    }

    public List<AprilTagObservation> observations(double nowSeconds) {
        List<AprilTagObservation> result = new ArrayList<>();
        if (tagProcessor == null) return result;

        for (AprilTagDetection detection : tagProcessor.getDetections()) {
            if (detection.ftcPose == null) continue;

            AprilTagRole role = FieldTagLibrary.roleOf(detection.id);
            double confidence = confidenceFrom(detection);

            result.add(new AprilTagObservation(
                    detection.id,
                    role,
                    detection.ftcPose.range,
                    detection.ftcPose.bearing,
                    detection.ftcPose.x,
                    detection.ftcPose.y,
                    detection.ftcPose.yaw,
                    confidence,
                    nowSeconds));
        }

        return result;
    }

    private static double confidenceFrom(AprilTagDetection detection) {
        double normalized = detection.decisionMargin / 100.0;
        return Math.max(0.2, Math.min(1.0, normalized));
    }

    public boolean isTagVisible(int tagId) {
        if (tagProcessor == null) return false;
        for (AprilTagDetection detection : tagProcessor.getDetections()) {
            if (detection.id == tagId) return true;
        }
        return false;
    }
}
