package org.firstinspires.ftc.teamcode.pedro.procedures;

import com.pedropathing.algorithm.Algorithm;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.drivetrain.Drivetrain;
import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Localizer;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.interpolator.Interpolator;
import com.pedropathing.tuning.autotune.DisplayName;
import com.pedropathing.tuning.autotune.Inputs;
import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.TuningOpMode;
import com.pedropathing.utils.Angle;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;

public class Tests extends Procedure {
    enum Test {
        @DisplayName("Hold Test")
        HOLD,
        @DisplayName("Line Test")
        LINE,
        @DisplayName("Curve Test")
        CURVED,
        @DisplayName("Interpolation Test")
        INTERPOLATION_CURVED,
        @DisplayName("Localization Test")
        LOCALIZATION,
        @DisplayName("Odometry Test")
        ODOMETRY,
        @DisplayName("Driving Test")
        DRIVING,
        @DisplayName("Pose Test")
        POSE
    }
    Function<HardwareMap, Drivetrain> drivetrainFunction;
    Function<HardwareMap, Localizer> localizerFunction;
    Supplier<Algorithm> algorithmSupplier;
    Function<HardwareMap, Follower> followerFunction;

    public Tests(Function<HardwareMap, Drivetrain> drivetrainFunction, Function<HardwareMap, Localizer> localizerFunction, Supplier<Algorithm> algorithmSupplier) {
        super("Tests", "A procedure for testing the Follower.");
        this.drivetrainFunction = drivetrainFunction;
        this.localizerFunction = localizerFunction;
        this.algorithmSupplier = algorithmSupplier;
    }

    @Override
    public void run() throws InterruptedException {
        boolean completed = false;
        boolean algorithm = true, localizer = true, drivetrain = true;

        if (algorithmSupplier == null)
            algorithm = false;

        if (localizerFunction == null)
            localizer = false;

        if (drivetrainFunction == null)
            drivetrain = false;

        if (algorithm && localizer && drivetrain)
            followerFunction = (hardwareMap) -> new Follower(localizerFunction.apply(hardwareMap), drivetrainFunction.apply(hardwareMap), algorithmSupplier.get());

        Inputs inputs = inputs("Select", "Select");
        Inputs.Field<Test> selectedTest = inputs.e("Test", Test.class).withDefault(Test.LINE);
        Inputs.Field<Double> distance = inputs.d("Distance in inches").withDefault(24.0);

        awaitInputs(inputs);
        if (selectedTest.get() == Test.LINE
                || selectedTest.get() == Test.CURVED
                || selectedTest.get() == Test.INTERPOLATION_CURVED) {
            if (distance.get() == null || !Double.isFinite(distance.get()) || distance.get() <= 0) {
                throw new IllegalArgumentException("Path test distance must be a positive number of inches.");
            }
        }

        switch (selectedTest.get()) {
            case HOLD:
                if (!algorithm)
                    throw new IllegalArgumentException("The selected test requires the follower algorithm.");
                completed = runOpMode(new TestsHold(followerFunction));
                break;
            case LINE:
                if (!algorithm)
                    throw new IllegalArgumentException("Algorithm is required for Hold Test.");
                completed = runOpMode(new TestsLine(followerFunction, distance.get()));
                break;
            case CURVED:
                if (!algorithm)
                    throw new IllegalArgumentException("Algorithm is required for Hold Test.");
                completed = runOpMode(new TestsCurve(followerFunction, distance.get()));
                break;
            case INTERPOLATION_CURVED:
                if (!algorithm)
                    throw new IllegalArgumentException("Algorithm is required for Hold Test.");
                completed = runOpMode(new TestsInterpolation(followerFunction, distance.get()));
                break;
            case LOCALIZATION:
                if (!drivetrain)
                    throw new IllegalArgumentException("Drivetrain is required for Localization Test.");
                if (!localizer)
                    throw new IllegalArgumentException("Localizer is required for Localization Test.");
                completed = runOpMode(new TestsLocalization(drivetrainFunction, localizerFunction));
                break;
            case ODOMETRY:
                if (!drivetrain)
                    throw new IllegalArgumentException("Drivetrain is required for Odometry Test.");
                if (!localizer)
                    throw new IllegalArgumentException("Localizer is required for Odometry Test.");
                completed = runOpMode(new TestsOdometry(drivetrainFunction, localizerFunction));
                if (!completed)
                    abort("Failed odometry test. Please check your odometry pods and ensure they are functioning correctly.");
                break;
            case POSE:
                if (!localizer)
                    throw new IllegalArgumentException("Localizer is required for Pose Test.");
                completed = runOpMode(new TestsPose(localizerFunction));
                break;
            case DRIVING:
                if (!drivetrain)
                    throw new IllegalArgumentException("Drivetrain is required for Driving Test.");
                completed = runOpMode(new TestsDriving(drivetrainFunction));
                break;
        }

        result("Completed", completed);
    }
}

