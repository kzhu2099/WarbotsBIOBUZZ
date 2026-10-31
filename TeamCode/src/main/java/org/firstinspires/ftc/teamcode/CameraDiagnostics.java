package org.firstinspires.ftc.teamcode;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;
import org.firstinspires.ftc.vision.opencv.ColorBlobLocatorProcessor;
import org.firstinspires.ftc.vision.opencv.ColorRange;
import org.firstinspires.ftc.vision.opencv.ImageRegion;

import java.util.List;

public final class CameraDiagnostics {
    private final VisionPortal portal;
    private final AprilTagProcessor aprilTags;
    private final ColorBlobLocatorProcessor pollen;
    private final ColorBlobLocatorProcessor redNectar;
    private final ColorBlobLocatorProcessor blueNectar;
    private final String error;

    public CameraDiagnostics(HardwareMap hardwareMap) {
        VisionPortal opened = null;
        AprilTagProcessor tags = null;
        ColorBlobLocatorProcessor yellow = null;
        ColorBlobLocatorProcessor red = null;
        ColorBlobLocatorProcessor blue = null;
        String problem = null;

        try {
            tags = new AprilTagProcessor.Builder().build();
            yellow = colorProcessor(ColorRange.YELLOW);
            red = colorProcessor(ColorRange.RED);
            blue = colorProcessor(ColorRange.BLUE);

            opened = new VisionPortal.Builder()
                    .setCamera(hardwareMap.get(WebcamName.class, RobotConfig.CAMERA))
                    .setCameraResolution(new Size(640, 480))
                    .enableLiveView(true)
                    .addProcessor(tags)
                    .addProcessor(yellow)
                    .addProcessor(red)
                    .addProcessor(blue)
                    .build();
        } catch (RuntimeException e) {
            problem = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        }

        portal = opened;
        aprilTags = tags;
        pollen = yellow;
        redNectar = red;
        blueNectar = blue;
        error = problem;
    }

    public void addTelemetry(Telemetry telemetry) {
        if (portal == null) {
            telemetry.addData("Camera", "unavailable: " + error);
            telemetry.addData("Configured name", RobotConfig.CAMERA);
            return;
        }

        telemetry.addData("Camera", portal.getCameraState());
        List<AprilTagDetection> detections = aprilTags.getDetections();
        telemetry.addData("AprilTags / clusters", detections.size());

        for (AprilTagDetection detection : detections) {
            String name = "Unknown tag";
            if (detection instanceof AprilTagSingleDetection) {
                AprilTagSingleDetection tag = (AprilTagSingleDetection) detection;
                if (tag.metadata != null) name = tag.metadata.name;
                telemetry.addData("Tag " + tag.id, name);
            } else if (detection instanceof AprilTagClusterDetection) {
                AprilTagClusterDetection cluster = (AprilTagClusterDetection) detection;
                if (cluster.metadata != null) name = cluster.metadata.name;
                telemetry.addData("Cluster", name + " (" + cluster.percentClusterFound + "%)");
            }
        }

        telemetry.addData("Yellow Pollen blobs", pollen.getBlobs().size());
        telemetry.addData("Red Nectar blobs", redNectar.getBlobs().size());
        telemetry.addData("Blue Nectar blobs", blueNectar.getBlobs().size());
        telemetry.addLine("Open Camera Stream on the Driver Station to see the image and outlines.");
        telemetry.addLine("This camera test does not control the robot or determine Hive state.");
    }

    public void close() {
        if (portal != null) portal.close();
    }

    private static ColorBlobLocatorProcessor colorProcessor(ColorRange color) {
        return new ColorBlobLocatorProcessor.Builder()
                .setTargetColorRange(color)
                .setContourMode(ColorBlobLocatorProcessor.ContourMode.EXTERNAL_ONLY)
                .setRoi(ImageRegion.entireFrame())
                .setDrawContours(true)
                .setBlurSize(5)
                .build();
    }
}
