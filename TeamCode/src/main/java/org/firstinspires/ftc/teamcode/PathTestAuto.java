package org.firstinspires.ftc.teamcode;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import static com.pedropathing.api.Paths.line;

@Autonomous(name = "Auto - Path Test (No Mechanisms)", group = "BIOBUZZ Tests")
public class PathTestAuto extends OpMode {
    private enum Start { SIDE_WALL, NEAR_CORNER, FAR_CORNER }
    private enum Flower { LEFT_WALL, TOP_WALL, RIGHT_WALL, BOTTOM_WALL }
    private enum Lane { INSIDE_HIVE, OUTSIDE_HIVE }
    private enum HiveSide { AUDIENCE, OPPOSITE_AUDIENCE }

    // INIT choices.
    private Start start = Start.SIDE_WALL;
    private Flower flower = Flower.LEFT_WALL;
    private Lane lane = Lane.OUTSIDE_HIVE;
    private HiveSide hiveSide = HiveSide.AUDIENCE;
    private boolean redAlliance = true;

    private RobotHardware drive;
    private Follower follower;
    private final PoseFactory poses = PoseFactory.radians();
    private Pose startPose, lanePose, hivePose, flowerPose;
    private Pose[] route;
    private String[] routeNames;
    private int nextPath, activePath;
    private double pathStartedAt, pauseEndsAt;
    private boolean pathRunning, pausing, done;
    private String error;

    @Override
    public void init() {
        drive = new RobotHardware(hardwareMap);
        if (!drive.driveReady()) {
            error = "Missing drive motors: " + drive.missingDriveMotors();
        } else {
            try {
                follower = Constants.create(hardwareMap);
            } catch (RuntimeException e) {
                error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                drive.stopDrive();
            }
        }
    }

    @Override
    public void init_loop() {
        if (gamepad1.xWasPressed()) redAlliance = !redAlliance;
        if (gamepad1.dpadLeftWasPressed()) start = Start.values()[(start.ordinal() + 2) % 3];
        if (gamepad1.dpadRightWasPressed()) start = Start.values()[(start.ordinal() + 1) % 3];
        if (gamepad1.dpadDownWasPressed()) flower = Flower.values()[(flower.ordinal() + 3) % 4];
        if (gamepad1.dpadUpWasPressed()) flower = Flower.values()[(flower.ordinal() + 1) % 4];
        if (gamepad1.aWasPressed()) lane = lane == Lane.INSIDE_HIVE ? Lane.OUTSIDE_HIVE : Lane.INSIDE_HIVE;
        if (gamepad1.bWasPressed()) hiveSide = hiveSide == HiveSide.AUDIENCE
                ? HiveSide.OPPOSITE_AUDIENCE : HiveSide.AUDIENCE;

        telemetry.addData("Alliance (X)", redAlliance ? "RED" : "BLUE");
        telemetry.addData("Start (D-pad L/R)", start);
        telemetry.addData("Flower (D-pad U/D)", flower);
        telemetry.addData("Lane (A)", lane);
        telemetry.addData("Hive side (B)", hiveSide);
        telemetry.addData("Foresight", Constants.foresightConfig == null ? "NOT SET" : "set");
        telemetry.addLine("Route pauses at the Hive and Flower instead of running mechanisms.");
        if (error != null) telemetry.addData("NOT READY", error);
        telemetry.update();
    }

