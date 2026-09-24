package org.firstinspires.ftc.teamcode;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.VisionProcessor;

/**
 * Shared by GameVision and HiveVision: opens one webcam, attaches whatever
 * processors the subclass needs, and never crashes the robot if that
 * webcam isn't plugged in / configured yet. This is the real, working
 * implementation - production code (Robot -> GameVision/HiveVision) uses
 * this directly. The fill-in-the-blank teaching version of this same idea
 * lives separately in practice/CameraPractice.java so new members can
 * build it themselves without touching (or blocking) the real robot.
 */
public class Camera {

    private final VisionPortal portal;
    private final VisionProcessor[] processors;

    /**
     * webcamName must match the name given to this camera in the Robot
     * Controller's hardware configuration. processors is whatever the
     * subclass needs attached.
     */
    public Camera(HardwareMap hardwareMap, String webcamName, Size resolution, VisionProcessor... processors) {
        this.processors = processors;

        VisionPortal builtPortal;
        try {
            // Throws if webcamName isn't in the current hardware config -
            // same failure mode as any other hardwareMap.get() with a bad
            // name. Caught below so a robot missing this specific webcam
            // still boots; every method here treats a null portal as
            // "nothing available" instead of crashing the OpMode.
            WebcamName webcam = hardwareMap.get(WebcamName.class, webcamName);

            VisionPortal.Builder builder = new VisionPortal.Builder()
                    .setCamera(webcam)
                    .setCameraResolution(resolution);

            for (VisionProcessor p : processors) {
                builder.addProcessor(p);
            }

            builtPortal = builder.build();
        } catch (Exception e) {
            builtPortal = null;
        }

        this.portal = builtPortal;
    }

    /**
     * Pulls one of this camera's processors back out by the same index it
     * was passed to super() at. Generics handle the cast, so subclasses
     * can write e.g. "ColorBlobLocatorProcessor x = processor(0);" with no
     * explicit cast.
     */
    @SuppressWarnings("unchecked")
    protected <T extends VisionProcessor> T processor(int index) {
        if (processors == null || index < 0 || index >= processors.length) return null;
        return (T) processors[index];
    }

    /** False if the webcam wasn't found or failed to open. */
    public boolean isAvailable() {
        return portal != null;
    }

    /** Releases the camera so the next OpMode can use it. Safe to call
     *  even if the camera never opened. */
    public void close() {
        if (portal != null) {
            portal.close();
        }
    }
}
