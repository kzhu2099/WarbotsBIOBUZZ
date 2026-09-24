package org.firstinspires.ftc.teamcode;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Camera 2: the upward-facing Arducam, used purely for AprilTag detection -
 * both hive tags and fixed navigation tags. This REPLACES TipperReader,
 * which (a) invented a "jar ball counting via color blobs" mechanism that
 * has no place once there is no jar, and (b) used a fabricated
 * AprilTagSingleDetection/AprilTagClusterDetection split that does not
 * exist in the real FTC SDK. The real SDK's AprilTagDetection just has
 * .id and .ftcPose (range/bearing/x/y/yaw) directly - see
 * https://ftc-docs.firstinspires.org/apriltag-detection-values.
 *
 * This class does not decide what a tag ID means - that's
 * {@link FieldTagLibrary}'s job - it only turns raw SDK detections into
 * structured, classified {@link AprilTagObservation}s.
 */
public class HiveVision extends Camera {

    private final AprilTagProcessor tagProcessor;

    public HiveVision(HardwareMap hardwareMap) {
        super(hardwareMap, HardwareConfig.HIVE_CAMERA_NAME, new Size(320, 240),
                new AprilTagProcessor.Builder().build());
        tagProcessor = processor(0);
    }

    /**
     * Every AprilTag visible this frame, classified by role and converted
     * to the units/robot-relative frame the rest of the project uses.
     * Tags whose pose couldn't be solved this frame (too far, too steep an
     * angle, or missing size metadata) are skipped rather than passed
     * through with garbage numbers.
     */
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

    /** The SDK doesn't hand back a single 0..1 confidence value, so this
     *  derives a rough one from decisionMargin (higher = a cleaner read).
     *  Not a rigorous statistic - just enough to let Hive/Localization
     *  weight sightings sensibly. TODO: tune the scale once real tag
     *  distances/angles are being tested. */
    private static double confidenceFrom(AprilTagDetection detection) {
        double normalized = detection.decisionMargin / 100.0;
        return Math.max(0.2, Math.min(1.0, normalized));
    }

    /** Quick existence check, e.g. for simple TeleOp indicators. */
    public boolean isTagVisible(int tagId) {
        if (tagProcessor == null) return false;
        for (AprilTagDetection detection : tagProcessor.getDetections()) {
            if (detection.id == tagId) return true;
        }
        return false;
    }
}
