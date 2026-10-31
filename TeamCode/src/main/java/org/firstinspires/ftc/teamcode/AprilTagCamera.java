package org.firstinspires.ftc.teamcode;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

/** The robot has one camera. This class only reports AprilTag detections. */
public final class AprilTagCamera {
    private final VisionPortal portal;
    private final AprilTagProcessor processor;
    private final String warning;

    public AprilTagCamera(HardwareMap hardwareMap) {
        VisionPortal opened = null;
        AprilTagProcessor tags = null;
        String problem = null;

        try {
            tags = new AprilTagProcessor.Builder().build();
            opened = new VisionPortal.Builder()
                    .setCamera(hardwareMap.get(WebcamName.class, RobotConfig.APRILTAG_CAMERA))
                    .setCameraResolution(new Size(640, 480))
                    .addProcessor(tags)
                    .build();
        } catch (RuntimeException e) {
            problem = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        }

        portal = opened;
        processor = tags;
        warning = problem;
    }

    public void addTelemetry(Telemetry telemetry) {
        if (processor == null) {
            telemetry.addData("AprilTag camera", "unavailable: " + warning);
            return;
        }

        List<AprilTagDetection> detections = processor.getDetections();
        StringBuilder ids = new StringBuilder();
        for (AprilTagDetection detection : detections) {
            if (ids.length() > 0) ids.append(", ");
            ids.append(detection.id);
        }
        telemetry.addData("AprilTag camera", detections.isEmpty() ? "no tags seen" : ids.toString());
    }

    public void close() {
        if (portal != null) portal.close();
    }
}
