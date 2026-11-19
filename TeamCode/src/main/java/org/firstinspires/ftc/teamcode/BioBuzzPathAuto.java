package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.pedropathing.api.Paths.line;

/**
 * Shared, deliberately small state machine for the two BIOBUZZ route OpModes.
 *
 * The route itself comes only from the literal presets in FieldPoses.java.
 * This class just follows those points in order, waits at the Flower, and
 * either simulates or runs the two outtake motors at the shooting points.
 */
abstract class BioBuzzPathAuto extends OpMode {
    private RobotHardware drive;
    private Follower follower;
    private final List<Pose> route = new ArrayList<>();

    private boolean redAlliance = true;
    private boolean innerLane = true;
    private int startChoice = 0;
    private int flowerChoice = 0;

    private Pose selectedStart;
    private int firstShotIndex = -1;
    private int pickupIndex = -1;
    private int secondShotIndex = -1;

    private int nextWaypoint;
    private int activeWaypoint = -1;
    private double pathStartedAt;
    private double waitStartedAt;
    private double waitDurationSeconds;
    private boolean following;
    private boolean waiting;
    private boolean waitingForShot;
    private boolean finished;
    private String error;

    protected abstract void initializeScoringHardware();
    protected abstract void beginShooting();
    protected abstract void endShooting();
    protected abstract String shootingMode();

    @Override
    public void init() {
        try {
            drive = new RobotHardware(hardwareMap);
            if (!drive.driveReady()) {
                error = "Missing drive motors: " + drive.missingDriveMotors();
            } else {
                initializeScoringHardware();
                follower = Constants.create(hardwareMap);
            }
        } catch (RuntimeException exception) {
            error = errorText(exception);
            stopDrive();
            endShooting();
        }
    }

    @Override
    public void init_loop() {
        readSelections();

        if (error == null) {
            buildRoute();
        }

        telemetry.addLine("BIOBUZZ - SIMPLE TWO-LANE FLOWER CYCLE");
        telemetry.addData("FIELD", "144 x 144 in; 6 x 6 tiles; 24 in per tile");
        telemetry.addData("ALLIANCE", redAlliance ? "RED" : "BLUE");
        telemetry.addData("START", FieldPoses.START_NAMES[startChoice]);
        telemetry.addData("FLOWER", FieldPoses.FLOWER_NAMES[flowerChoice]);
        telemetry.addData("LANE", innerLane ? "INSIDE - straight shot" : "OUTSIDE - angled shot");
        telemetry.addData("SHOOTING", shootingMode());

        telemetry.addLine("CONTROLS: X = alliance | D-pad LEFT/RIGHT = start wall");
        telemetry.addLine("D-pad UP/DOWN = Flower | A = inside/outside lane");
        telemetry.addLine("PATH: shoot -> Flower pickup -> shoot -> park");

        if (error != null) {
            telemetry.addData("NOT READY", error);
        } else {
            telemetry.addData("DRIVE", "ready");
            telemetry.addData("ODOMETRY", "Pedro + Pinpoint initialized");
            telemetry.addData("START CENTER", formatPose(selectedStart));
            telemetry.addLine("----- PRESET WAYPOINTS -----");
            for (int i = 0; i < route.size(); i++) {
                telemetry.addData(String.format(Locale.US, "%02d", i + 1),
                        waypointDescription(i) + " " + formatPose(route.get(i)));
            }
            telemetry.addLine("Set the robot center and heading to the displayed START CENTER.");
        }
        telemetry.update();
    }

    private void readSelections() {
        if (gamepad1.xWasPressed()) redAlliance = !redAlliance;

        if (gamepad1.dpadLeftWasPressed()) {
            startChoice = (startChoice + 2) % 3;
        } else if (gamepad1.dpadRightWasPressed()) {
            startChoice = (startChoice + 1) % 3;
        }

        if (gamepad1.dpadDownWasPressed()) {
            flowerChoice = (flowerChoice + 2) % 3;
        } else if (gamepad1.dpadUpWasPressed()) {
            flowerChoice = (flowerChoice + 1) % 3;
        }

        if (gamepad1.aWasPressed()) innerLane = !innerLane;
    }

    private void buildRoute() {
        route.clear();

        selectedStart = redAlliance
                ? FieldPoses.RED_STARTS[startChoice]
                : FieldPoses.BLUE_STARTS[startChoice];

        Pose[][] startPaths;
        Pose[][] flowerPaths;

        if (redAlliance && innerLane) {
            startPaths = FieldPoses.RED_INNER_START_TO_SHOOT;
            flowerPaths = FieldPoses.RED_INNER_FLOWER_PATHS;
        } else if (redAlliance) {
            startPaths = FieldPoses.RED_OUTER_START_TO_SHOOT;
            flowerPaths = FieldPoses.RED_OUTER_FLOWER_PATHS;
        } else if (innerLane) {
            startPaths = FieldPoses.BLUE_INNER_START_TO_SHOOT;
            flowerPaths = FieldPoses.BLUE_INNER_FLOWER_PATHS;
        } else {
            startPaths = FieldPoses.BLUE_OUTER_START_TO_SHOOT;
            flowerPaths = FieldPoses.BLUE_OUTER_FLOWER_PATHS;
        }

        Pose[] startToShoot = startPaths[startChoice];
        Pose[] toFlower = flowerPaths[flowerChoice];

        for (Pose pose : startToShoot) {
            route.add(pose);
        }
        firstShotIndex = route.size() - 1;

        for (Pose pose : toFlower) {
            route.add(pose);
        }
        pickupIndex = route.size() - 1;

        // Retrace the Flower approach waypoints back to the same shooting pose.
        for (int i = toFlower.length - 2; i >= 0; i--) {
            route.add(toFlower[i]);
        }
        secondShotIndex = route.size();
        route.add(startToShoot[startToShoot.length - 1]);

        route.add(redAlliance ? FieldPoses.RED_PARK : FieldPoses.BLUE_PARK);
    }

