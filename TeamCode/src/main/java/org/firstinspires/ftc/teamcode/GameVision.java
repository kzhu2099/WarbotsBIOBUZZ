package org.firstinspires.ftc.teamcode;

import android.util.Size;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.vision.opencv.ColorBlobLocatorProcessor;
import org.firstinspires.ftc.vision.opencv.ColorRange;
import org.firstinspires.ftc.vision.opencv.ImageRegion;
import org.opencv.core.RotatedRect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Camera 1: the floor-facing webcam, used to find Pollen and Nectar on the
 * field. This is the REAL, working implementation - it is what
 * {@link Robot} actually uses. It intentionally detects THREE things, not
 * two, because "our balls vs. opponent balls" (project brief section 6) is
 * exactly a color question here: Pollen (yellow) is neutral/usable by
 * anyone; our-alliance-colored Nectar is ours; the other alliance's colored
 * Nectar is the opponent's and must never be treated as a target.
 *
 * Produces structured {@link BallDetection}s (type, owner, camera-relative
 * position, bearing/distance estimate, confidence, timestamp) rather than
 * a bare "boolean seesBall()" - see project brief section 5.
 *
 * IMPORTANT AUDIT FIX: an earlier version of this class baked "which color
 * is ours" into the constructor. Robot.toggleAlliance() can flip alliance
 * at any time (e.g. during init), which would have silently left this
 * class pointed at the wrong color - "our" nectar and the opponent's would
 * swap without anything telling you. This version tracks a plain RED
 * locator and a plain BLUE locator, and only decides which one is "ours"
 * at query time, from whatever the current alliance actually is. That
 * makes toggling alliance safe at any point, including after this class
 * is constructed.
 */
public class GameVision extends Camera {

    private static final int FRAME_WIDTH = 320;

    // Placeholders - see GameVisionKey/the rollout notes for how to
    // measure real ones once the camera is actually mounted.
    // TODO: VERIFY once camera is mounted on the real robot.
    private static final double CAMERA_HORIZONTAL_FOV_DEGREES = 60;
    private static final double DISTANCE_CALIBRATION = 1000;
    private static final double MAX_USABLE_RANGE_INCHES = 60;

    /** Where the ball camera sits relative to the robot's tracked center
     *  point (forward +, lateral + = right), inches. TODO: VERIFY. */
    private static final double CAMERA_FORWARD_OFFSET_INCHES = 6.0;
    private static final double CAMERA_LATERAL_OFFSET_INCHES = 0.0;

    private static final double MIN_BLOB_AREA = 50;
    private static final double MAX_BLOB_AREA = 20000;

    /** Fixed detection confidence for a passing blob. Not a real per-blob
     *  statistical confidence (the SDK doesn't provide one) - a
     *  placeholder that leaves room to later scale by blob size/density if
     *  false positives turn out to be a problem. */
    private static final double BASE_CONFIDENCE = 0.75;

    private final ColorBlobLocatorProcessor pollenLocator;
    private final ColorBlobLocatorProcessor redLocator;
    private final ColorBlobLocatorProcessor blueLocator;

    public GameVision(HardwareMap hardwareMap) {
        super(hardwareMap, HardwareConfig.BALL_CAMERA_NAME, new Size(FRAME_WIDTH, 240),
                locator(ColorRange.YELLOW), locator(ColorRange.RED), locator(ColorRange.BLUE));
        // Index order matches the three locators passed to super() above.
        pollenLocator = processor(0);
        redLocator = processor(1);
        blueLocator = processor(2);
    }

    private static ColorBlobLocatorProcessor locator(ColorRange color) {
        return new ColorBlobLocatorProcessor.Builder()
                .setTargetColorRange(color)
                .setContourMode(ColorBlobLocatorProcessor.ContourMode.EXTERNAL_ONLY)
                .setRoi(ImageRegion.entireFrame())
                .setDrawContours(true)
                .setBlurSize(5)
                .build();
    }

    private ColorBlobLocatorProcessor ownLocator(boolean redAlliance) {
        return redAlliance ? redLocator : blueLocator;
    }

    private ColorBlobLocatorProcessor opponentLocator(boolean redAlliance) {
        return redAlliance ? blueLocator : redLocator;
    }