class TestsHold extends TuningOpMode<Boolean> {
    Function<HardwareMap, Follower> followerFunction;

    public TestsHold(Function<HardwareMap, Follower> followerFunction) {
        super("Hold Test", "Tests the Follower's ability to hold a position.", true);
        this.followerFunction = followerFunction;
    }

    @Override
    public Boolean runTuningOpMode() throws InterruptedException {
        Follower follower = followerFunction.apply(hardwareMap);
        try {
            follower.setPose(Pose.zero());
            Thread.sleep(1000);
            waitForStart();
            follower.setPose(Pose.zero());
            follower.update();
            follower.hold(Pose.zero());
            while (opModeIsActive()) {
                follower.update();
                idle();
            }
        } finally {
            follower.stop();
        }
        return true;
    }
}

class TestsLine extends TuningOpMode<Boolean> {
    private final Function<HardwareMap, Follower> followerFunction;
    private final double distance;

    public TestsLine(Function<HardwareMap, Follower> followerFunction, double distance) {
        super("Line Test", "Tests the Follower's ability to follow a line.", true);
        this.followerFunction = followerFunction;
        this.distance = distance;
    }

    @Override
    public Boolean runTuningOpMode() throws InterruptedException {
        Follower follower = followerFunction.apply(hardwareMap);

        try {
            Path forwardPath = line(Pose.zero(), new Pose(distance, 0, 0)).constant(0);
            Path returnPath = line(new Pose(distance, 0, 0), Pose.zero()).constant(0);
            boolean followingForward = true;

            follower.setPose(Pose.zero());
            Thread.sleep(1000);
            waitForStart();
            follower.setPose(Pose.zero());
            follower.update();
            follower.follow(forwardPath);

            while (opModeIsActive()) {
                follower.update();

                if (follower.atParametricEnd()) {
                    follower.follow(followingForward ? returnPath : forwardPath);
                    followingForward = !followingForward;
                }

                idle();
            }
        } finally {
            follower.stop();
        }

        return true;
    }
}

class TestsCurve extends TuningOpMode<Boolean> {
    private final Function<HardwareMap, Follower> followerFunction;
    private final double distance;

    public TestsCurve(Function<HardwareMap, Follower> followerFunction, double distance) {
        super("Curve Test", "Tests the Follower's ability to follow a curve.", true);
        this.followerFunction = followerFunction;
        this.distance = distance;
    }

    @Override
    public Boolean runTuningOpMode() throws InterruptedException {
        Follower follower = followerFunction.apply(hardwareMap);

        try {
            follower.setPose(Pose.zero());

            Path forwardPath = curve(
                    Pose.zero(),
                    new Pose(distance, 0),
                    new Pose(distance, distance)
            ).tangent();

            Path returnPath = curve(
                    new Pose(distance, distance),
                    new Pose(distance, 0),
                    Pose.zero()
            ).tangent();

            boolean followingForward = true;

            Thread.sleep(1000);
            waitForStart();
            follower.setPose(Pose.zero());
            follower.update();
            follower.follow(forwardPath);

            while (opModeIsActive()) {
                follower.update();

                if (follower.atParametricEnd()) {
                    follower.follow(followingForward ? returnPath : forwardPath);
                    followingForward = !followingForward;
                }

                idle();
            }
        } finally {
            follower.stop();
        }

        return true;
    }
}

class TestsInterpolation extends TuningOpMode<Boolean> {
    private final Function<HardwareMap, Follower> followerFunction;
    private final double distance;

