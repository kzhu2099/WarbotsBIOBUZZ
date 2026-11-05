package org.firstinspires.ftc.teamcode;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pedropathing.api.Paths.line;

@Autonomous(name = "TEST - Pedro Path (No Mechanisms)", group = "BIOBUZZ Tests")
public class PathTestAuto extends OpMode {
    private enum Start { SIDE_WALL_CENTER, AUDIENCE_CORNER, SCORING_CORNER }
    private enum Flower { LEFT_WALL, TOP_WALL, RIGHT_WALL, BOTTOM_WALL }
    private enum Lane { INSIDE, OUTSIDE }
    private enum HiveSide { AUDIENCE, FIELD }

    private static final class PathStep {
        final String name;
        final Pose target;
        final double pauseSeconds;

        PathStep(String name, Pose target, double pauseSeconds) {
            this.name = name;
            this.target = target;
            this.pauseSeconds = pauseSeconds;
        }
    }

    private static final class RoutePoint {
        final Pose pose;
        final String outboundName;
        final String returnName;
        final double pauseSeconds;

        RoutePoint(Pose pose, String outboundName, String returnName, double pauseSeconds) {
            this.pose = pose;
            this.outboundName = outboundName;
            this.returnName = returnName;
            this.pauseSeconds = pauseSeconds;
        }
    }

    private final PoseFactory poses = PoseFactory.radians();
    private final List<PathStep> route = new ArrayList<>();

    private Start start = Start.SIDE_WALL_CENTER;
    private Flower flower = Flower.LEFT_WALL;
    private Lane lane = Lane.INSIDE;
    private HiveSide hiveSide = HiveSide.AUDIENCE;
    private boolean redAlliance = true;

    private RobotHardware drive;
    private Follower follower;

    private Pose startPose;
    private Pose laneEntryPose;
    private Pose laneAtHivePose;
    private Pose hiveCellPose;
    private Pose hiveApproachPose;
    private Pose bypassPose;
    private Pose flowerApproachPose;

    private int nextStep;
    private PathStep activeStep;
    private double pathStartedAt;
    private double pauseEndsAt;
    private boolean followingPath;
    private boolean pausing;
    private boolean finished;
    private String hardwareError;

    @Override
    public void init() {
        drive = new RobotHardware(hardwareMap);

        if (!drive.driveReady()) {
            hardwareError = "Missing drive motors: " + drive.missingDriveMotors();
            return;
        }

        try {
            follower = Constants.create(hardwareMap);
        } catch (RuntimeException exception) {
            hardwareError = errorText(exception);
            drive.stopDrive();
        }
    }

    @Override
    public void init_loop() {
        readSelections();

        telemetry.addData("Alliance (X)", redAlliance ? "RED" : "BLUE");
        telemetry.addData("Start (D-pad L/R)", start);
        telemetry.addData("Flower (D-pad U/D)", flower);
        telemetry.addData("Lane (A)", lane == Lane.INSIDE ? "INSIDE" : "OUTSIDE");
        telemetry.addData("Hive cell (B)",
                hiveSide == HiveSide.FIELD ? "FIELD / SCORING SIDE" : "AUDIENCE SIDE");
        telemetry.addLine("X runs Red on the left; Y runs Audience to Scoring.");
        telemetry.addLine("This OpMode drives only. It does not run the mechanisms.");

        String fieldError = checkFieldConfiguration();
        if (fieldError == null) {
            buildRoute();
            telemetry.addData("Field setup", "ready");
            showPoses();
            showRoute();
        } else {
            telemetry.addData("FIELD SETUP NEEDED", fieldError);
        }

        if (follower == null) {
            telemetry.addData("Pedro", "NOT READY");
            telemetry.addData("Reason", hardwareError);
        } else {
            telemetry.addData("Pedro", "ready");
        }

        telemetry.update();
    }