    private List<ColorBlobLocatorProcessor.Blob> blobs(ColorBlobLocatorProcessor locator) {
        if (locator == null) return Collections.emptyList();
        List<ColorBlobLocatorProcessor.Blob> blobs = locator.getBlobs();
        ColorBlobLocatorProcessor.Util.filterByCriteria(
                ColorBlobLocatorProcessor.BlobCriteria.BY_CONTOUR_AREA, MIN_BLOB_AREA, MAX_BLOB_AREA, blobs);
        return blobs;
    }

    private double offsetOf(List<ColorBlobLocatorProcessor.Blob> blobs) {
        if (blobs.isEmpty()) return 0;
        RotatedRect box = blobs.get(0).getBoxFit();
        return (box.center.x - FRAME_WIDTH / 2.0) / (FRAME_WIDTH / 2.0);
    }

    // ---- Quick driver-assist queries (TeleOp) --------------------------

    public boolean seesPollen() {
        return !blobs(pollenLocator).isEmpty();
    }

    public boolean seesOwnNectar(boolean redAlliance) {
        return !blobs(ownLocator(redAlliance)).isEmpty();
    }

    public boolean seesOpponentNectar(boolean redAlliance) {
        return !blobs(opponentLocator(redAlliance)).isEmpty();
    }

    public double pollenOffset() {
        return offsetOf(blobs(pollenLocator));
    }

    // ---- Structured detections (planner) -------------------------------

    /**
     * Every currently-visible Pollen/Nectar blob this frame, classified
     * and projected into field coordinates using the robot's current pose
     * and CURRENT alliance. Feed this straight into BallMap.observe().
     */
    public List<BallDetection> detections(Pose robotPose, double nowSeconds, boolean redAlliance) {
        List<BallDetection> result = new ArrayList<>();
        addDetections(result, pollenLocator, BallType.POLLEN, BallOwner.OURS, robotPose, nowSeconds);
        addDetections(result, ownLocator(redAlliance), BallType.NECTAR, BallOwner.OURS, robotPose, nowSeconds);
        addDetections(result, opponentLocator(redAlliance), BallType.NECTAR, BallOwner.OPPONENT, robotPose, nowSeconds);
        return result;
    }

    /** The field-of-view model this camera uses, for BallMap's "should
     *  this ball have been visible" staleness checks - kept alongside the
     *  camera's own FOV/range constants so the two can never drift apart. */
    public BallMap.FieldOfView fieldOfView() {
        return new BallMap.ConeFieldOfView(Math.toRadians(CAMERA_HORIZONTAL_FOV_DEGREES / 2.0), MAX_USABLE_RANGE_INCHES);
    }

    private void addDetections(List<BallDetection> out, ColorBlobLocatorProcessor locator,
                                BallType type, BallOwner owner, Pose robotPose, double nowSeconds) {
        for (ColorBlobLocatorProcessor.Blob blob : blobs(locator)) {
            RotatedRect box = blob.getBoxFit();
            double normalizedX = (box.center.x - FRAME_WIDTH / 2.0) / (FRAME_WIDTH / 2.0);
            double bearingDegrees = normalizedX * (CAMERA_HORIZONTAL_FOV_DEGREES / 2.0);
            double distanceInches = DISTANCE_CALIBRATION / Math.sqrt(blob.getContourArea());

            double bearingRadians = Math.toRadians(bearingDegrees);
            double robotRelForward = CAMERA_FORWARD_OFFSET_INCHES + distanceInches * Math.cos(bearingRadians);
            double robotRelLateral = CAMERA_LATERAL_OFFSET_INCHES + distanceInches * Math.sin(bearingRadians);

            double heading = robotPose.heading();
            double fieldX = robotPose.x() + robotRelForward * Math.cos(heading) - robotRelLateral * Math.sin(heading);
            double fieldY = robotPose.y() + robotRelForward * Math.sin(heading) + robotRelLateral * Math.cos(heading);
            // A ball has no meaningful heading of its own; store the
            // robot's heading at capture time so downstream code that
            // expects a Pose still has something sane if it ever reads it.
            Pose fieldPose = new Pose(fieldX, fieldY, heading);

            out.add(new BallDetection(type, owner, normalizedX, 0, bearingDegrees, distanceInches,
                    fieldPose, BASE_CONFIDENCE, nowSeconds));
        }
    }
}
