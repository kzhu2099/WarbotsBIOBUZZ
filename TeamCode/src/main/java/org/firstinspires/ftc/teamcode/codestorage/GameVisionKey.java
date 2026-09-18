package org.firstinspires.ftc.teamcode.codestorage;

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

// ANSWER KEY - this is the filled-in version of the GameVision template.
// It is not meant to sit in the project next to GameVision.java (same class
// name, same package - two copies won't compile together). Use it to check
// work or explain the reasoning, then delete it or keep it outside src/.

public class GameVisionKey {

    // Camera images are analyzed at a fixed resolution, not whatever the
    // camera's native size is - smaller means faster processing, and we
    // don't need much detail to tell "there's a yellow blob over there."
    // Every pixel measurement in this class (offsets, filter sizes) is only
    // meaningful relative to this number, so it lives in one place.
    private static final int FRAME_WIDTH = 320;

    // VisionPortal is the thing that actually owns the camera and runs
    // processors on every frame. A "processor" is a plug-in that looks at
    // each frame and extracts something - here, colored blobs.
    private final VisionPortal portal;
    private final ColorBlobLocatorProcessor pollenLocator;
    private final ColorBlobLocatorProcessor nectarLocator;

    public GameVisionKey(HardwareMap hardwareMap, boolean redAlliance) {
        VisionPortal builtPortal;
        ColorBlobLocatorProcessor pollen;
        ColorBlobLocatorProcessor nectar;

        try {
            // hardwareMap.get() throws if "Webcam 1" isn't in the current
            // robot config - same failure mode as a motor with the wrong
            // name. We catch that below so a robot without a camera wired
            // up yet still boots; vision methods just report nothing seen.
            WebcamName webcam = hardwareMap.get(WebcamName.class, "Webcam 1");

            // Two separate locators because a blob detector only looks for
            // ONE color range at a time. Pollen is always yellow. Nectar's
            // color depends on which alliance we are, so we pick RED or
            // BLUE based on the redAlliance flag passed in.
            pollen = locator(ColorRange.YELLOW);
            nectar = locator(redAlliance ? ColorRange.RED : ColorRange.BLUE);

            // A VisionPortal can run several processors on the same camera
            // feed at once - that's why both locators get added to the one
            // portal instead of needing two cameras.
            builtPortal = new VisionPortal.Builder()
                    .setCamera(webcam)
                    .addProcessor(pollen)
                    .addProcessor(nectar)
                    .setCameraResolution(new Size(FRAME_WIDTH, 240))
                    .build();
        } catch (Exception e) {
            // Camera not configured yet, or failed to open. Everything
            // downstream treats null locators as "nothing detected" instead
            // of crashing the whole robot over a missing webcam.
            builtPortal = null;
            pollen = null;
            nectar = null;
        }

        portal = builtPortal;
        pollenLocator = pollen;
        nectarLocator = nectar;
    }

    // Builds one color locator. Pulled into its own method because we need
    // the exact same setup twice (once per color) and repeating a five-line
    // builder chain with one word changed is how bugs sneak in.
    private static ColorBlobLocatorProcessor locator(ColorRange color) {
        return new ColorBlobLocatorProcessor.Builder()
                .setTargetColorRange(color)
                // EXTERNAL_ONLY: only the outer outline of each blob matters
                // to us. A game ball won't have meaningful holes in it, so
                // we don't need the processor to also track internal
                // contours - that's wasted CPU time for no benefit here.
                .setContourMode(ColorBlobLocatorProcessor.ContourMode.EXTERNAL_ONLY)
                // entireFrame(): look at the whole camera image. Once the
                // camera is actually mounted on the robot, narrowing this to
                // just the floor area in front of it (using
                // ImageRegion.asUnityCenterCoordinates(...)) will cut down
                // on false positives from anything outside the field.
                .setRoi(ImageRegion.entireFrame())
                // Draws the detected outline on the camera preview stream so
                // you can SEE what it's picking up while testing. Costs a
                // little performance; harmless to leave on.
                .setDrawContours(true)
                // Blurring the image slightly before color-matching smooths
                // out small lighting variations and pixel noise, so you get
                // one solid blob instead of a speckled cluster of tiny ones.
                .setBlurSize(5)
                .build();
    }

    // Every frame, a locator can report a bunch of blobs, including tiny
    // ones from stray reflections or noise. This filters those out by area
    // (in pixels) so only real, roughly-ball-sized blobs remain. The 50/
    // 20000 bounds are a starting guess - once the camera is actually
    // mounted at its real height and angle, watch the telemetry/preview and
    // tighten these to match how big a real Pollen or Nectar ball actually
    // looks on screen at typical pickup distance.
    private List<ColorBlobLocatorProcessor.Blob> blobs(ColorBlobLocatorProcessor locator) {
        if (locator == null) return Collections.emptyList();
        List<ColorBlobLocatorProcessor.Blob> blobs = locator.getBlobs();
        ColorBlobLocatorProcessor.Util.filterByCriteria(
                ColorBlobLocatorProcessor.BlobCriteria.BY_CONTOUR_AREA, 50, 20000, blobs);
        return blobs;
    }

    // getBlobs() returns blobs sorted biggest-first already, so blobs.get(0)
    // is "the blob we're most confident is real," not an arbitrary pick.
    // getBoxFit() is the smallest rectangle that fully contains the blob;
    // its .center.x is the blob's horizontal pixel position on screen.
    //
    // Raw pixel position isn't useful by itself (depends on resolution), so
    // we convert it to a -1..1 scale relative to the middle of the frame:
    //   center.x == 0            -> -1.0  (touching the left edge)
    //   center.x == FRAME_WIDTH/2 ->  0.0  (dead center)
    //   center.x == FRAME_WIDTH   -> +1.0  (touching the right edge)
    // That -1..1 number is easy to turn into a turning direction later
    // ("offset > 0 means turn right") without caring what resolution the
    // camera happens to be running at.
    private double offsetOf(List<ColorBlobLocatorProcessor.Blob> blobs) {
        if (blobs.isEmpty()) return 0;
        RotatedRect box = blobs.get(0).getBoxFit();
        return (box.center.x - FRAME_WIDTH / 2.0) / (FRAME_WIDTH / 2.0);
    }

    // VisionPortal holds the camera open. If it's never released, the next
    // OpMode that tries to open the same camera can fail to start. Robot
    // calls this from stop(), matching how intake()/launch() get shut off
    // there too - anything the robot turned on, stop() turns back off.
    public void close() {
        if (portal != null) {
            portal.close();
        }
    }

    // This and the three methods below are the ones left blank in the
    // template. Each is a thin, one-line composition of blobs()/offsetOf()
    // above - the point of leaving these out isn't that they're hard, it's
    // that they're the smallest possible unit someone new can own end to
    // end: pick the right locator field, call the right helper, done.
    public boolean seesPollen() {
        return !blobs(pollenLocator).isEmpty();
    }

    // Same shape as seesPollen(), swapped to the Nectar locator. Two nearly
    // identical methods instead of one method with a boolean/color argument
    // on purpose - callers elsewhere (Robot, Auto) read as
    // "robot.vision.seesPollen()", not "robot.vision.sees(POLLEN)", which
    // is easier to search for and harder to call with the wrong color.
    public boolean seesNectar() {
        return !blobs(nectarLocator).isEmpty();
    }

    public double pollenOffset() {
        return offsetOf(blobs(pollenLocator));
    }

    public double nectarOffset() {
        return offsetOf(blobs(nectarLocator));
    }
}