    private void readSelections() {
        if (gamepad1.xWasPressed()) redAlliance = !redAlliance;
        if (gamepad1.dpadLeftWasPressed()) {
            start = Start.values()[(start.ordinal() + Start.values().length - 1)
                    % Start.values().length];
        }
        if (gamepad1.dpadRightWasPressed()) {
            start = Start.values()[(start.ordinal() + 1) % Start.values().length];
        }
        if (gamepad1.dpadDownWasPressed()) {
            flower = Flower.values()[(flower.ordinal() + Flower.values().length - 1)
                    % Flower.values().length];
        }
        if (gamepad1.dpadUpWasPressed()) {
            flower = Flower.values()[(flower.ordinal() + 1) % Flower.values().length];
        }
        if (gamepad1.aWasPressed()) {
            lane = lane == Lane.INSIDE ? Lane.OUTSIDE : Lane.INSIDE;
        }
        if (gamepad1.bWasPressed()) {
            hiveSide = hiveSide == HiveSide.AUDIENCE ? HiveSide.FIELD : HiveSide.AUDIENCE;
        }
    }

    @Override
    public void start() {
        if (hardwareError != null || follower == null) {
            drive.stopDrive();
            return;
        }

        String fieldError = checkFieldConfiguration();
        if (fieldError != null) {
            hardwareError = fieldError;
            drive.stopDrive();
            return;
        }

        try {
            buildRoute();
            follower.setPose(startPose);
            follower.update();
            startNextStep();
        } catch (RuntimeException exception) {
            hardwareError = errorText(exception);
            stopPath();
        }
    }

    @Override
    public void loop() {
        if (hardwareError != null || follower == null) {
            drive.stopDrive();
            telemetry.addData("PATH TEST STOPPED", hardwareError);
            telemetry.update();
            return;
        }

        if (finished) {
            drive.stopDrive();
            telemetry.addData("PATH TEST", "complete");
            showPoses();
            telemetry.update();
            return;
        }

        double now = getRuntime();

        try {
            follower.update();

            if (pausing) {
                drive.stopDrive();
                if (now >= pauseEndsAt) {
                    pausing = false;
                    startNextStep();
                }
            } else if (followingPath && !follower.isBusy()) {
                followingPath = false;
                follower.stop();
                drive.stopDrive();

                if (activeStep.pauseSeconds > 0) {
                    pausing = true;
                    pauseEndsAt = now + activeStep.pauseSeconds;
                } else {
                    startNextStep();
                }
            } else if (followingPath && now - pathStartedAt > RobotConfig.PATH_TIMEOUT_SECONDS) {
                hardwareError = "Path timed out at: " + activeStep.name;
                stopPath();
            }
        } catch (RuntimeException exception) {
            hardwareError = errorText(exception);
            stopPath();
        }

        telemetry.addData("Path step", activeStep == null ? "starting" : activeStep.name);
        telemetry.addData("Status", pausing ? "pause" : "running");
        telemetry.addData("Robot center", formatPose(follower.pose()));
        showPoses();
        telemetry.update();
    }

    private void buildRoute() {
        double fieldSize = FieldConfig.FIELD_SIZE_INCHES;
        double center = FieldConfig.CENTER_INCHES;
        double halfWidth = FieldConfig.ROBOT_WIDTH_INCHES / 2.0;

        startPose = createStartPose(fieldSize, halfWidth);

        double redHiveCellX = center - FieldConfig.HIVE_CELL_X_OFFSET_INCHES;
        double hiveCellX = redAlliance ? redHiveCellX : fieldSize - redHiveCellX;
        double hiveCellY = center + (hiveSide == HiveSide.FIELD
                ? FieldConfig.HIVE_CELL_SEPARATION_INCHES / 2.0
                : -FieldConfig.HIVE_CELL_SEPARATION_INCHES / 2.0);

        double allianceHeading = redAlliance ? 0.0 : Math.PI;
        double hiveApproachX = redAlliance
                ? hiveCellX - FieldConfig.HIVE_APPROACH_INCHES
                : hiveCellX + FieldConfig.HIVE_APPROACH_INCHES;

        hiveCellPose = poses.of(hiveCellX, hiveCellY, allianceHeading);
        hiveApproachPose = poses.of(hiveApproachX, hiveCellY, allianceHeading);

        double redLaneX = lane == Lane.INSIDE
                ? FieldConfig.INSIDE_LANE_X_INCHES : FieldConfig.OUTSIDE_LANE_X_INCHES;
        double laneX = redAlliance ? redLaneX : fieldSize - redLaneX;

        laneEntryPose = poses.of(laneX, startPose.y(), allianceHeading);
        laneAtHivePose = poses.of(laneX, hiveCellY, allianceHeading);
        flowerApproachPose = createFlowerPose(fieldSize);

        route.clear();
        bypassPose = null;

        addStep("Move from start to the selected lane", laneEntryPose, 0);
        addStep("Follow the " + lane.name().toLowerCase(Locale.US) + " lane to the Hive row",
                laneAtHivePose, 0);
        addStep("Approach the " + hiveSide.name().toLowerCase(Locale.US) + " Hive cell",
                hiveApproachPose, RobotConfig.PATH_PAUSE_SECONDS);

        List<RoutePoint> flowerRoute = createFlowerRoute(
                fieldSize, center, laneX, allianceHeading, halfWidth);

        addStep("Back out of the Hive onto the lane", laneAtHivePose, 0);

        for (int i = 1; i < flowerRoute.size(); i++) {
            RoutePoint point = flowerRoute.get(i);
            addStep(point.outboundName, point.pose, point.pauseSeconds);
        }

        for (int i = flowerRoute.size() - 2; i >= 0; i--) {
            RoutePoint point = flowerRoute.get(i);
            addStep(point.returnName, point.pose, 0);
        }

        addStep("Approach the Hive again", hiveApproachPose, RobotConfig.PATH_PAUSE_SECONDS);
    }