    public TestsInterpolation(Function<HardwareMap, Follower> followerFunction, double distance) {
        super("Interpolation Curve Test",
                "Tests the Follower's ability to follow a curve with several interpolations.", true);
        this.followerFunction = followerFunction;
        this.distance = distance;
    }

    @Override
    public Boolean runTuningOpMode() throws InterruptedException {
        Follower follower = followerFunction.apply(hardwareMap);

        try {
            follower.setPose(Pose.zero());

            Path forwardPath = curve(
                    Pose.zero(),
                    new Pose(distance, 0),
                    new Pose(distance, distance)
            ).heading((path, t) -> Math.PI);

            Path returnPath = curve(
                    new Pose(distance, distance),
                    new Pose(distance, 0),
                    Pose.zero()
            ).heading(Interpolator.piecewise()
                    .until(0.5, Interpolator.tangent)
                    .until(1.0, Interpolator.constant(0)));

            boolean followingForward = true;

            Thread.sleep(1000);
            waitForStart();
            follower.setPose(Pose.zero());
            follower.update();
            follower.follow(forwardPath);

            while (opModeIsActive()) {
                follower.update();

                if (follower.atParametricEnd()) {
                    follower.follow(followingForward ? returnPath : forwardPath);
                    followingForward = !followingForward;
                }

                idle();
            }
        } finally {
            follower.stop();
        }

        return true;
    }
}

class TestsLocalization extends TuningOpMode<Boolean> {
    Function<HardwareMap, Drivetrain> drivetrainFunction;
    Function<HardwareMap, Localizer> localizerFunction;

    public TestsLocalization(Function<HardwareMap, Drivetrain> drivetrainFunction, Function<HardwareMap, Localizer> localizerFunction) {
        super("Odometry Test", "Checks the direction and scale of both odometry axes and heading.", true);
        this.drivetrainFunction = drivetrainFunction;
        this.localizerFunction = localizerFunction;
    }

    @Override
    public Boolean runTuningOpMode() throws InterruptedException {
        Localizer localizer = localizerFunction.apply(hardwareMap);
        Drivetrain drivetrain = drivetrainFunction.apply(hardwareMap);

        try {
            localizer.setPose(Pose.zero());

            Thread.sleep(1000);
            waitForStart();
            localizer.setPose(Pose.zero());

            while (opModeIsActive()) {
                drivetrain.drive(new DrivePowers(
                        -gamepad1.left_stick_y,
                        gamepad1.left_stick_x,
                        gamepad1.right_stick_x
                ), true);
                localizer.update();
                telemetry.addData("Pose", localizer.pose());
                telemetry.update();
                idle();
            }
        } finally {
            drivetrain.stop();
        }
        return true;
    }
}

class TestsOdometry extends TuningOpMode<Boolean> {
    private enum Test { FORWARD, LEFT, TURN, IDLE }

    private final Function<HardwareMap, Drivetrain> drivetrainFunction;
    private final Function<HardwareMap, Localizer> localizerFunction;
    private final ElapsedTime timer = new ElapsedTime();

    private Test test = Test.FORWARD;
    private double totalHeading;
    private double previousHeading;
    private boolean passedX;
    private boolean passedY;
    private boolean passedHeading;

    public static double POWER = 0.5;

    public TestsOdometry(
            Function<HardwareMap, Drivetrain> drivetrainFunction,
            Function<HardwareMap, Localizer> localizerFunction) {
        super("Odometry Test", "Checks the direction and scale of both odometry axes and heading.", true);
        this.drivetrainFunction = drivetrainFunction;
        this.localizerFunction = localizerFunction;
    }

