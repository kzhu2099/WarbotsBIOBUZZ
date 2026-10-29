package org.firstinspires.ftc.teamcode.practice;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.VisionProcessor;

/** undefined */
public class CameraPractice {

    private final VisionPortal portal;
    private final VisionProcessor[] processors;

    /**
     * webcamName must match the name given to this camera in the Robot
     * Controller's hardware config. processors is whatever the subclass
     * needs attached (built before super() runs, since that's legal even
     * though declaring local variables first and passing them isn't).
     */
    public CameraPractice(HardwareMap hardwareMap, String webcamName, Size resolution, VisionProcessor... processors) {
        // TODO: implement. You'll need:
        //   - hardwareMap.get(WebcamName.class, webcamName) to find the
        //     camera - wrap this (and the rest of this constructor) in a
        //     try/catch so a missing webcam doesn't crash the OpMode.
        //   - a new VisionPortal.Builder(), .setCamera(...),
        //     .setCameraResolution(resolution), .addProcessor(...) for
        //     each processor, then .build().
        //   - store the built portal (or null, if it failed) in `portal`.
        this.processors = processors;
        this.portal = null;
    }

    /**
     * For a subclass to pull one of its processors back out by the same
     * index it was passed in to super() at. Cast happens automatically via
     * generics, so callers can write
     * "ColorBlobLocatorProcessor x = processor(0);" with no cast needed.
     */
    @SuppressWarnings("unchecked")
    protected <T extends VisionProcessor> T processor(int index) {
        return (T) processors[index];
    }

    /**
     * False if the webcam wasn't found or failed to open. Every method in
     * GameVisionPractice/CellVisionPractice should check this (or just
     * check whatever they built off of it) before trusting any detection.
     */
    public boolean isAvailable() {
        return false; // TODO: implement
    }

    /**
     * Releases the camera so the next OpMode can use it.
     */
    public void close() {
        // TODO: implement
    }
}
