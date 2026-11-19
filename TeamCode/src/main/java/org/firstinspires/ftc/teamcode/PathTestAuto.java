package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pedropathing.api.Paths.line;

/**
 * Drive-only BIOBUZZ path test. It initializes four drive motors and Pedro,
 * but never initializes or commands intake, transfer, outtake, or cameras.
 */
@Autonomous(name = "TEST - Pedro Path (Drive Only)", group = "BIOBUZZ Tests")
public class PathTestAuto extends OpMode {
    private static final class PathStep {
        final String name;
        final Pose target;

        PathStep(String name, Pose target) {
            this.name = name;
            this.target = target;
        }
    }

    private boolean redAlliance = true;
    private int startRow = 0;
    private FieldPoses.Lane lane = FieldPoses.Lane.INSIDE;
    private FieldPoses.Flower flower = FieldPoses.Flower.RED_WALL;
    private FieldPoses.BypassSide bypassSide = FieldPoses.BypassSide.AUDIENCE;

    private RobotHardware drive;
    private Follower follower;
    private final List<PathStep> route = new ArrayList<>();

    private Pose startPose;
    private String routeError;
    private String hardwareError;
    private int nextStep;
    private PathStep activeStep;
    private double pathStartedAt;
    private boolean followingPath;
    private boolean finished;

    @Override
    public void init() {
        try {
            drive = new RobotHardware(hardwareMap);
            if (!drive.driveReady()) {
                hardwareError = "Missing drive motors: " + drive.missingDriveMotors();
                return;
            }
            follower = Constants.create(hardwareMap);
        } catch (RuntimeException exception) {
            hardwareError = errorText(exception);
            if (drive != null) drive.stopDrive();
        }
    }

    @Override
    public void init_loop() {
        readSelections();
        buildRoute();

        showFieldAndPlacement();
        showRoute();

        if (routeError != null) {
            telemetry.addData("ROUTE BLOCKED", routeError);
        } else {
            telemetry.addData("ROUTE CHECK", "Preset points and straight segments clear modeled Hive keep-out.");
        }

        if (hardwareError != null) {
            telemetry.addData("DRIVE HARDWARE NOT READY", hardwareError);
        } else if (follower == null) {
            telemetry.addData("PEDRO NOT READY", "Follower did not initialize.");
        } else {
            telemetry.addData("PEDRO", "Settings present; physically verify motor and Pinpoint directions before moving.");
        }

        telemetry.addLine("DRIVE ONLY: intake, transfer, outtakes, and camera are not initialized.");
        telemetry.addLine("Press Play only after matching the displayed robot center and heading.");
        telemetry.update();
    }

    private void readSelections() {
        if (gamepad1.xWasPressed()) redAlliance = !redAlliance;

        if (gamepad1.dpadLeftWasPressed()) {
            startRow = (startRow + 5) % 6;
        } else if (gamepad1.dpadRightWasPressed()) {
            startRow = (startRow + 1) % 6;
        }

        if (gamepad1.dpadDownWasPressed()) {
            int count = FieldPoses.Flower.values().length;
            flower = FieldPoses.Flower.values()[(flower.ordinal() + count - 1) % count];
        } else if (gamepad1.dpadUpWasPressed()) {
            int count = FieldPoses.Flower.values().length;
            flower = FieldPoses.Flower.values()[(flower.ordinal() + 1) % count];
        }

        if (gamepad1.aWasPressed()) {
            lane = lane == FieldPoses.Lane.INSIDE
                    ? FieldPoses.Lane.OUTSIDE : FieldPoses.Lane.INSIDE;
        }

        if (gamepad1.bWasPressed()) {
            bypassSide = bypassSide == FieldPoses.BypassSide.AUDIENCE
                    ? FieldPoses.BypassSide.SCORING : FieldPoses.BypassSide.AUDIENCE;
        }
    }