    @Override
    public Boolean runTuningOpMode() throws InterruptedException {
        Localizer localizer = localizerFunction.apply(hardwareMap);
        Drivetrain drivetrain = drivetrainFunction.apply(hardwareMap);

        try {
            localizer.setPose(Pose.zero());
            Thread.sleep(1000);
            waitForStart();
            localizer.setPose(Pose.zero());
            timer.reset();

            while (opModeIsActive()) {
                localizer.update();

                if (localizer.pose().x() != 0
                        || localizer.pose().y() != 0
                        || localizer.pose().heading() != 0) {
                    double heading = localizer.pose().heading();
                    totalHeading += Angle.normalizeSigned(heading - previousHeading);
                    previousHeading = heading;
                }

                switch (test) {
                    case FORWARD:
                        drivetrain.drive(new DrivePowers(POWER, 0, 0), false);
                        if (timer.seconds() > 1) {
                            test = Test.LEFT;
                            timer.reset();
                        }
                        break;

                    case LEFT:
                        drivetrain.drive(new DrivePowers(0, POWER, 0), false);
                        if (timer.seconds() > 1) {
                            test = Test.TURN;
                            timer.reset();
                            previousHeading = localizer.pose().heading();
                            totalHeading = 0;
                        }
                        break;

                    case TURN:
                        drivetrain.drive(new DrivePowers(0, 0, POWER), false);
                        if (timer.seconds() > 1) {
                            test = Test.IDLE;
                            timer.reset();
                        }
                        break;

                    case IDLE:
                        drivetrain.stop();
                        telemetry.addData("Test", "completed");
                        Pose measuredPose = localizer.pose();
                        passedX = reportAxisResult("X pod", measuredPose.x(), 2, 144);
                        passedY = reportAxisResult("Y pod", measuredPose.y(), 2, 144);
                        passedHeading = reportHeadingResult(totalHeading);
                        break;
                }

                telemetry.addData("Pose", localizer.pose());
                telemetry.update();
                idle();
            }
        } finally {
            drivetrain.stop();
        }

        return passedX && passedY && passedHeading;
    }

    private boolean reportAxisResult(String name, double value, double minimum, double maximum) {
        if (value < 0) {
            telemetry.addData(name + " direction", "flipped");
            return false;
        }
        if (value < minimum) {
            telemetry.addData(name + " scale", "too small");
            return false;
        }
        if (value > maximum) {
            telemetry.addData(name + " scale", "too large");
            return false;
        }

        telemetry.addData(name, "good");
        return true;
    }

    private boolean reportHeadingResult(double headingChange) {
        if (headingChange < 0) {
            telemetry.addData("Heading direction", "flipped");
            return false;
        }
        if (headingChange < 0.02) {
            telemetry.addData("Heading scale", "too small");
            return false;
        }
        if (headingChange > 2 * Math.PI) {
            telemetry.addData("Heading scale", "too large");
            return false;
        }

        telemetry.addData("Heading", "good");
        return true;
    }
}

class TestsDriving extends TuningOpMode<Boolean> {
    Function<HardwareMap, Drivetrain> drivetrainFunction;

    public TestsDriving(Function<HardwareMap, Drivetrain> drivetrainFunction) {
        super("Driving Test", "Tests raw drivetrain control without localization.", true);
        this.drivetrainFunction = drivetrainFunction;
    }

    @Override
    public Boolean runTuningOpMode() throws InterruptedException {
        Drivetrain drivetrain = drivetrainFunction.apply(hardwareMap);
        try {
            waitForStart();
            while (opModeIsActive()) {
                drivetrain.drive(new DrivePowers(
                        -gamepad1.left_stick_y,
                        gamepad1.left_stick_x,
                        gamepad1.right_stick_x
                ), true);
                idle();
            }
        } finally {
            drivetrain.stop();
        }
        return true;
    }
}

class TestsPose extends TuningOpMode<Boolean> {
    private final Function<HardwareMap, Localizer> localizerFunction;

    public TestsPose(Function<HardwareMap, Localizer> localizerFunction) {
        super("Pose Test", "Displays live localization without commanding the drivetrain.", true);
        this.localizerFunction = localizerFunction;
    }

    @Override
    public Boolean runTuningOpMode() throws InterruptedException {
        Localizer localizer = localizerFunction.apply(hardwareMap);

        localizer.setPose(Pose.zero());
        Thread.sleep(1000);
        waitForStart();
        localizer.setPose(Pose.zero());
        localizer.update();

        while (opModeIsActive()) {
            localizer.update();
            telemetry.addData("Robot pose", localizer.pose());
            telemetry.update();
            idle();
        }

        return true;
    }
}

