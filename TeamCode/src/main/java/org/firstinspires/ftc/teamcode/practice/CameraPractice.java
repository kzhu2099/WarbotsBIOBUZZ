package org.firstinspires.ftc.teamcode.practice;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.VisionProcessor;

/**
 * PRACTICE TEMPLATE - fill in the TODOs. This is the exercise version of
 * the real Camera class (org.firstinspires.ftc.teamcode.Camera, which the
 * actual robot uses). Compare your work against
 * codestorage/CameraKey.java once you've given it a real try.
 *
 * (This used to live directly in the production package and pretend to be
 * the real Camera class - every method just returned false/null. That
 * meant the actual robot's vision never worked. It has been moved here,
 * where a fill-in-the-blank template belongs, and the production package
 * now has a real, working Camera class instead.)
 *
 * Shared by GameVisionPractice and HiveVisionPractice: opens one webcam,
 * attaches whatever processors the subclass needs, and never crashes the
 * robot if that webcam isn't plugged in yet.
 */
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
     * GameVisionPractice/HiveVisionPractice should check this (or just
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