    @Override
    public void start() {
        if (error != null || follower == null) return;

        if (!Double.isFinite(RobotConfig.ROBOT_WIDTH_INCHES) || RobotConfig.ROBOT_WIDTH_INCHES <= 0
                || !Double.isFinite(RobotConfig.ROBOT_LENGTH_INCHES) || RobotConfig.ROBOT_LENGTH_INCHES <= 0
                || !Double.isFinite(RobotConfig.INSIDE_LANE_X) || !Double.isFinite(RobotConfig.INSIDE_LANE_Y)
                || !Double.isFinite(RobotConfig.OUTSIDE_LANE_X) || !Double.isFinite(RobotConfig.OUTSIDE_LANE_Y)
                || !Double.isFinite(RobotConfig.FLOWER_STANDOFF_INCHES)
                || RobotConfig.FLOWER_STANDOFF_INCHES <= 0) {
            error = "Set robot dimensions, both lane points, and Flower standoff in RobotConfig first.";
            drive.stopDrive();
            return;
        }

        double field = RobotConfig.FIELD_SIZE_INCHES;
        double halfWidth = RobotConfig.ROBOT_WIDTH_INCHES / 2.0;
        double halfLength = RobotConfig.ROBOT_LENGTH_INCHES / 2.0;
        double inset = RobotConfig.CORNER_INSET_INCHES;

        // Explicit chassis-center starting poses. These are the team's three presets.
        if (start == Start.SIDE_WALL && redAlliance) {
            startPose = poses.of(halfLength, field / 2.0, 0.0);
        } else if (start == Start.SIDE_WALL) {
            startPose = poses.of(field - halfLength, field / 2.0, Math.PI);
        } else if (start == Start.NEAR_CORNER && redAlliance) {
            startPose = poses.of(halfWidth + inset, field - halfLength, -Math.PI / 2.0);
        } else if (start == Start.NEAR_CORNER) {
            startPose = poses.of(field - halfWidth - inset, halfLength, Math.PI / 2.0);
        } else if (start == Start.FAR_CORNER && redAlliance) {
            startPose = poses.of(halfWidth + inset, halfLength, Math.PI / 2.0);
        } else {
            startPose = poses.of(field - halfWidth - inset, field - halfLength, -Math.PI / 2.0);
        }

        // Hive/Cell coordinates below are inherited estimates; verify them against field CAD.
        double center = field / 2.0;
        double audienceY = center - RobotConfig.CELL_SEPARATION_INCHES / 2.0;
        double oppositeY = center + RobotConfig.CELL_SEPARATION_INCHES / 2.0;
        double hiveX = redAlliance ? center - RobotConfig.HIVE_X_OFFSET_INCHES
                                   : center + RobotConfig.HIVE_X_OFFSET_INCHES;
        double hiveApproachX = redAlliance ? hiveX - RobotConfig.HIVE_APPROACH_INCHES
                                           : hiveX + RobotConfig.HIVE_APPROACH_INCHES;
        double hiveHeading = redAlliance ? 0.0 : Math.PI;
        hivePose = poses.of(hiveApproachX,
                hiveSide == HiveSide.AUDIENCE ? audienceY : oppositeY, hiveHeading);

        // Flower centers are estimates. The approach distance must be measured.
        double standoff = RobotConfig.FLOWER_STANDOFF_INCHES;
        if (flower == Flower.LEFT_WALL) {
            flowerPose = poses.of(standoff, RobotConfig.FLOWER_LEFT_Y_INCHES, Math.PI);
        } else if (flower == Flower.TOP_WALL) {
            flowerPose = poses.of(RobotConfig.FLOWER_TOP_X_INCHES, field - standoff, Math.PI / 2.0);
        } else if (flower == Flower.RIGHT_WALL) {
            flowerPose = poses.of(field - standoff, RobotConfig.FLOWER_RIGHT_Y_INCHES, 0.0);
        } else {
            flowerPose = poses.of(RobotConfig.FLOWER_BOTTOM_X_INCHES, standoff, -Math.PI / 2.0);
        }

        // Lane points are set for Red in RobotConfig; Blue reflects across the field centerline.
        if (lane == Lane.INSIDE_HIVE && redAlliance) {
            lanePose = poses.of(RobotConfig.INSIDE_LANE_X, RobotConfig.INSIDE_LANE_Y, hiveHeading);
        } else if (lane == Lane.INSIDE_HIVE) {
            lanePose = poses.of(field - RobotConfig.INSIDE_LANE_X, RobotConfig.INSIDE_LANE_Y, hiveHeading);
        } else if (redAlliance) {
            lanePose = poses.of(RobotConfig.OUTSIDE_LANE_X, RobotConfig.OUTSIDE_LANE_Y, hiveHeading);
        } else {
            lanePose = poses.of(field - RobotConfig.OUTSIDE_LANE_X, RobotConfig.OUTSIDE_LANE_Y, hiveHeading);
        }

        // Pause at Hive = no preload/final shot. Pause at Flower = no intake.
        route = new Pose[]{lanePose, hivePose, flowerPose, lanePose, hivePose};
        routeNames = new String[]{"Lane", "Hive - preload pause", "Flower - intake pause",
                                  "Lane - return", "Hive - final pause"};

        try {
            follower.setPose(startPose);
            nextPath = 0;
            startNextPath();
        } catch (RuntimeException e) {
            error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            stopPath();
        }
    }

    @Override
    public void loop() {
        if (error != null || follower == null) {
            drive.stopDrive();
            telemetry.addData("PATH TEST STOPPED", error);
            telemetry.update();
            return;
        }
        if (done) {
            drive.stopDrive();
            telemetry.addData("PATH TEST", "complete - no mechanisms used");
            telemetry.addData("Final pose", follower.pose());
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
                    startNextPath();
                }
            } else if (pathRunning && !follower.isBusy()) {
                pathRunning = false;
                follower.stop();
                drive.stopDrive();
                if (activePath == 1 || activePath == 2 || activePath == 4) {
                    pausing = true;
                    pauseEndsAt = now + RobotConfig.PATH_PAUSE_SECONDS;
                } else {
                    startNextPath();
                }
            } else if (pathRunning && now - pathStartedAt > RobotConfig.PATH_TIMEOUT_SECONDS) {
                error = "Path timed out: " + routeNames[activePath];
                stopPath();
            }
        } catch (RuntimeException e) {
            error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            stopPath();
        }

        telemetry.addData("Path", done ? "DONE" : (pausing ? "PAUSE" : routeNames[activePath]));
        telemetry.addData("Pose", follower.pose());
        telemetry.addData("Start pose", startPose);
        telemetry.addData("Lane point", lanePose);
        telemetry.addData("Hive approach", hivePose);
        telemetry.addData("Flower approach", flowerPose);
        telemetry.addData("Next", nextPath + " / " + route.length);
        telemetry.addData("Status", error == null ? "running" : error);
        telemetry.update();
    }

    private void startNextPath() {
        if (nextPath == route.length) {
            done = true;
            pathRunning = false;
            drive.stopDrive();
            return;
        }
        activePath = nextPath++;
        follower.follow(line(follower.pose(), route[activePath]).constant(route[activePath].heading()));
        pathStartedAt = getRuntime();
        pathRunning = true;
    }

    private void stopPath() {
        if (follower != null) {
            try {
                follower.stop();
            } catch (RuntimeException ignored) {
                // Direct motor stop below is the safety fallback.
            }
        }
        if (drive != null) drive.stopDrive();
        pathRunning = false;
        pausing = false;
        done = true;
    }

    @Override
    public void stop() {
        stopPath();
    }
}
