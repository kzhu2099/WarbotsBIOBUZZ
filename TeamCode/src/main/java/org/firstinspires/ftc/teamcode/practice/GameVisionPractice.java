package org.firstinspires.ftc.teamcode.practice;

import android.util.Size;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.BallDetection;
import org.firstinspires.ftc.teamcode.BallOwner;
import org.firstinspires.ftc.teamcode.BallType;
import org.firstinspires.ftc.vision.opencv.ColorBlobLocatorProcessor;
import org.firstinspires.ftc.vision.opencv.ColorRange;
import org.firstinspires.ftc.vision.opencv.ImageRegion;
import org.opencv.core.RotatedRect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PRACTICE TEMPLATE - fill in the TODOs. This is the exercise version of
 * the real GameVision class. Compare your work against
 * codestorage/GameVisionKey.java once you've given it a real try.
 *
 * Uses one webcam (facing the floor) to look for Pollen (yellow), our
 * alliance's Nectar, and the opponent's Nectar - three colors, not two,
 * because "our balls vs. opponent balls" is a real gameplay requirement,
 * not an afterthought (see the main project's audit notes on this).
 */
public class GameVisionPractice extends CameraPractice {

    private static final int FRAME_WIDTH = 320;

    // Placeholders - see the rollout steps for how to measure real ones.
    private static final double CAMERA_HORIZONTAL_FOV_DEGREES = 60;
    private static final double DISTANCE_CALIBRATION = 1000;

    private final ColorBlobLocatorProcessor pollenLocator;
    private final ColorBlobLocatorProcessor redLocator;
    private final ColorBlobLocatorProcessor blueLocator;

    public GameVisionPractice(HardwareMap hardwareMap) {
        super(hardwareMap, "Webcam 1", new Size(FRAME_WIDTH, 240),
                locator(ColorRange.YELLOW), locator(ColorRange.RED), locator(ColorRange.BLUE));
        pollenLocator = processor(0);
        redLocator = processor(1);
        blueLocator = processor(2);
    }

    /**
     * Builds one color detector for the given color range. Look at
     * ColorBlobLocatorProcessor.Builder: you'll want
     * setTargetColorRange(color), setContourMode(EXTERNAL_ONLY),
     * setRoi(ImageRegion.entireFrame()), and .build().
     */
    private static ColorBlobLocatorProcessor locator(ColorRange color) {
        return null; // TODO: implement
    }

    /**
     * This frame's detected blobs from the given locator, filtered down to
     * ones big enough to be a real game piece instead of noise. Use
     * locator.getBlobs() then ColorBlobLocatorProcessor.Util.filterByCriteria
     * with BlobCriteria.BY_CONTOUR_AREA and a min/max area.
     */
    private List<ColorBlobLocatorProcessor.Blob> blobs(ColorBlobLocatorProcessor locator) {
        return Collections.emptyList(); // TODO: implement
    }

    /** Which locator is "ours" depends on alliance, decided at query time
     *  (not baked in at construction) so toggling alliance later is safe. */
    private ColorBlobLocatorProcessor ownLocator(boolean redAlliance) {
        return null; // TODO: implement - redAlliance ? redLocator : blueLocator
    }

    private ColorBlobLocatorProcessor opponentLocator(boolean redAlliance) {
        return null; // TODO: implement - the other one from ownLocator()
    }

    /**
     * Where the biggest blob in the list is, left/right of center.
     * -1.0 = far left edge of frame, 0.0 = dead center, 1.0 = far right edge.
     * 0.0 if the list is empty.
     */
    private double offsetOf(List<ColorBlobLocatorProcessor.Blob> blobs) {
        return 0; // TODO: implement
    }

    /** Does the camera currently see at least one Pollen blob? */
    public boolean seesPollen() {
        return false; // TODO: implement
    }

    public boolean seesOwnNectar(boolean redAlliance) {
        return false; // TODO: implement
    }

    public boolean seesOpponentNectar(boolean redAlliance) {
        return false; // TODO: implement
    }

    /** Where the biggest Pollen blob is, left/right of center. See offsetOf(). */
    public double pollenOffset() {
        return 0; // TODO: implement
    }

    /**
     * Every currently-visible Pollen/Nectar blob (not just the biggest),
     * converted into a structured BallDetection at an estimated field
     * position. For each blob:
     *   - bearing (degrees) = normalized pixel offset * (FOV / 2)
     *   - distance (inches) = DISTANCE_CALIBRATION / sqrt(blob.getContourArea())
     *   - project bearing+distance, plus the robot's current pose, into a
     *     field-frame Pose (this is the same forward/lateral rotation math
     *     AprilTagObservation.estimateFieldPose() uses - look at that for
     *     the pattern once you've tried it yourself)
     * robotPose is where the robot is RIGHT NOW.
     * Double-check the sign on bearing once you can test against a real
     * ball placed to one side - flip it if detections show up mirrored.
     */
    public List<BallDetection> detections(Pose robotPose, double nowSeconds, boolean redAlliance) {
        return new ArrayList<>(); // TODO: implement
    }
}