    private void buildRoute() {
        route.clear();
        routeError = null;
        startPose = FieldPoses.start(redAlliance, startRow);

        // These points are kept separately so the return route retraces the
        // lane and any bypass, but does not re-approach the Hive safe stop.
        List<Pose> returnWaypoints = new ArrayList<>();

        Pose laneEntry = FieldPoses.laneEntry(redAlliance, lane, startRow);
        addMove("START -> " + laneName() + " LANE", laneEntry);
        returnWaypoints.add(laneEntry);

        Pose hiveRow = FieldPoses.hiveLane(redAlliance, lane, bypassSide);
        addMove("FOLLOW " + laneName() + " LANE TO " + bypassSideName() + " HIVE ROW", hiveRow);
        returnWaypoints.add(hiveRow);

        Pose hiveSafeStop = FieldPoses.hiveSafeStop(redAlliance, bypassSide);
        addMove("HIVE SAFE STOP (OUTSIDE FRAME; NOT A SCORING POSE)", hiveSafeStop);
        addMove("BACK OUT TO " + laneName() + " LANE", hiveRow);

        boolean crossToOtherHalf = FieldPoses.crossesToOtherAllianceHalf(redAlliance, flower);
        boolean flowerLaneIsRed = FieldPoses.flowerLaneIsRed(redAlliance, flower);

        if (crossToOtherHalf) {
            Pose nearBypass = FieldPoses.bypass(redAlliance, lane, bypassSide);
            Pose farBypass = FieldPoses.bypass(!redAlliance, lane, bypassSide);

            addMove("MOVE TO " + bypassSideName() + " BYPASS", nearBypass);
            returnWaypoints.add(nearBypass);

            addMove("CROSS BEHIND HIVE TO " + (flowerLaneIsRed ? "RED / LEFT" : "BLUE / RIGHT") + " LANE",
                    farBypass);
            returnWaypoints.add(farBypass);
        }

        Pose flowerLaneRow = FieldPoses.flowerLane(flowerLaneIsRed, lane, flower);
        addMove("FOLLOW LANE TO " + flower.label + " ROW", flowerLaneRow);
        returnWaypoints.add(flowerLaneRow);

        addMove("FLOWER ROBOT STOP (FRONT FACES FLOWER)", flower.robotStop);

        for (int i = returnWaypoints.size() - 1; i >= 0; i--) {
            addMove("RETURN THROUGH " + formatPose(returnWaypoints.get(i)), returnWaypoints.get(i));
        }
        addMove("RETURN TO SELECTED START", startPose);

        routeError = checkWholeRoute();
    }

    private void addMove(String name, Pose target) {
        route.add(new PathStep(name, target));
    }

    private String checkWholeRoute() {
        String startError = checkRobotInsideField(startPose, "SELECTED START");
        if (startError != null) return startError;

        Pose previous = startPose;
        for (int i = 0; i < route.size(); i++) {
            Pose target = route.get(i).target;

            String pointError = checkRobotInsideField(target, route.get(i).name);
            if (pointError != null) return pointError;

            if (Math.abs(previous.x() - target.x()) > 0.001
                    && Math.abs(previous.y() - target.y()) > 0.001) {
                return "Non-orthogonal segment before " + route.get(i).name
                        + ": " + formatPose(previous) + " -> " + formatPose(target);
            }

            if (segmentIntersectsHiveKeepOut(previous, target)) {
                return "Robot footprint plus " + FieldConfig.HIVE_CLEARANCE_INCHES
                        + " in clearance intersects the CAD Hive keep-out before "
                        + route.get(i).name + ": " + formatPose(previous)
                        + " -> " + formatPose(target);
            }
            previous = target;
        }
        return null;
    }

    private static String checkRobotInsideField(Pose pose, String label) {
        double halfWidth = FieldConfig.ROBOT_WIDTH_INCHES / 2.0;
        double halfLength = FieldConfig.ROBOT_LENGTH_INCHES / 2.0;
        if (pose.x() - halfWidth < 0 || pose.x() + halfWidth > FieldConfig.FIELD_SIZE_INCHES
                || pose.y() - halfLength < 0 || pose.y() + halfLength > FieldConfig.FIELD_SIZE_INCHES) {
            return label + " puts part of the 18 x 18 in robot outside the modeled field: "
                    + formatPose(pose);
        }
        return null;
    }