    @Override
    public void start() {
        if (error != null || drive == null || follower == null) {
            stopAll();
            telemetry.addData("AUTO NOT STARTED", error == null ? "Path or hardware not ready." : error);
            telemetry.update();
            return;
        }

        try {
            buildRoute();
            if (route.isEmpty()) {
                throw new IllegalStateException("The selected preset route is empty.");
            }

            follower.setPose(selectedStart);
            follower.update();

            nextWaypoint = 0;
            activeWaypoint = -1;
            following = false;
            waiting = false;
            finished = false;
            startNextWaypoint();
        } catch (RuntimeException exception) {
            fail(exception);
        }
    }

    @Override
    public void loop() {
        if (error != null || follower == null || drive == null) {
            stopAll();
            telemetry.addData("AUTO STOPPED", error == null ? "Drive or odometry unavailable." : error);
            telemetry.update();
            return;
        }

        try {
            if (waiting) {
                if (getRuntime() - waitStartedAt >= waitDurationSeconds) {
                    if (waitingForShot) endShooting();
                    waiting = false;
                    waitingForShot = false;
                    startNextWaypoint();
                }
            } else if (following) {
                follower.update();

                if (getRuntime() - pathStartedAt > RobotConfig.PATH_TIMEOUT_SECONDS
                        && follower.isBusy()) {
                    throw new IllegalStateException(
                            "Timed out on waypoint " + (activeWaypoint + 1) + ": "
                                    + waypointDescription(activeWaypoint));
                }

                if (!follower.isBusy()) {
                    follower.stop();
                    drive.stopDrive();
                    following = false;
                    handleWaypointReached();
                }
            }
        } catch (RuntimeException exception) {
            fail(exception);
        }

        telemetry.addData("STATUS", error != null ? "STOPPED" : finished ? "COMPLETE"
                : waiting ? (waitingForShot ? "SHOOTING / DWELL" : "PICKING UP POLLEN")
                : following ? "DRIVING" : "READY");
        if (activeWaypoint >= 0 && activeWaypoint < route.size()) {
            telemetry.addData("STEP", (activeWaypoint + 1) + " / " + route.size());
            telemetry.addData("ACTION", waypointDescription(activeWaypoint));
            telemetry.addData("TARGET", formatPose(route.get(activeWaypoint)));
        }
        if (follower != null) telemetry.addData("ODOMETRY POSE", follower.pose());
        if (error != null) telemetry.addData("ERROR", error);
        telemetry.update();
    }

    private void handleWaypointReached() {
        if (activeWaypoint == firstShotIndex || activeWaypoint == secondShotIndex) {
            beginShooting();
            beginWait(RobotConfig.AUTO_SHOOT_MS, true);
        } else if (activeWaypoint == pickupIndex) {
            // Pollen is retrieved through the robot's passive Flower pickup.
            // Hold at the retrieval opening before retracing the same route.
            beginWait(RobotConfig.AUTO_PICKUP_MS, false);
        } else {
            startNextWaypoint();
        }
    }

    private void beginWait(long milliseconds, boolean isShot) {
        waiting = true;
        waitingForShot = isShot;
        waitStartedAt = getRuntime();
        waitDurationSeconds = milliseconds / 1000.0;
    }

    private void startNextWaypoint() {
        if (nextWaypoint >= route.size()) {
            finished = true;
            following = false;
            drive.stopDrive();
            endShooting();
            return;
        }

        activeWaypoint = nextWaypoint++;
        Pose from = follower.pose();
        Pose target = route.get(activeWaypoint);

        follower.follow(line(from, target).linear(from, target));
        pathStartedAt = getRuntime();
        following = true;
    }

    private String waypointDescription(int index) {
        if (index == firstShotIndex) return "FIRST SHOT";
        if (index == pickupIndex) return "FLOWER PICKUP";
        if (index == secondShotIndex) return "SECOND SHOT";
        if (index == route.size() - 1) return "PARK IN ALLIANCE LOADING ZONE";
        return "DRIVE";
    }

    private void fail(RuntimeException exception) {
        error = errorText(exception);
        stopAll();
    }

    @Override
    public void stop() {
        stopAll();
    }

    private void stopAll() {
        following = false;
        waiting = false;
        try {
            endShooting();
        } catch (RuntimeException ignored) {
            // Continue attempting to stop drivetrain hardware.
        }

        if (follower != null) {
            try {
                follower.stop();
            } catch (RuntimeException ignored) {
                // Direct motor stop is still attempted below.
            }
        }
        stopDrive();
    }

    private void stopDrive() {
        if (drive != null) drive.stopDrive();
    }

    private static String formatPose(Pose pose) {
        return String.format(Locale.US, "(%.1f, %.1f) in / %.0f deg",
                pose.x(), pose.y(), Math.toDegrees(pose.heading()));
    }

    private static String errorText(RuntimeException exception) {
        return exception.getMessage() == null
                ? exception.getClass().getSimpleName() : exception.getMessage();
    }
}
