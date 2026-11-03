package org.firstinspires.ftc.teamcode;

import android.util.Size;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

import java.util.List;

@TeleOp(name = "Camera - AprilTag Test", group = "BIOBUZZ Tests")
public class CameraTest extends OpMode {
    private VisionPortal camera;
    private AprilTagProcessor aprilTags;
    private String error;

    @Override
    public void init() {
        try {
            aprilTags = new AprilTagProcessor.Builder().build();
            camera = new VisionPortal.Builder()
                    .setCamera(hardwareMap.get(WebcamName.class, RobotConfig.CAM1))
                    .setCameraResolution(new Size(640, 480))
                    .enableLiveView(true)
                    .addProcessor(aprilTags)
                    .build();
        } catch (RuntimeException e) {
            error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        }
    }

    @Override
    public void loop() {
        if (camera == null) {
            telemetry.addData("cam1 / Webcam 2", "unavailable");
            telemetry.addData("Error", error);
        } else {
            telemetry.addData("cam1", camera.getCameraState());

            List<AprilTagDetection> detections = aprilTags.getDetections();
            telemetry.addData("AprilTags / clusters", detections.size());

            for (AprilTagDetection detection : detections) {
                if (detection instanceof AprilTagSingleDetection) {
                    AprilTagSingleDetection tag = (AprilTagSingleDetection) detection;
                    String name = tag.metadata == null ? "Unknown tag" : tag.metadata.name;
                    telemetry.addData("Tag " + tag.id, name);
                } else if (detection instanceof AprilTagClusterDetection) {
                    AprilTagClusterDetection cluster = (AprilTagClusterDetection) detection;
                    String name = cluster.metadata == null ? "Unknown cluster" : cluster.metadata.name;
                    telemetry.addData("Cluster", name);
                }
            }
            telemetry.addLine("Open Camera Stream on the Driver Station to see the image.");
        }

        telemetry.update();
    }

    @Override
    public void stop() {
        if (camera != null) camera.close();
    }
}