    /**
     * Every planned segment is axis-aligned. This conservatively checks its
     * swept 18 x 18 in robot footprint against the CAD Hive base footprint
     * expanded by the specified additional clearance.
     */
    private static boolean segmentIntersectsHiveKeepOut(Pose from, Pose to) {
        double expandX = FieldConfig.ROBOT_WIDTH_INCHES / 2.0 + FieldConfig.HIVE_CLEARANCE_INCHES;
        double expandY = FieldConfig.ROBOT_LENGTH_INCHES / 2.0 + FieldConfig.HIVE_CLEARANCE_INCHES;

        double keepOutMinX = FieldConfig.HIVE_FRAME_MIN_X_INCHES - expandX;
        double keepOutMaxX = FieldConfig.HIVE_FRAME_MAX_X_INCHES + expandX;
        double keepOutMinY = FieldConfig.HIVE_FRAME_MIN_Y_INCHES - expandY;
        double keepOutMaxY = FieldConfig.HIVE_FRAME_MAX_Y_INCHES + expandY;

        double segmentMinX = Math.min(from.x(), to.x());
        double segmentMaxX = Math.max(from.x(), to.x());
        double segmentMinY = Math.min(from.y(), to.y());
        double segmentMaxY = Math.max(from.y(), to.y());

        boolean overlapsX = segmentMaxX >= keepOutMinX && segmentMinX <= keepOutMaxX;
        boolean overlapsY = segmentMaxY >= keepOutMinY && segmentMinY <= keepOutMaxY;
        return overlapsX && overlapsY;
    }

    private void showFieldAndPlacement() {
        Pose start = FieldPoses.start(redAlliance, startRow);
        String alliance = redAlliance ? "RED / LEFT" : "BLUE / RIGHT";

        telemetry.addData("ALLIANCE", alliance);
        telemetry.addData("START ROW", (startRow + 1) + " of 6 (Row 1 = Audience/front)");
        telemetry.addData("PLACE ROBOT CENTER", formatPose(start));
        telemetry.addData("PLACE INSTRUCTIONS", startPlacementText());
        telemetry.addData("SELECTED LANE", laneName());
        telemetry.addData("HIVE ROW / BYPASS", bypassSideName());
        telemetry.addData("FLOWER TARGET", flower.label);
        telemetry.addData("PHYSICAL FLOWER CENTER", flower.center.toString() + " in");
        telemetry.addData("ROBOT STOP AT FLOWER", formatPose(flower.robotStop));
        telemetry.addData("RED AUDIENCE CELL CENTER", FieldPoses.RED_AUDIENCE_CELL_CENTER.toString());
        telemetry.addData("RED SCORING CELL CENTER", FieldPoses.RED_SCORING_CELL_CENTER.toString());
        telemetry.addData("BLUE AUDIENCE CELL CENTER", FieldPoses.BLUE_AUDIENCE_CELL_CENTER.toString());
        telemetry.addData("BLUE SCORING CELL CENTER", FieldPoses.BLUE_SCORING_CELL_CENTER.toString());
        telemetry.addData("FIELD MODEL", "141.348 x 141.348 in inside-wall CAD span; event may vary.");
        telemetry.addLine("ORIGIN: Red/left + Audience/front. +X right; +Y toward Scoring.");
        telemetry.addLine("HEADINGS: 0 right; 90 back; 180 left; 270 front.");
        telemetry.addLine("CONTROLS: X alliance | D-pad L/R row | U/D Flower | A lane | B bypass side.");
    }

    private String startPlacementText() {
        Pose start = FieldPoses.start(redAlliance, startRow);
        if (redAlliance) {
            return String.format(Locale.US,
                    "LEFT bumper flush to Red/left inside wall; center=(9.000, %.3f); front RIGHT; heading 0 deg.",
                    start.y());
        }
        return String.format(Locale.US,
                "RIGHT bumper flush to Blue/right inside wall; center=(132.348, %.3f); front LEFT; heading 180 deg.",
                start.y());
    }

    private void showRoute() {
        telemetry.addLine("----- COMPLETE DRIVE ROUTE (ROBOT CENTER) -----");
        telemetry.addData("START", formatPose(startPose));
        for (int i = 0; i < route.size(); i++) {
            PathStep step = route.get(i);
            telemetry.addData(String.format(Locale.US, "%02d", i + 1),
                    step.name + " -> " + formatPose(step.target));
        }
    }

