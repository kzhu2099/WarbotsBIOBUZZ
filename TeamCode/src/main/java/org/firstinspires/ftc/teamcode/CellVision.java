package org.firstinspires.ftc.teamcode;

import android.util.Size;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

import java.util.ArrayList;
import java.util.List;

/** Reads AprilTags from the upward-facing camera and turns valid detections into observations for the robot. */
public class CellVision extends Camera {

    private final AprilTagProcessor tagProcessor;

    public CellVision(HardwareMap hardwareMap) {
        super(hardwareMap, HardwareConfig.CELL_CAMERA_NAME, new Size(320, 240),
                new AprilTagProcessor.Builder()
                        .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
                        .setCameraPose(cameraPosition(), cameraOrientation())
                        .build());
        tagProcessor = processor(0);
    }

    private static Position cameraPosition() {
        // FTC SDK convention (robot axes): x=right+, y=forward+, z=up+.
        return new Position(DistanceUnit.INCH,
                RobotGeometry.CELL_CAMERA_LATERAL_INCHES,
                RobotGeometry.CELL_CAMERA_FORWARD_INCHES,
                RobotGeometry.CELL_CAMERA_HEIGHT_INCHES,
                0);
    }

    private static YawPitchRollAngles cameraOrientation() {
        // FTC SDK convention: yaw=pitch=roll=0 means "camera pointing
        // straight up" - exactly this camera's real orientation, which is
        // why RobotGeometry's defaults are 0, not an arbitrary placeholder.
        return new YawPitchRollAngles(AngleUnit.DEGREES,
                RobotGeometry.CELL_CAMERA_YAW_DEGREES,
                RobotGeometry.CELL_CAMERA_PITCH_DEGREES,
                RobotGeometry.CELL_CAMERA_ROLL_DEGREES,
                0);
    }

    /**
     * Every AprilTag visible this frame, classified by role and converted
     * to the units/robot-relative frame the rest of the project uses.
     * Tags whose pose couldn't be solved this frame (too far, too steep an
     * angle, or missing size metadata) are skipped rather than passed
     * through with garbage numbers.
     */
    public List<AprilTagObservation> observations(double nowSeconds) {
        List<AprilTagObservation> result = new ArrayList<>();
        if (tagProcessor == null) return result;

        for (AprilTagDetection detection : tagProcessor.getDetections()) {
            if (detection.ftcPose == null) continue;

            Pose sdkRobotPose = sdkRobotPoseOf(detection);

            if (detection instanceof AprilTagSingleDetection) {
                AprilTagSingleDetection single = (AprilTagSingleDetection) detection;
                AprilTagRole role = FieldTagLibrary.roleOf(single.id);
                // Known tag (metadata resolved by the SDK's tag library) is
                // itself meaningful evidence of a clean read.
                double confidence = single.metadata != null ? 0.85 : 0.5;

                result.add(new AprilTagObservation(
                        single.id, role,
                        detection.ftcPose.range, detection.ftcPose.bearing,
                        detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.yaw,
                        confidence, nowSeconds, sdkRobotPose));

            } else if (detection instanceof AprilTagClusterDetection) {
                // This field's cells each carry four AprilTags (see
                // FieldTagLibrary) - if BIOBUZZ's tag library registers
                // those as an actual SDK-level "cluster," readings for it
                // arrive here instead of as four separate single
                // detections, with a combined/more-robust pose.
                //
                // TODO: VERIFY on real hardware whether this branch ever
                // actually fires for this game's cell tags - there is no
                // published confirmation either way. If it never fires,
                // that's fine: every cell tag is still fully handled by
                // the AprilTagSingleDetection branch above, four
                // independent sightings reconciled onto the same Cell
                // object (see CellMap). If it DOES fire, a cluster has no
                // simple numeric ID (only cluster.metadata.name, a
                // String) - there's no confirmed real name-to-cell mapping
                // yet, so this is surfaced as IRRELEVANT/unrouted for now
                // rather than guessed at.
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

    /** The SDK's own computed robot field pose for this detection, if it
     *  could produce one (tag known to the (default, current-season) tag
     *  library, and setCameraPose() configured correctly). Null otherwise -
     *  callers fall back to this project's own estimateFieldPose() math. */
    private static Pose sdkRobotPoseOf(AprilTagDetection detection) {
        if (detection.robotPose == null) return null;
        double x = detection.robotPose.getPosition().x;
        double y = detection.robotPose.getPosition().y;
        double headingRadians = Math.toRadians(detection.robotPose.getOrientation().getYaw(AngleUnit.DEGREES));
        return new Pose(x, y, headingRadians);
    }

    /** Quick existence check, e.g. for simple TeleOp indicators. Only
     *  matches single-tag detections (a specific numeric ID isn't a
     *  meaningful question to ask of a cluster reading). */
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
