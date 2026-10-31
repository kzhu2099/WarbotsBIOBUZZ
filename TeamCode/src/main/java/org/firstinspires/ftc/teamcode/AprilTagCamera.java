package org.firstinspires.ftc.teamcode;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;
import java.util.Locale;

/** The single webcam reads the season's HIVE AprilTag clusters. */
public final class AprilTagCamera {
    private static final int STABLE_POLLS_REQUIRED = 3;

    public static final class HiveReading {
        public final RobotConfig.HiveSide upwardCell;
        public final String clusterName;
        public final double rollDegrees;
        public final double percentFound;

        HiveReading(RobotConfig.HiveSide upwardCell, String clusterName,
                    double rollDegrees, double percentFound) {
            this.upwardCell = upwardCell;
            this.clusterName = clusterName;
            this.rollDegrees = rollDegrees;
            this.percentFound = percentFound;
        }
    }

    private final VisionPortal portal;
    private final AprilTagProcessor processor;
    private final String warning;

    private RobotConfig.Alliance lastAlliance;
    private String candidateName = "";
    private int candidatePolls;
    private HiveReading stableReading;

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

    /** Returns a new stable reading only after the same upward-facing cluster is seen repeatedly. */
    public HiveReading updateHiveState(RobotConfig.Alliance alliance) {
        if (processor == null) return null;

        if (lastAlliance != alliance) {
            lastAlliance = alliance;
            candidateName = "";
            candidatePolls = 0;
            stableReading = null;
        }

        RobotConfig.HiveSide visibleSide = null;
        String visibleName = null;
        double visibleRoll = 0.0;
        double visiblePercent = 0.0;
        boolean conflictingReadings = false;

        for (AprilTagDetection detection : processor.getDetections()) {
            if (!(detection instanceof AprilTagClusterDetection) || detection.ftcPose == null) continue;

            AprilTagClusterDetection cluster = (AprilTagClusterDetection) detection;
            if (cluster.metadata == null || cluster.metadata.name == null) continue;

            String name = cluster.metadata.name.toUpperCase(Locale.US);
            if (!belongsToAlliance(name, alliance)) continue;

            RobotConfig.HiveSide side = sideFromClusterName(name);
            if (side == null) continue;

            // FTC's AprilTag-cluster guidance: |roll| < 90 degrees is the scorable, upward-facing Cell.
            double roll = detection.ftcPose.roll;
            if (Math.abs(roll) >= 90.0) continue;

            if (visibleSide != null && visibleSide != side) {
                conflictingReadings = true;
                break;
            }

            if (visibleSide == null || cluster.percentClusterFound > visiblePercent) {
                visibleSide = side;
                visibleName = name;
                visibleRoll = roll;
                visiblePercent = cluster.percentClusterFound;
            }
        }

        if (conflictingReadings || visibleSide == null) {
            candidateName = "";
            candidatePolls = 0;
            return null;
        }

        if (visibleName.equals(candidateName)) {
            candidatePolls++;
        } else {
            candidateName = visibleName;
            candidatePolls = 1;
        }

        if (candidatePolls >= STABLE_POLLS_REQUIRED) {
            stableReading = new HiveReading(visibleSide, visibleName, visibleRoll, visiblePercent);
            return stableReading;
        }

        return null;
    }

    public void addTelemetry(Telemetry telemetry) {
        if (processor == null) {
            telemetry.addData("AprilTag camera", "unavailable: " + warning);
            return;
        }

        List<AprilTagDetection> detections = processor.getDetections();
        StringBuilder clusters = new StringBuilder();

        for (AprilTagDetection detection : detections) {
            if (!(detection instanceof AprilTagClusterDetection)) continue;

            AprilTagClusterDetection cluster = (AprilTagClusterDetection) detection;
            if (cluster.metadata == null || cluster.metadata.name == null || detection.ftcPose == null) continue;

            if (clusters.length() > 0) clusters.append(" | ");
            clusters.append(cluster.metadata.name)
                    .append(" roll ").append(String.format(Locale.US, "%.1f", detection.ftcPose.roll))
                    .append(" deg, ").append(String.format(Locale.US, "%.0f", cluster.percentClusterFound))
                    .append("% found");
        }

        telemetry.addData("AprilTag camera", clusters.length() == 0 ? "no HIVE clusters visible" : clusters.toString());
        if (stableReading != null) {
            telemetry.addData("Last stable Cell", stableReading.clusterName);
        }
    }

    public boolean isAvailable() {
        return portal != null;
    }

    public void close() {
        if (portal != null) portal.close();
    }

    private static boolean belongsToAlliance(String name, RobotConfig.Alliance alliance) {
        return alliance == RobotConfig.Alliance.RED ? name.startsWith("RED ") : name.startsWith("BLUE ");
    }

    private static RobotConfig.HiveSide sideFromClusterName(String name) {
        if (name.contains("AUDIENCE")) return RobotConfig.HiveSide.AUDIENCE;
        if (name.contains("SCORING") || name.contains("OPPOSITE")) {
            return RobotConfig.HiveSide.OPPOSITE_AUDIENCE;
        }
        return null;
    }
}
