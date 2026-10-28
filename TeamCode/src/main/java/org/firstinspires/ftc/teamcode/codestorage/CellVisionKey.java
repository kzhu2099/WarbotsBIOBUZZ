package org.firstinspires.ftc.teamcode.codestorage;

import android.util.Size;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.AprilTagObservation;
import org.firstinspires.ftc.teamcode.AprilTagRole;
import org.firstinspires.ftc.teamcode.FieldTagLibrary;
import org.firstinspires.ftc.teamcode.RobotGeometry;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

import java.util.ArrayList;
import java.util.List;

// Reference implementation for the Cell vision practice exercise.
// Uses the FTC SDK's single-tag and cluster detection types.

public class CellVisionKey extends CameraKey {

    private final AprilTagProcessor tagProcessor;

    public CellVisionKey(HardwareMap hardwareMap) {
        super(hardwareMap, "Webcam 2", new Size(320, 240),
                new AprilTagProcessor.Builder()
                        .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
                        .setCameraPose(cameraPosition(), cameraOrientation())
                        .build());
        tagProcessor = processor(0);
    }

    private static Position cameraPosition() {
        return new Position(DistanceUnit.INCH,
                RobotGeometry.CELL_CAMERA_LATERAL_INCHES,
                RobotGeometry.CELL_CAMERA_FORWARD_INCHES,
                RobotGeometry.CELL_CAMERA_HEIGHT_INCHES,
                0);
    }

    private static YawPitchRollAngles cameraOrientation() {
        // In the FTC SDK's coordinate system, zero pitch and roll point straight up.
        return new YawPitchRollAngles(AngleUnit.DEGREES,
                RobotGeometry.CELL_CAMERA_YAW_DEGREES,
                RobotGeometry.CELL_CAMERA_PITCH_DEGREES,
                RobotGeometry.CELL_CAMERA_ROLL_DEGREES,
                0);
    }

    public List<AprilTagObservation> observations(double nowSeconds) {
        List<AprilTagObservation> result = new ArrayList<>();
        if (tagProcessor == null) return result;

        for (AprilTagDetection detection : tagProcessor.getDetections()) {
            if (detection.ftcPose == null) continue;

            Pose sdkRobotPose = sdkRobotPoseOf(detection);

            if (detection instanceof AprilTagSingleDetection) {
                AprilTagSingleDetection single = (AprilTagSingleDetection) detection;
                AprilTagRole role = FieldTagLibrary.roleOf(single.id);
                double confidence = single.metadata != null ? 0.85 : 0.5;

                result.add(new AprilTagObservation(
                        single.id, role,
                        detection.ftcPose.range, detection.ftcPose.bearing,
                        detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.yaw,
                        confidence, nowSeconds, sdkRobotPose));

            } else if (detection instanceof AprilTagClusterDetection) {
                // Check on the robot whether the Cell tags arrive as a cluster.
                AprilTagClusterDetection cluster = (AprilTagClusterDetection) detection;
                double confidence = cluster.percentClusterFound / 100.0;

                result.add(new AprilTagObservation(
                        -1, AprilTagRole.IRRELEVANT,
                        detection.ftcPose.range, detection.ftcPose.bearing,
                        detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.yaw,
                        confidence, nowSeconds, sdkRobotPose));
            }
        }

        return result;
    }

    private static Pose sdkRobotPoseOf(AprilTagDetection detection) {
        if (detection.robotPose == null) return null;
        double x = detection.robotPose.getPosition().x;
        double y = detection.robotPose.getPosition().y;
        double headingRadians = Math.toRadians(detection.robotPose.getOrientation().getYaw(AngleUnit.DEGREES));
        return new Pose(x, y, headingRadians);
    }

    public boolean isTagVisible(int tagId) {
        if (tagProcessor == null) return false;
        for (AprilTagDetection detection : tagProcessor.getDetections()) {
            if (detection instanceof AprilTagSingleDetection && ((AprilTagSingleDetection) detection).id == tagId) {
                return true;
            }
        }
        return false;
    }
}
