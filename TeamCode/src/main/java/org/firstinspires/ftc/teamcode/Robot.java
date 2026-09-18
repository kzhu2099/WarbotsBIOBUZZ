package org.firstinspires.ftc.teamcode;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;

public class Robot {

    public final Follower follower;
    public final GameVision vision;

    private final Telemetry telemetry;
    private final Gamepad gamepad1;
    private final Gamepad gamepad2;

    private final DcMotor intakeMotor;
    private final DcMotor launchMotor;

    public boolean redAlliance;
    private boolean fieldCentric = true;
    private boolean endgameAlerted = false;

    private String[] startPoseOptions = new String[0];
    private int startPoseIndex = 0;

    public Robot(HardwareMap hardwareMap, Telemetry telemetry, Gamepad gamepad1, Gamepad gamepad2, boolean redAlliance) {
        this.telemetry = telemetry;
        this.gamepad1 = gamepad1;
        this.gamepad2 = gamepad2;
        this.redAlliance = redAlliance;

        follower = Constants.create(hardwareMap);
        vision = new GameVision(hardwareMap, redAlliance);

        intakeMotor = getMotor(hardwareMap, "intake");
        launchMotor = getMotor(hardwareMap, "launch");

        telemetry.addData("Status", "Initialized");
        telemetry.update();
    }

    private static DcMotor getMotor(HardwareMap hardwareMap, String name) {
        try {
            return hardwareMap.get(DcMotor.class, name);
        } catch (Exception e) {
            return null;
        }
    }

    public void toggleAlliance() {
        if (gamepad1.xWasPressed()) {
            redAlliance = !redAlliance;
        }
    }

    public void setStartPoseOptions(String... names) {
        startPoseOptions = names;
        startPoseIndex = 0;
    }

    public void cycleStartPose() {
        if (startPoseOptions.length == 0) return;

        if (gamepad1.dpadRightWasPressed()) {
            startPoseIndex = (startPoseIndex + 1) % startPoseOptions.length;
        } else if (gamepad1.dpadLeftWasPressed()) {
            startPoseIndex = (startPoseIndex - 1 + startPoseOptions.length) % startPoseOptions.length;
        }
    }

    public String selectedStartPose() {
        return startPoseOptions.length == 0 ? null : startPoseOptions[startPoseIndex];
    }

    public String ownHive() {
        return redAlliance ? "hive_red" : "hive_blue";
    }

    public void teleOpDrive() {
        follower.update();

        if (gamepad1.bWasPressed()) {
            fieldCentric = !fieldCentric;
        }

        if (gamepad1.y && Points.has(ownHive())) {
            aimAt(ownHive());
            return;
        }

        DrivePowers powers = new DrivePowers(-gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_stick_x);

        if (fieldCentric) {
            double allianceOffset = redAlliance ? Math.PI : 0;
            powers = ManualDrive.fieldCentric(powers, follower.pose().heading(), allianceOffset);
        }

        follower.manual(powers);
    }

    public void intake(boolean on) {
        if (intakeMotor != null) {
            intakeMotor.setPower(on ? 1 : 0);
        }
    }

    public void launch(boolean on) {
        if (launchMotor != null) {
            launchMotor.setPower(on ? 1 : 0);
        }
    }

    public double headingTo(double x, double y) {
        Pose pose = follower.pose();
        return Math.atan2(y - pose.y(), x - pose.x());
    }

    public void aimAt(double x, double y) {
        Pose pose = follower.pose();
        follower.hold(new Pose(pose.x(), pose.y(), headingTo(x, y)));
    }

    public void aimAt(String name) {
        Pose target = Points.get(name);
        aimAt(target.x(), target.y());
    }

    public Path pathTo(Pose target) {
        return line(follower.pose(), target).constant(target.heading());
    }

    public Path pathTo(String name) {
        return pathTo(Points.get(name));
    }

