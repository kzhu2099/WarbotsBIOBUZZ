package org.firstinspires.ftc.teamcode.codestorage;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.VisionProcessor;

// ANSWER KEY for practice/CameraPractice.java. Different class name and
// package on purpose (CameraKey, teamcode.codestorage) so this can sit in
// the real project without colliding with the template. This matches the
// real, production Camera class (org.firstinspires.ftc.teamcode.Camera)
// almost exactly - see that file's header for why the production robot
// uses its own copy rather than this one directly (this package exists
// for comparison/teaching, not to be depended on by OpModes).

public class CameraKey {

    private final VisionPortal portal;
    private final VisionProcessor[] processors;

    public CameraKey(HardwareMap hardwareMap, String webcamName, Size resolution, VisionProcessor... processors) {
        this.processors = processors;

        VisionPortal builtPortal;
        try {
            // Same failure mode as any other hardwareMap.get() with a name
            // that isn't in the current config - throws. Caught below so a
            // robot missing this specific webcam still boots.
            WebcamName webcam = hardwareMap.get(WebcamName.class, webcamName);

            VisionPortal.Builder builder = new VisionPortal.Builder()
                    .setCamera(webcam)
                    .setCameraResolution(resolution);

            // addProcessor() takes the common VisionProcessor interface, so
            // this loop works whether the caller passed ColorBlobLocator-
            // Processors, an AprilTagProcessor, or a mix of both - Camera
            // itself never needs to know which.
            for (VisionProcessor p : processors) {
                builder.addProcessor(p);
            }

            builtPortal = builder.build();
        } catch (Exception e) {
            // Webcam missing or failed to open. portal stays null; every
            // other method treats that as "nothing available" instead of
            // crashing the OpMode over one unplugged camera.
            builtPortal = null;
        }

        portal = builtPortal;
    }

    @SuppressWarnings("unchecked")
    protected <T extends VisionProcessor> T processor(int index) {
        // AUDIT FIX: the original version of this key indexed straight
        // into `processors` with no bounds check, so a mismatched index
        // (or an empty processors array) would throw
        // ArrayIndexOutOfBoundsException instead of failing the same safe
        // way isAvailable()/close() do. Now consistent with the real
        // Camera class.
        if (processors == null || index < 0 || index >= processors.length) return null;
        return (T) processors[index];
    }

    public boolean isAvailable() {
        return portal != null;
    }

    public void close() {
        if (portal != null) {
            portal.close();
        }
    }
}