    private List<RoutePoint> createFlowerRoute(
            double fieldSize,
            double center,
            double laneX,
            double allianceHeading,
            double halfWidth) {
        List<RoutePoint> points = new ArrayList<>();
        points.add(new RoutePoint(laneAtHivePose, "", "Follow the original lane back to the Hive row", 0));

        if (needsHiveBypass()) {
            double bypassDistance = FieldConfig.HIVE_FRAME_DEPTH_Y_INCHES / 2.0
                    + halfWidth + FieldConfig.HIVE_CLEARANCE_INCHES;
            boolean useFieldSide = hiveSide == HiveSide.FIELD;
            double bypassY = useFieldSide
                    ? center + bypassDistance : center - bypassDistance;
            String bypassSide = useFieldSide ? "scoring-side" : "audience-side";

            bypassPose = poses.of(laneX, bypassY, allianceHeading);
            points.add(new RoutePoint(
                    bypassPose,
                    "Move to the " + bypassSide + " Hive bypass",
                    "Cross behind the Hive back to the original lane",
                    0
            ));

            double farLaneX = fieldSize - laneX;
            Pose farSideOfBypass = poses.of(farLaneX, bypassY, allianceHeading);
            Pose farLaneAtFlower = poses.of(farLaneX, flowerApproachPose.y(), allianceHeading);

            points.add(new RoutePoint(
                    farSideOfBypass,
                    "Cross behind the Hive to the far-side lane",
                    "Follow the far-side lane back to the bypass",
                    0
            ));
            points.add(new RoutePoint(
                    farLaneAtFlower,
                    "Follow the far-side lane to the Flower row",
                    "Leave the Flower and rejoin the far-side lane",
                    0
            ));
        } else {
            Pose laneAtFlower = poses.of(laneX, flowerApproachPose.y(), allianceHeading);
            points.add(new RoutePoint(
                    laneAtFlower,
                    "Follow the selected lane to the Flower row",
                    "Leave the Flower and rejoin the selected lane",
                    0
            ));
        }

        points.add(new RoutePoint(
                flowerApproachPose,
                "Approach the " + flower.name().toLowerCase(Locale.US) + " Flower",
                "",
                RobotConfig.PATH_PAUSE_SECONDS
        ));

        return points;
    }

    private boolean needsHiveBypass() {
        return (flower == Flower.LEFT_WALL && !redAlliance)
                || (flower == Flower.RIGHT_WALL && redAlliance);
    }

    private Pose createStartPose(double fieldSize, double halfWidth) {
        double halfLength = FieldConfig.ROBOT_LENGTH_INCHES / 2.0;
        double wallClearance = FieldConfig.START_WALL_CLEARANCE_INCHES;

        double x = redAlliance
                ? halfLength + wallClearance
                : fieldSize - halfLength - wallClearance;

        double y = FieldConfig.CENTER_INCHES;
        if (start == Start.AUDIENCE_CORNER) {
            y = halfWidth + wallClearance;
        } else if (start == Start.SCORING_CORNER) {
            y = fieldSize - halfWidth - wallClearance;
        }

        return poses.of(x, y, redAlliance ? 0.0 : Math.PI);
    }

