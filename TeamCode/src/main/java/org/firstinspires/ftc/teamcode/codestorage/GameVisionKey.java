package org.firstinspires.ftc.teamcode.codestorage;

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

// ANSWER KEY for practice/GameVisionPractice.java.
//
// AUDIT NOTE: the previous version of this file produced
// MemoryPalace.Sighting objects and only tracked a single Nectar color (no
// our-balls-vs-opponent-balls distinction at all - the exact thing the
// project brief calls out as "not optional"). MemoryPalace no longer
// exists (renamed to BallMap, whose entries are Ball/BallDetection
// objects), and this key now matches the real, three-locator
// implementation the production GameVision class uses.

public class GameVisionKey extends CameraKey {

    private static final int FRAME_WIDTH = 320;

    // Placeholders. FOV comes from the ArduCam's spec sheet. To find
    // DISTANCE_CALIBRATION: put a ball at a known distance D (inches)
    // directly in front of the camera, read the contour area A it reports,
    // then DISTANCE_CALIBRATION = D * sqrt(A).
    private static final double CAMERA_HORIZONTAL_FOV_DEGREES = 60;
    private static final double DISTANCE_CALIBRATION = 1000;
    private static final double CAMERA_FORWARD_OFFSET_INCHES = 6.0;
    private static final double CAMERA_LATERAL_OFFSET_INCHES = 0.0;
    private static final double BASE_CONFIDENCE = 0.75;

    private final ColorBlobLocatorProcessor pollenLocator;
    private final ColorBlobLocatorProcessor redLocator;
    private final ColorBlobLocatorProcessor blueLocator;

    public GameVisionKey(HardwareMap hardwareMap) {
        super(hardwareMap, "Webcam 1", new Size(FRAME_WIDTH, 240),
                locator(ColorRange.YELLOW), locator(ColorRange.RED), locator(ColorRange.BLUE));
        // Index has to match the order those three locators were passed to
        // super() above.
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

    private List<ColorBlobLocatorProcessor.Blob> blobs(ColorBlobLocatorProcessor locator) {
        if (locator == null) return Collections.emptyList();
        List<ColorBlobLocatorProcessor.Blob> blobs = locator.getBlobs();
        ColorBlobLocatorProcessor.Util.filterByCriteria(
                ColorBlobLocatorProcessor.BlobCriteria.BY_CONTOUR_AREA, 50, 20000, blobs);
        return blobs;
    }

    // Which locator is "ours" is resolved here, at query time, from
    // whatever alliance is passed in - NOT baked into the constructor.
    // That's what makes toggling alliance after construction safe.
    private ColorBlobLocatorProcessor ownLocator(boolean redAlliance) {
        return redAlliance ? redLocator : blueLocator;
    }

    private ColorBlobLocatorProcessor opponentLocator(boolean redAlliance) {
        return redAlliance ? blueLocator : redLocator;
    }

    private double offsetOf(List<ColorBlobLocatorProcessor.Blob> blobs) {
        if (blobs.isEmpty()) return 0;
        RotatedRect box = blobs.get(0).getBoxFit();
        return (box.center.x - FRAME_WIDTH / 2.0) / (FRAME_WIDTH / 2.0);
    }

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

    public List<BallDetection> detections(Pose robotPose, double nowSeconds, boolean redAlliance) {
        List<BallDetection> result = new ArrayList<>();
        addDetections(result, pollenLocator, BallType.POLLEN, BallOwner.OURS, robotPose, nowSeconds);
        addDetections(result, ownLocator(redAlliance), BallType.NECTAR, BallOwner.OURS, robotPose, nowSeconds);
        addDetections(result, opponentLocator(redAlliance), BallType.NECTAR, BallOwner.OPPONENT, robotPose, nowSeconds);
        return result;
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
            Pose fieldPose = new Pose(fieldX, fieldY, heading);

            out.add(new BallDetection(type, owner, normalizedX, 0, bearingDegrees, distanceInches,
                    fieldPose, BASE_CONFIDENCE, nowSeconds));
        }
    }
}