    private String laneName() {
        return lane == FieldPoses.Lane.INSIDE
                ? "INSIDE (closer to Hive)" : "OUTSIDE (closer to alliance wall)";
    }

    private String bypassSideName() {
        return bypassSide == FieldPoses.BypassSide.AUDIENCE
                ? "AUDIENCE / FRONT" : "SCORING / BACK";
    }

    private static String formatPose(Pose pose) {
        return String.format(Locale.US, "(%.3f, %.3f) in, heading %.0f deg [%s]",
                pose.x(), pose.y(), headingDegrees(pose), directionName(pose));
    }

    private static double headingDegrees(Pose pose) {
        double degrees = Math.toDegrees(pose.heading()) % 360.0;
        return degrees < 0 ? degrees + 360.0 : degrees;
    }

    private static String directionName(Pose pose) {
        int heading = (int) Math.round(headingDegrees(pose)) % 360;
        switch (heading) {
            case 0: return "front RIGHT / +X";
            case 90: return "front BACK / +Y";
            case 180: return "front LEFT / -X";
            case 270: return "front AUDIENCE / -Y";
            default: return "front at " + heading + " deg";
        }
    }

    private static String errorText(RuntimeException exception) {
        return exception.getMessage() == null
                ? exception.getClass().getSimpleName() : exception.getMessage();
    }

    @Override
    public void start() {
        if (drive == null || !drive.driveReady() || follower == null
                || hardwareError != null || routeError != null) {
            if (drive != null) drive.stopDrive();
            telemetry.addLine("PATH TEST NOT STARTED");
            telemetry.addData("Hardware", hardwareError == null ? "ready" : hardwareError);
            telemetry.addData("Route", routeError == null ? "clear" : routeError);
            telemetry.update();
            return;
        }

        try {
            follower.setPose(startPose);
            follower.update();
            nextStep = 0;
            finished = false;
            followingPath = false;
            startNextStep();
        } catch (RuntimeException exception) {
            hardwareError = errorText(exception);
            stopPath();
        }
    }

    @Override
    public void loop() {
        if (drive == null || follower == null || hardwareError != null) {
            if (drive != null) drive.stopDrive();
            telemetry.addData("PATH TEST STOPPED",
                    hardwareError == null ? "Drive hardware not ready." : hardwareError);
            telemetry.update();
            return;
        }

        if (finished) {
            drive.stopDrive();
            telemetry.addData("PATH TEST", "complete — drive stopped");
            telemetry.addData("ROBOT CENTER", formatPose(follower.pose()));
            telemetry.update();
            return;
        }

        try {
            follower.update();

            if (followingPath && !follower.isBusy()) {
                followingPath = false;
                follower.stop();
                drive.stopDrive();
                startNextStep();
            } else if (followingPath
                    && getRuntime() - pathStartedAt > RobotConfig.PATH_TIMEOUT_SECONDS) {
                hardwareError = "Timed out at " + (activeStep == null ? "unknown waypoint" : activeStep.name);
                stopPath();
            }
        } catch (RuntimeException exception) {
            hardwareError = errorText(exception);
            stopPath();
        }

        telemetry.addData("CURRENT STEP", activeStep == null ? "starting" : activeStep.name);
        telemetry.addData("STATUS", followingPath ? "driving" : "stopped between steps");
        telemetry.addData("ACTUAL ROBOT CENTER", formatPose(follower.pose()));
        telemetry.update();
    }

    private void startNextStep() {
        if (nextStep >= route.size()) {
            finished = true;
            followingPath = false;
            drive.stopDrive();
            return;
        }

        activeStep = route.get(nextStep++);
        Pose pathStart = follower.pose();
        follower.follow(line(pathStart, activeStep.target).linear(pathStart, activeStep.target));
        pathStartedAt = getRuntime();
        followingPath = true;
    }

    private void stopPath() {
        if (follower != null) {
            try {
                follower.stop();
            } catch (RuntimeException ignored) {
                // Direct motor stop below remains the fallback.
            }
        }
        if (drive != null) drive.stopDrive();
        followingPath = false;
        finished = true;
    }

    @Override
    public void stop() {
        stopPath();
    }
}