    private Pose createFlowerPose(double fieldSize) {
        double standoff = FieldConfig.FLOWER_STANDOFF_INCHES;
        double alongWall = FieldConfig.FLOWER_POSITION_INCHES;

        switch (flower) {
            case LEFT_WALL:
                return poses.of(standoff, alongWall, Math.PI);
            case TOP_WALL:
                return poses.of(alongWall, fieldSize - standoff, Math.PI / 2.0);
            case RIGHT_WALL:
                return poses.of(fieldSize - standoff, alongWall, 0);
            case BOTTOM_WALL:
                return poses.of(alongWall, standoff, -Math.PI / 2.0);
            default:
                throw new IllegalStateException("Unknown Flower");
        }
    }

    private String checkFieldConfiguration() {
        List<String> missing = new ArrayList<>();

        requirePositive(missing, "robot width including bumpers", FieldConfig.ROBOT_WIDTH_INCHES);
        requirePositive(missing, "robot length including bumpers", FieldConfig.ROBOT_LENGTH_INCHES);
        requirePositive(missing, "start wall clearance", FieldConfig.START_WALL_CLEARANCE_INCHES);
        requirePositive(missing, "Hive cell X offset", FieldConfig.HIVE_CELL_X_OFFSET_INCHES);
        requirePositive(missing, "Hive approach distance", FieldConfig.HIVE_APPROACH_INCHES);
        requirePositive(missing, "selected lane X coordinate",
                lane == Lane.INSIDE ? FieldConfig.INSIDE_LANE_X_INCHES
                        : FieldConfig.OUTSIDE_LANE_X_INCHES);
        requirePositive(missing, "Flower standoff", FieldConfig.FLOWER_STANDOFF_INCHES);

        if (!missing.isEmpty()) {
            return "Set these in FieldConfig.java: " + String.join(", ", missing);
        }

        double fieldSize = FieldConfig.FIELD_SIZE_INCHES;
        double center = FieldConfig.CENTER_INCHES;
        double halfWidth = FieldConfig.ROBOT_WIDTH_INCHES / 2.0;
        double halfLength = FieldConfig.ROBOT_LENGTH_INCHES / 2.0;

        double frameLeft = center - FieldConfig.HIVE_FRAME_WIDTH_X_INCHES / 2.0;
        double frameRight = center + FieldConfig.HIVE_FRAME_WIDTH_X_INCHES / 2.0;

        double redLaneX = lane == Lane.INSIDE
                ? FieldConfig.INSIDE_LANE_X_INCHES : FieldConfig.OUTSIDE_LANE_X_INCHES;
        double laneX = redAlliance ? redLaneX : fieldSize - redLaneX;
        double redHiveCellX = center - FieldConfig.HIVE_CELL_X_OFFSET_INCHES;
        double hiveCellX = redAlliance ? redHiveCellX : fieldSize - redHiveCellX;
        double hiveApproachX = redAlliance
                ? hiveCellX - FieldConfig.HIVE_APPROACH_INCHES
                : hiveCellX + FieldConfig.HIVE_APPROACH_INCHES;

        if (redLaneX <= halfLength || redLaneX + halfLength
                >= frameLeft - FieldConfig.HIVE_CLEARANCE_INCHES) {
            return "The selected lane is not clear of the wall and Hive frame.";
        }
        if (redHiveCellX <= frameLeft || redHiveCellX >= center) {
            return "Hive cell X offset does not place the red cell inside the red half of the frame.";
        }
        if (redAlliance && (hiveApproachX <= laneX
                || hiveApproachX + halfLength >= frameLeft - FieldConfig.HIVE_CLEARANCE_INCHES)) {
            return "Hive approach is not between the selected lane and the Hive frame.";
        }
        if (!redAlliance && (hiveApproachX >= laneX
                || hiveApproachX - halfLength <= frameRight + FieldConfig.HIVE_CLEARANCE_INCHES)) {
            return "Hive approach is not between the selected lane and the Hive frame.";
        }

        Pose selectedStart = createStartPose(fieldSize, halfWidth);
        if (!robotFitsInsideField(selectedStart, halfLength, halfWidth)) {
            return "Selected start pose puts part of the robot outside the field.";
        }

        Pose flowerPose = createFlowerPose(fieldSize);
        if (!robotFitsInsideField(flowerPose, halfLength, halfWidth)) {
            return "Flower approach puts part of the robot outside the field.";
        }

        if (flower == Flower.TOP_WALL || flower == Flower.BOTTOM_WALL) {
            double frameClearY = FieldConfig.HIVE_FRAME_DEPTH_Y_INCHES / 2.0
                    + halfWidth + FieldConfig.HIVE_CLEARANCE_INCHES;
            if (flower == Flower.TOP_WALL && flowerPose.y() <= center + frameClearY) {
                return "Top Flower approach is not clear of the Hive frame.";
            }
            if (flower == Flower.BOTTOM_WALL && flowerPose.y() >= center - frameClearY) {
                return "Bottom Flower approach is not clear of the Hive frame.";
            }
        }

        if (flower == Flower.LEFT_WALL
                && flowerPose.x() + halfLength
                >= frameLeft - FieldConfig.HIVE_CLEARANCE_INCHES) {
            return "Left Flower approach is too close to the Hive frame.";
        }
        if (flower == Flower.RIGHT_WALL
                && flowerPose.x() - halfLength
                <= frameRight + FieldConfig.HIVE_CLEARANCE_INCHES) {
            return "Right Flower approach is too close to the Hive frame.";
        }

        if (needsHiveBypass()) {
            double bypassDistance = FieldConfig.HIVE_FRAME_DEPTH_Y_INCHES / 2.0
                    + halfWidth + FieldConfig.HIVE_CLEARANCE_INCHES;
            if ((hiveSide == HiveSide.FIELD && center + bypassDistance >= fieldSize - halfWidth)
                    || (hiveSide == HiveSide.AUDIENCE && center - bypassDistance <= halfWidth)) {
                return "Robot is too wide to clear the Hive frame on the selected bypass side.";
            }
        }

        return null;
    }

