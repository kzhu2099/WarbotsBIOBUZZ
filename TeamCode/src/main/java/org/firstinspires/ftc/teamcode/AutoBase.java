package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.Locale;

import static com.pedropathing.api.Paths.line;

/**
 * Shared auto runner. Each auto runs its PLAN, an ordered list of jobs (shoot the FRONT cell,
 * shoot the BACK cell, refill at a FLOWER), using Red spots; they are rotated for Blue here.
 *
 * - A step is skipped if an earlier drive timed out or the clock is past GO_PARK_AFTER_SECONDS.
 * - park() always runs last.
 *
 * INIT (gamepad 1): X = alliance, D-pad up/down = start delay.
 */
public abstract class AutoBase extends LinearOpMode {

    /** Handed to TeleOp 2/3 so field-centric drive starts correct. */
    public static Pose lastPose = null;
    public static boolean lastWasRed = true;

    protected Follower follower;
    protected Mechanisms mechanisms;
    protected boolean isRed = true;

    private final ElapsedTime matchClock = new ElapsedTime();
    private double startDelay = 0;
    private boolean failed = false;
    private boolean parking = false;
    private String step = "INIT";
    private String problem = "";

    protected abstract Pose startSpot();

    protected abstract Pose parkSpot();

    /** The jobs an auto can put in its PLAN. Edit the PLAN list at the top of each auto. */
    public enum Job {
        FRONT,   // shoot the audience-side cell (Red's starts up)
        BACK,    // shoot the scoring-side cell (Blue's starts up)
        FLOWER   // drive to our flower and refill (the robot holds 4)
    }

    /** The ordered plan, e.g. { FRONT, FLOWER, BACK }. */
    protected abstract Job[] plan();

    /** How this auto does one job. */
    protected abstract void run(Job job);

    /** Last moves before park() that must not be skipped (e.g. get to the right side of the hive). */
    protected void beforePark() {}

    /** Extra INIT choices (practice auto). */
    protected void readExtraInitButtons() {}

    protected String describe() {
        return getClass().getSimpleName();
    }

    @Override
    public void runOpMode() {
        follower = Constants.create(hardwareMap);
        mechanisms = new Mechanisms(hardwareMap);

        while (opModeInInit()) {
            if (gamepad1.xWasPressed()) isRed = !isRed;
            if (gamepad1.dpadUpWasPressed()) startDelay += 0.5;
            if (gamepad1.dpadDownWasPressed()) startDelay = Math.max(0, startDelay - 0.5);
            readExtraInitButtons();

            telemetry.addData("Alliance (X)", isRed ? "RED" : "BLUE");
            telemetry.addData("Auto", describe());
            telemetry.addData("Plan", java.util.Arrays.toString(plan()));
            telemetry.addData("Start at", formatPose(field(startSpot())));
            telemetry.addData("Start delay (dpad)", "%.1f s", startDelay);
            telemetry.addData("Missing", show(mechanisms.missing()));
            telemetry.update();
        }
        if (isStopRequested()) return;

        matchClock.reset();
        follower.setPose(field(startSpot()));
        try {
            waitSeconds("START DELAY", startDelay);
            for (Job job : plan()) run(job);
            park();
        } finally {
            mechanisms.stopAll();
            follower.stop();
            lastPose = follower.pose();
            lastWasRed = isRed;
        }
        while (opModeIsActive()) report();
    }

    // ---------- steps ----------

    /** Spin up on the way, drive to the shot spot, fire. */
    protected void shootFrom(Spots.Shot shot) {
        if (shouldSkip()) return;
        mechanisms.aimFor(shot);
        driveTo(shot.name, shot.pose);
        fire(shot);
    }

    /** Line up, drive in with the intake running, wait, back out. */
    protected void collectFrom(String name, Pose lineup, Pose atFlower) {
        if (shouldSkip()) return;
        driveTo(name + " LINEUP", lineup);
        mechanisms.setIntake(1);
        driveTo(name, atFlower);
        waitSeconds("INTAKE " + name, Settings.FLOWER_INTAKE_SECONDS);
        driveTo(name + " LINEUP", lineup);
        mechanisms.setIntake(0);
    }

