package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.Locale;

/**
 * Field-centric drive on Pinpoint + Pedro. TeleOp 3 adds mechanisms on gamepad 2.
 * Starts from the pose and alliance the last auto ended with.
 *
 * INIT  gp1: X = alliance, dpad L/R = outside/inside start (only if no auto ran)
 * gp1   left stick = drive (field-centric), right stick X = turn, LB = slow
 *       Y = reset heading (robot facing away from your alliance wall)
 *       hold A = drive to the inside shot on this side of the hive, hold B = outside shot
 * gp2   A / Y / B = load inside-front / inside-back / outside shot (angle + flywheel)
 *       RB = fire (feeds only at speed, rumbles when ready), RT = intake, LT = reverse
 *       dpad up/down = angle, dpad right/left = rpm, X = stop all
 */
@TeleOp(name = "2 Drive - odometry", group = "Match")
public class TeleOp2Odometry extends OpMode {
    private Follower follower;
    private Mechanisms mechanisms;
    private boolean isRed = AutoBase.lastWasRed;
    private boolean fromAuto;
    private boolean insideStart = true;

    private Spots.Shot autoDriveShot = null;
    private String shotName = "none";
    private boolean wasReady = false;

    protected boolean useMechanisms() {
        return false;
    }

    @Override
    public void init() {
        follower = Constants.create(hardwareMap);
        if (useMechanisms()) mechanisms = new Mechanisms(hardwareMap);
        fromAuto = AutoBase.lastPose != null;
        if (fromAuto) follower.setPose(AutoBase.lastPose);
    }

    @Override
    public void init_loop() {
        if (gamepad1.xWasPressed()) isRed = !isRed;
        if (gamepad1.dpadLeftWasPressed()) insideStart = false;
        if (gamepad1.dpadRightWasPressed()) insideStart = true;
        if (!fromAuto) {
            follower.setPose(field(insideStart ? Spots.INSIDE_START : Spots.OUTSIDE_START));
        }
        follower.update();

        telemetry.addData("Alliance (X)", isRed ? "RED" : "BLUE");
        telemetry.addData("Pose from", fromAuto ? "auto" : (insideStart ? "INSIDE" : "OUTSIDE") + " start (dpad L/R)");
        telemetry.addData("Pose", AutoBase.formatPose(follower.pose()));
        if (mechanisms != null) telemetry.addData("Missing", AutoBase.show(mechanisms.missing()));
        telemetry.update();
    }

    @Override
    public void loop() {
        driver();
        if (mechanisms != null) operator();
        follower.update();

        telemetry.addData("Pose", AutoBase.formatPose(follower.pose()));
        telemetry.addData("Drive", autoDriveShot == null ? "manual" : "auto to " + autoDriveShot.name);
        if (mechanisms != null) {
            telemetry.addData("Shot", shotName);
            telemetry.addData("Flywheel", "%.0f / %.0f %s", mechanisms.rpm(), mechanisms.targetRpm(),
                    mechanisms.isReady() ? "READY" : "");
            telemetry.addData("Angle", mechanisms.angle() < 0 ? "not set"
                    : String.format(Locale.US, "%.2f", mechanisms.angle()));
            telemetry.addData("Missing", AutoBase.show(mechanisms.missing()));
        }
        telemetry.update();
    }

    private void driver() {
        if (gamepad1.yWasPressed()) {
            Pose now = follower.pose();
            follower.setPose(new Pose(now.x(), now.y(), isRed ? 0 : Math.PI));
        }

        // Hold A/B: drive a straight line to a shot spot. The inside shot used is the one
        // on the robot's current side of the hive, so the line never crosses the hive.
        Spots.Shot wanted = null;
        if (gamepad1.a) wanted = onFrontSide() ? Spots.INSIDE_FRONT_SHOT : Spots.INSIDE_BACK_SHOT;
        if (gamepad1.b) wanted = Spots.OUTSIDE_FRONT_SHOT;
        if (wanted != autoDriveShot) {
            if (wanted == null) {
                follower.stop();
            } else {
                Pose from = follower.pose();
                Pose target = field(wanted.pose);
                Pose to = new Pose(target.x(), target.y(),
                        from.heading() + AutoBase.angleBetween(from.heading(), target.heading()));
                follower.follow(AutoBase.straightLine(from, to));
            }
            autoDriveShot = wanted;
        }
        if (autoDriveShot != null) return;

        double speed = gamepad1.left_bumper ? Settings.SLOW_MODE_POWER : 1.0;
        // Driver's view: stick up = away from own wall, stick right = driver's right.
        // Red drivers face +X; Blue drivers face -X, so Blue flips both.
        double flip = isRed ? 1 : -1;
        double fieldX = -Find.stick(gamepad1.left_stick_y) * speed * flip;
        double fieldY = -Find.stick(gamepad1.left_stick_x) * speed * flip;
        double turnLeft = -Find.stick(gamepad1.right_stick_x) * speed;

        double heading = follower.pose().heading();
        double forward = fieldX * Math.cos(heading) + fieldY * Math.sin(heading);
        double left = -fieldX * Math.sin(heading) + fieldY * Math.cos(heading);
        follower.manual(forward, left, turnLeft); // Pedro: +forward, +left, +counterclockwise
    }

    private void operator() {
        if (gamepad2.aWasPressed()) load(Spots.INSIDE_FRONT_SHOT);
        if (gamepad2.yWasPressed()) load(Spots.INSIDE_BACK_SHOT);
        if (gamepad2.bWasPressed()) load(Spots.OUTSIDE_FRONT_SHOT);
        if (gamepad2.xWasPressed()) {
            mechanisms.stopAll();
            shotName = "none";
        }

        if (gamepad2.dpadUpWasPressed()) mechanisms.nudgeAngle(Settings.ANGLE_STEP);
        if (gamepad2.dpadDownWasPressed()) mechanisms.nudgeAngle(-Settings.ANGLE_STEP);
        if (gamepad2.dpadRightWasPressed()) mechanisms.spinUp(mechanisms.targetRpm() + Settings.RPM_STEP);
        if (gamepad2.dpadLeftWasPressed()) mechanisms.spinUp(Math.max(0, mechanisms.targetRpm() - Settings.RPM_STEP));

        if (gamepad2.left_trigger > 0.3) {
            mechanisms.setIntake(-1);
            mechanisms.setTransfer(-1);
        } else if (gamepad2.right_bumper) {
            mechanisms.feedIfReady();
        } else {
            mechanisms.setIntake(gamepad2.right_trigger > 0.3 ? 1 : 0);
            mechanisms.setTransfer(0);
        }

        boolean ready = mechanisms.isReady();
        if (ready && !wasReady) gamepad2.rumble(300);
        wasReady = ready;
    }

    private void load(Spots.Shot shot) {
        mechanisms.aimFor(shot);
        shotName = shot.name;
    }

    /** True when the robot is on the INSIDE_FRONT_SHOT side of the hive (judged in Red coordinates). */
    private boolean onFrontSide() {
        double redY = isRed ? follower.pose().y() : Spots.FIELD - follower.pose().y();
        return redY < Spots.FIELD / 2;
    }

    private Pose field(Pose redSpot) {
        return Spots.forAlliance(redSpot, isRed);
    }

    @Override
    public void stop() {
        follower.stop();
        if (mechanisms != null) mechanisms.stopAll();
    }
}