    private static boolean robotFitsInsideField(Pose pose, double halfLength, double halfWidth) {
        double heading = pose.heading();
        double xExtent = Math.abs(Math.cos(heading)) * halfLength
                + Math.abs(Math.sin(heading)) * halfWidth;
        double yExtent = Math.abs(Math.sin(heading)) * halfLength
                + Math.abs(Math.cos(heading)) * halfWidth;

        return pose.x() >= xExtent && pose.x() <= FieldConfig.FIELD_SIZE_INCHES - xExtent
                && pose.y() >= yExtent && pose.y() <= FieldConfig.FIELD_SIZE_INCHES - yExtent;
    }

    private static void requirePositive(List<String> missing, String label, double value) {
        if (!Double.isFinite(value) || value <= 0) missing.add(label);
    }

    private void addStep(String name, Pose target, double pauseSeconds) {
        route.add(new PathStep(name, target, pauseSeconds));
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

    private void showPoses() {
        telemetry.addData("Start robot center", formatPose(startPose));
        telemetry.addData("Lane entry", formatPose(laneEntryPose));
        telemetry.addData("Lane at Hive row", formatPose(laneAtHivePose));
        telemetry.addData("Hive cell center", formatPose(hiveCellPose));
        telemetry.addData("Hive approach", formatPose(hiveApproachPose));
        telemetry.addData("Hive bypass", formatPose(bypassPose));
        telemetry.addData("Flower approach", formatPose(flowerApproachPose));
    }

    private void showRoute() {
        for (int i = 0; i < route.size(); i++) {
            PathStep step = route.get(i);
            telemetry.addData("Step " + (i + 1), step.name + " -> " + formatPose(step.target));
        }
    }

    private static String formatPose(Pose pose) {
        if (pose == null) return "not used";
        return String.format(Locale.US, "(%.1f in, %.1f in, %.0f deg)",
                pose.x(), pose.y(), Math.toDegrees(pose.heading()));
    }

    private void stopPath() {
        if (follower != null) {
            try {
                follower.stop();
            } catch (RuntimeException ignored) {
                // Direct motor stop remains the fallback.
            }
        }
        if (drive != null) drive.stopDrive();
        followingPath = false;
        pausing = false;
        finished = true;
    }

    private static String errorText(RuntimeException exception) {
        return exception.getMessage() == null
                ? exception.getClass().getSimpleName() : exception.getMessage();
    }

    @Override
    public void stop() {
        stopPath();
    }
}
