package org.firstinspires.ftc.teamcode.practice;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.AprilTagObservation;
import org.firstinspires.ftc.teamcode.AprilTagRole;
import org.firstinspires.ftc.teamcode.FieldTagLibrary;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * PRACTICE TEMPLATE - fill in the TODOs. This is the exercise version of
 * the real HiveVision class. Compare your work against
 * codestorage/HiveVisionKey.java once you've given it a real try.
 *
 * Uses one upward-facing webcam to read AprilTags - both the tags mounted
 * on each alliance's hive, and separate fixed tags elsewhere on the field
 * usable to double-check robot position.
 *
 * IMPORTANT CORRECTION: an earlier version of this exercise (then called
 * TipperReader) had you check `detection instanceof AprilTagSingleDetection`
 * to get a tag's numeric ID. That type does not exist in the real FTC SDK -
 * it was a mistake in an earlier pass at this file. The real
 * AprilTagDetection class just has the ID directly, as detection.id. This
 * version reflects the corrected, real API - see
 * https://ftc-docs.firstinspires.org/apriltag-detection-values for the
 * full field list (id, ftcPose.range, ftcPose.bearing, ftcPose.x,
 * ftcPose.y, ftcPose.yaw, decisionMargin, and more).
 */
public class HiveVisionPractice extends CameraPractice {

    private final AprilTagProcessor tagProcessor;

    public HiveVisionPractice(HardwareMap hardwareMap) {
        super(hardwareMap, "Webcam 2", new Size(320, 240), new AprilTagProcessor.Builder().build());
        tagProcessor = processor(0);
    }

    /**
     * Every AprilTag visible this frame, turned into a structured
     * AprilTagObservation. For each detection from
     * tagProcessor.getDetections():
     *   - skip it if detection.ftcPose == null (pose wasn't solvable this
     *     frame)
     *   - look up FieldTagLibrary.roleOf(detection.id) to find out if this
     *     is a HIVE_TAG, NAVIGATION_TAG, or IRRELEVANT
     *   - pull distance/bearing/x/y/yaw straight from detection.ftcPose
     *   - confidence: there's no single built-in confidence value: try
     *     something based on detection.decisionMargin (bigger margin =
     *     cleaner read), clamped to a sane 0..1-ish range
     */
    public List<AprilTagObservation> observations(double nowSeconds) {
        return new ArrayList<>(); // TODO: implement
    }

    /** True if the given AprilTag ID is visible in this frame. */
    public boolean isTagVisible(int tagId) {
        return false; // TODO: implement
    }
}
