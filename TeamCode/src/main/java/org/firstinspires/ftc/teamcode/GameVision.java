package org.firstinspires.ftc.teamcode;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.opencv.ColorBlobLocatorProcessor;
import org.firstinspires.ftc.vision.opencv.ColorRange;
import org.firstinspires.ftc.vision.opencv.ImageRegion;
import org.opencv.core.RotatedRect;

import java.util.Collections;
import java.util.List;

// Uses the robot's camera to look for Pollen (yellow) and this alliance's
// Nectar (red or blue) balls on the field floor, for use during autonomous.
public class GameVision {

    private static final int FRAME_WIDTH = 320;

    private final VisionPortal portal;
    private final ColorBlobLocatorProcessor pollenLocator;
    private final ColorBlobLocatorProcessor nectarLocator;

    /**
     * Sets up the camera and two color detectors: one for Pollen (always
     * yellow), one for this alliance's Nectar (red or blue). If the camera
     * isn't wired up yet, this should still let the robot boot instead of
     * crashing - the rest of this class treats "no camera" as "nothing seen".
     */
    public GameVision(HardwareMap hardwareMap, boolean redAlliance) {
        // TODO: implement
        portal = null;
        pollenLocator = null;
        nectarLocator = null;
    }

    /**
     * Builds one color detector for the given color range.
     */
    private static ColorBlobLocatorProcessor locator(ColorRange color) {
        return null; // TODO: implement
    }

    /**
     * This frame's detected blobs from the given locator, filtered down to
     * ones big enough to be a real game piece instead of noise.
     */
    private List<ColorBlobLocatorProcessor.Blob> blobs(ColorBlobLocatorProcessor locator) {
        return Collections.emptyList(); // TODO: implement
    }

    /**
     * Where the biggest blob in the list is, left/right of center.
     * -1.0 = far left edge of frame, 0.0 = dead center, 1.0 = far right edge.
     * 0.0 if the list is empty.
     */
    private double offsetOf(List<ColorBlobLocatorProcessor.Blob> blobs) {
        return 0; // TODO: implement
    }

    /**
     * Releases the camera so the next OpMode can use it.
     */
    public void close() {
        // TODO: implement
    }

    /**
     * Does the camera currently see at least one Pollen blob?
     */
    public boolean seesPollen() {
        return false; // TODO: implement
    }

    /**
     * Same idea as seesPollen(), but for this alliance's Nectar color.
     */
    public boolean seesNectar() {
        return false; // TODO: implement
    }

    /**
     * Where the biggest Pollen blob is, left/right of center. See offsetOf().
     */
    public double pollenOffset() {
        return 0; // TODO: implement
    }

    /**
     * Same idea as pollenOffset(), but for this alliance's Nectar color.
     */
    public double nectarOffset() {
        return 0; // TODO: implement
    }
}