    public Path pathToFacing(Pose target, Pose face) {
        return line(follower.pose(), target).facingPoint(face);
    }

    public Path pathToFacing(String target, String face) {
        return pathToFacing(Points.get(target), Points.get(face));
    }

    public Path curveTo(Pose... through) {
        Pose[] points = new Pose[through.length + 1];
        points[0] = follower.pose();
        System.arraycopy(through, 0, points, 1, through.length);
        return curve(points).constant(points[points.length - 1].heading());
    }

    public Path curveTo(String... names) {
        Pose[] poses = new Pose[names.length];
        for (int i = 0; i < names.length; i++) {
            poses[i] = Points.get(names[i]);
        }
        return curveTo(poses);
    }

    public void followTo(Pose target) {
        follower.follow(pathTo(target));
    }

    public void followTo(String name) {
        follower.follow(pathTo(name));
    }

    public void followToFacing(Pose target, Pose face) {
        follower.follow(pathToFacing(target, face));
    }

    public void followToFacing(String target, String face) {
        follower.follow(pathToFacing(target, face));
    }

    public void followCurve(Pose... through) {
        follower.follow(curveTo(through));
    }

    public void followCurve(String... names) {
        follower.follow(curveTo(names));
    }

    public boolean busy() {
        return follower.isBusy();
    }

    public void setStartPose(Pose pose) {
        follower.setPose(pose);
    }

    public void setStartPose(String name) {
        setStartPose(Points.get(name));
    }

    public void updateFollower() {
        follower.update();
    }

    public void start() {
        follower.update();
    }

    public void stop() {
        follower.stop();
        intake(false);
        launch(false);
        vision.close();
    }

    public void updateTelemetry() {
        telemetry.addData("pose", follower.pose());
        telemetry.addData("field centric (b)", fieldCentric);
        telemetry.addData("alliance (x)", redAlliance ? "RED" : "BLUE");
        if (selectedStartPose() != null) {
            telemetry.addData("start pose (dpad)", selectedStartPose());
        }
        telemetry.update();
    }

    public void updateTelemetry(double matchTime) {
        if (!endgameAlerted && matchTime >= 90) {
            gamepad1.rumble(500);
            gamepad2.rumble(500);
            endgameAlerted = true;
        }

        telemetry.addData("match time", "%.0f", matchTime);
        telemetry.addData("endgame", matchTime >= 90);
        updateTelemetry();
    }

    public Step stepTo(String point) {
        return new Step() {
            public void start() {
                followTo(point);
            }

            public boolean isDone() {
                return !busy();
            }
        };
    }

    public Step stepToFacing(String target, String face) {
        return new Step() {
            public void start() {
                followToFacing(target, face);
            }

            public boolean isDone() {
                return !busy();
            }
        };
    }

    public Step stepCurve(String... points) {
        return new Step() {
            public void start() {
                followCurve(points);
            }

            public boolean isDone() {
                return !busy();
            }
        };
    }

    public Step stepAim(String point, double seconds) {
        return Step.timed(seconds, () -> aimAt(point));
    }

    public Step stepIntake(boolean on) {
        return Step.run(() -> intake(on));
    }

    public Step stepIntakeFor(boolean on, double seconds) {
        return new Step() {
            long startTime;

            public void start() {
                intake(on);
                startTime = System.nanoTime();
            }

            public boolean isDone() {
                return (System.nanoTime() - startTime) / 1e9 >= seconds;
            }

            public void stop() {
                intake(false);
            }
        };
    }

    public Step stepLaunch(boolean on) {
        return Step.run(() -> launch(on));
    }

    public Step stepLaunchFor(boolean on, double seconds) {
        return new Step() {
            long startTime;

            public void start() {
                launch(on);
                startTime = System.nanoTime();
            }

            public boolean isDone() {
                return (System.nanoTime() - startTime) / 1e9 >= seconds;
            }

            public void stop() {
                launch(false);
            }
        };
    }
}