    /** Straight line to a Red spot (rotated for Blue), turning the short way on the way. */
    protected void driveTo(String name, Pose redSpot) {
        if (shouldSkip()) return;
        step = name;

        Pose target = field(redSpot);
        Pose from = follower.pose();
        Pose to = new Pose(target.x(), target.y(),
                from.heading() + angleBetween(from.heading(), target.heading()));
        boolean turnInPlace = distance(from, to) < 1.0;
        if (turnInPlace) {
            follower.algorithm().reset();
            follower.hold(to);
        } else {
            follower.follow(straightLine(from, to));
        }

        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive()) {
            follower.update();
            lastPose = follower.pose();
            if (isCloseTo(to) || (!turnInPlace && !follower.isBusy())) return;
            if (timer.seconds() > Settings.DRIVE_TIMEOUT_SECONDS) {
                failed = true;
                problem = "TIMEOUT " + name + " at " + formatPose(follower.pose());
                follower.stop();
                return;
            }
            telemetry.addData("Target", formatPose(to));
            report();
        }
    }

    protected void waitSeconds(String name, double seconds) {
        if (shouldSkip() || seconds <= 0) return;
        step = name;
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && timer.seconds() < seconds) {
            follower.update();
            report();
        }
    }

    private void fire(Spots.Shot shot) {
        if (shouldSkip()) return;
        if (!mechanisms.hasShooter()) {
            waitSeconds("NO OUTTAKE - SKIP SHOT", 0.5);
            return;
        }
        step = "SPIN UP " + shot.name;
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && !mechanisms.isReady() && timer.seconds() < Settings.SPIN_UP_MAX_SECONDS) {
            follower.update();
            report();
        }
        // Feed even if not at speed after the timeout; telemetry shows which happened.
        step = "FEED " + shot.name + (mechanisms.isReady() ? "" : " (NOT AT SPEED)");
        mechanisms.setTransfer(1);
        mechanisms.setIntake(1);
        timer.reset();
        while (opModeIsActive() && timer.seconds() < Settings.FEED_SECONDS) {
            follower.update();
            report();
        }
        mechanisms.stopAll();
    }

    private void park() {
        mechanisms.stopAll();
        parking = true;
        beforePark();
        driveTo("PARK", parkSpot());
        step = "DONE";
    }

    private boolean shouldSkip() {
        if (!opModeIsActive()) return true;
        if (parking) return false;
        return failed || matchClock.seconds() > Settings.GO_PARK_AFTER_SECONDS;
    }

    private void report() {
        telemetry.addData("Step", step);
        telemetry.addData("Pose", formatPose(follower.pose()));
        telemetry.addData("Clock", "%.1f", matchClock.seconds());
        if (mechanisms.hasShooter()) {
            telemetry.addData("Flywheel", "%.0f / %.0f", mechanisms.rpm(), mechanisms.targetRpm());
        }
        if (!problem.isEmpty()) telemetry.addData("PROBLEM", problem);
        telemetry.update();
    }

    // ---------- helpers ----------

    private Pose field(Pose redSpot) {
        return Spots.forAlliance(redSpot, isRed);
    }

    /** True when the robot is on the scoring-wall side of the hive (works for both alliances). */
    protected boolean onBackSide() {
        return Spots.forAlliance(follower.pose(), isRed).y() > Spots.FIELD / 2;
    }

    protected void unknownJob(Job job) {
        problem = job + " is not allowed in " + describe() + " - skipped";
    }

    public static Path straightLine(Pose from, Pose to) {
        return line(from, to).linear(from, to);
    }

    /** Shortest signed turn from one heading to another, radians in [-pi, pi]. */
    public static double angleBetween(double fromHeading, double toHeading) {
        double difference = toHeading - fromHeading;
        return Math.atan2(Math.sin(difference), Math.cos(difference));
    }

    public static double distance(Pose a, Pose b) {
        return Math.hypot(a.x() - b.x(), a.y() - b.y());
    }

    private boolean isCloseTo(Pose target) {
        Pose now = follower.pose();
        return distance(now, target) < Settings.DONE_INCHES
                && Math.abs(Math.toDegrees(angleBetween(now.heading(), target.heading()))) < Settings.DONE_DEGREES;
    }

    public static String formatPose(Pose pose) {
        double degrees = Math.toDegrees(Math.atan2(Math.sin(pose.heading()), Math.cos(pose.heading())));
        return String.format(Locale.US, "(%.1f, %.1f) %.0f deg", pose.x(), pose.y(), degrees);
    }

    static String show(String missing) {
        return missing.isEmpty() ? "none" : missing;
    }
}
