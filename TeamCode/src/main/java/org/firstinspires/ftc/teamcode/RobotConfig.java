package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Pose;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/** Hardware names and values that are expected to change during robot testing. */
public final class RobotConfig {
    private RobotConfig() {}

    public enum Alliance { RED, BLUE }
    public enum StartPosition { SIDE_WALL, NEAR_CORNER, FAR_CORNER }
    public enum Flower { LEFT_WALL, TOP_WALL, RIGHT_WALL, BOTTOM_WALL }
    public enum RouteLane { INSIDE_HIVE, OUTSIDE_HIVE }
    public enum HiveSide {
        AUDIENCE, OPPOSITE_AUDIENCE;

        public HiveSide opposite() {
            return this == AUDIENCE ? OPPOSITE_AUDIENCE : AUDIENCE;
        }
    }

    public static final class AutoPlan {
        public Alliance alliance = Alliance.RED;
        public StartPosition start = StartPosition.SIDE_WALL;
        public Flower flower = Flower.LEFT_WALL;
        public RouteLane lane = RouteLane.OUTSIDE_HIVE;
        public HiveSide manualStartingHiveSide = defaultStartingHiveSide(Alliance.RED);
        public boolean preloadExpectedToTip = false;

        private HiveSide cameraStartingHiveSide;
        private String cameraClusterName = "No stable cluster";
        private boolean initialStateFrozen;
        private HiveSide frozenStartingHiveSide;
        private String frozenStateSource;
        private HiveSide postPreloadCameraHiveSide;
        private String postPreloadCameraClusterName;

        public void toggleAlliance() {
            alliance = alliance == Alliance.RED ? Alliance.BLUE : Alliance.RED;
            manualStartingHiveSide = defaultStartingHiveSide(alliance);
            clearCameraReading();
        }

        public void cycleStart(int direction) {
            start = StartPosition.values()[cycle(start.ordinal(), StartPosition.values().length, direction)];
        }

        public void cycleFlower(int direction) {
            flower = Flower.values()[cycle(flower.ordinal(), Flower.values().length, direction)];
        }

        public void toggleLane() {
            lane = lane == RouteLane.INSIDE_HIVE ? RouteLane.OUTSIDE_HIVE : RouteLane.INSIDE_HIVE;
        }

        public void toggleManualHiveSide() {
            manualStartingHiveSide = manualStartingHiveSide.opposite();
        }

        public void togglePreloadTip() {
            preloadExpectedToTip = !preloadExpectedToTip;
        }

        /** Called during INIT. After start, a reading is treated as a post-preload update. */
        public void setCameraReading(HiveSide side, String clusterName) {
            if (side == null) return;

            if (!initialStateFrozen) {
                cameraStartingHiveSide = side;
                cameraClusterName = clusterName == null ? "Camera" : clusterName;
            } else {
                postPreloadCameraHiveSide = side;
                postPreloadCameraClusterName = clusterName == null ? "Camera" : clusterName;
            }
        }

        public void clearCameraReading() {
            cameraStartingHiveSide = null;
            cameraClusterName = "No stable cluster";
            initialStateFrozen = false;
            frozenStartingHiveSide = null;
            frozenStateSource = null;
            postPreloadCameraHiveSide = null;
            postPreloadCameraClusterName = null;
        }

        /** Freeze the initial state before the autonomous sequence starts. */
        public void freezeInitialState() {
            if (initialStateFrozen) return;
            frozenStartingHiveSide = cameraStartingHiveSide != null
                    ? cameraStartingHiveSide : manualStartingHiveSide;
            frozenStateSource = cameraStartingHiveSide != null
                    ? "CAMERA: " + cameraClusterName : "ASSUMED: INIT selection";
            initialStateFrozen = true;
        }

        public boolean hasCameraStartingReading() {
            return cameraStartingHiveSide != null;
        }

        public HiveSide startingHiveSide() {
            if (initialStateFrozen) return frozenStartingHiveSide;
            return cameraStartingHiveSide != null ? cameraStartingHiveSide : manualStartingHiveSide;
        }

        public HiveSide hiveSideAfterPreload() {
            if (postPreloadCameraHiveSide != null) return postPreloadCameraHiveSide;
            HiveSide startSide = startingHiveSide();
            return preloadExpectedToTip ? startSide.opposite() : startSide;
        }

        public String hiveStateSource() {
            if (postPreloadCameraHiveSide != null) {
                return "CAMERA after preload: " + postPreloadCameraClusterName;
            }
            if (initialStateFrozen) return frozenStateSource;
            return cameraStartingHiveSide != null
                    ? "CAMERA: " + cameraClusterName : "ASSUMED: INIT selection";
        }

        public RouteLane partnerLane() {
            return lane == RouteLane.INSIDE_HIVE ? RouteLane.OUTSIDE_HIVE : RouteLane.INSIDE_HIVE;
        }

        public Pose startPose() {
            return RobotConfig.startPose(alliance, start);
        }
    }

    public static final String FRONT_LEFT = "fl";
    public static final String FRONT_RIGHT = "fr";
    public static final String BACK_LEFT = "bl";
    public static final String BACK_RIGHT = "br";

    public static final String ODOMETRY = "odom";
    public static final String APRILTAG_CAMERA = "Webcam 2";

    public static final String INTAKE = "intake";
    public static final String TRANSFER = "transfer";
    public static final String OUTTAKE_LEFT = "outtakeLeft";
    public static final String OUTTAKE_RIGHT = "outtakeRight";

    public static final DcMotorSimple.Direction FRONT_LEFT_DIRECTION = DcMotorSimple.Direction.REVERSE;
    public static final DcMotorSimple.Direction FRONT_RIGHT_DIRECTION = DcMotorSimple.Direction.REVERSE;
    public static final DcMotorSimple.Direction BACK_LEFT_DIRECTION = DcMotorSimple.Direction.REVERSE;
    public static final DcMotorSimple.Direction BACK_RIGHT_DIRECTION = DcMotorSimple.Direction.FORWARD;

    public static final DcMotorSimple.Direction INTAKE_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction TRANSFER_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction OUTTAKE_LEFT_DIRECTION = DcMotorSimple.Direction.FORWARD;
    public static final DcMotorSimple.Direction OUTTAKE_RIGHT_DIRECTION = DcMotorSimple.Direction.FORWARD;

    public static final double DRIVE_PRECISION_SCALE = 0.35;

    public static final double AUTO_TEST_POWER = 0.20;
    public static final double AUTO_TEST_MOVE_SECONDS = 0.20;
    public static final double AUTO_TEST_PAUSE_SECONDS = 0.25;

    // Measure the assembled robot, including bumpers. Poses refer to its center.
    public static final double ROBOT_WIDTH_INCHES = Double.NaN;
    public static final double ROBOT_LENGTH_INCHES = Double.NaN;
    public static final double FIELD_SIZE_INCHES = 144.0;
    public static final double FIELD_MIDLINE_X = FIELD_SIZE_INCHES / 2.0;
    public static final double CORNER_INSET_INCHES = 9.0;

    public static final double PINPOINT_Y_POD_OFFSET_INCHES = 3.75;
    public static final double PINPOINT_X_POD_OFFSET_INCHES = 0.20;
    public static final GoBildaPinpointDriver.EncoderDirection PINPOINT_Y_DIRECTION =
            GoBildaPinpointDriver.EncoderDirection.REVERSED;
    public static final GoBildaPinpointDriver.EncoderDirection PINPOINT_X_DIRECTION =
            GoBildaPinpointDriver.EncoderDirection.REVERSED;

    public static final double INTAKE_POWER = 1.0;
    public static final double INTAKE_SECONDS = 2.5;
    public static final double TRANSFER_POWER = 1.0;
    public static final double TRANSFER_SECONDS = 1.5;
    public static final double OUTTAKE_LEFT_POWER = 1.0;
    public static final double OUTTAKE_RIGHT_POWER = 1.0;
    public static final double OUTTAKE_SECONDS = 1.0;
    public static final double CLEAR_SECONDS = 0.3;

    /** INIT fallback from the season's starting setup; the driver can toggle it if needed. */
    public static HiveSide defaultStartingHiveSide(Alliance alliance) {
        return alliance == Alliance.RED ? HiveSide.AUDIENCE : HiveSide.OPPOSITE_AUDIENCE;
    }

    /** Three team start presets, for both alliances. Each pose is at the chassis center. */
    public static Pose startPose(Alliance alliance, StartPosition start) {
        requireRobotDimensions();

        double halfWidth = ROBOT_WIDTH_INCHES / 2.0;
        double halfLength = ROBOT_LENGTH_INCHES / 2.0;
        double field = FIELD_SIZE_INCHES;

        switch (start) {
            case NEAR_CORNER:
                return alliance == Alliance.RED
                        ? new Pose(halfWidth + CORNER_INSET_INCHES, field - halfLength, -Math.PI / 2.0)
                        : new Pose(field - halfWidth - CORNER_INSET_INCHES, halfLength, Math.PI / 2.0);

            case FAR_CORNER:
                return alliance == Alliance.RED
                        ? new Pose(halfWidth + CORNER_INSET_INCHES, halfLength, Math.PI / 2.0)
                        : new Pose(field - halfWidth - CORNER_INSET_INCHES, field - halfLength, -Math.PI / 2.0);

            case SIDE_WALL:
            default:
                return alliance == Alliance.RED
                        ? robotCenterFromWall(0.0, field / 2.0, 0.0, 0.0)
                        : robotCenterFromWall(field, field / 2.0, Math.PI, 0.0);
        }
    }

    /** Converts a wall location and inward heading into a chassis-center pose. */
    public static Pose robotCenterFromWall(
            double wallX, double wallY, double inwardHeading, double clearanceInches) {
        requireRobotDimensions();
        double halfExtent = Math.abs(Math.cos(inwardHeading)) * ROBOT_LENGTH_INCHES / 2.0
                + Math.abs(Math.sin(inwardHeading)) * ROBOT_WIDTH_INCHES / 2.0;
        double centerDistance = halfExtent + clearanceInches;
        return new Pose(
                wallX + Math.cos(inwardHeading) * centerDistance,
                wallY + Math.sin(inwardHeading) * centerDistance,
                inwardHeading
        );
    }

    /** True only when the full robot footprint remains on its own half of the field. */
    public static boolean staysOnAllianceHalf(Pose pose, Alliance alliance) {
        requireRobotDimensions();
        double halfExtentX = Math.abs(Math.cos(pose.heading())) * ROBOT_LENGTH_INCHES / 2.0
                + Math.abs(Math.sin(pose.heading())) * ROBOT_WIDTH_INCHES / 2.0;
        return alliance == Alliance.RED
                ? pose.x() + halfExtentX <= FIELD_MIDLINE_X
                : pose.x() - halfExtentX >= FIELD_MIDLINE_X;
    }

    public static boolean geometryMeasured() {
        return Double.isFinite(ROBOT_WIDTH_INCHES) && ROBOT_WIDTH_INCHES > 0
                && Double.isFinite(ROBOT_LENGTH_INCHES) && ROBOT_LENGTH_INCHES > 0;
    }

    private static void requireRobotDimensions() {
        if (!geometryMeasured()) {
            throw new IllegalStateException("Measure ROBOT_WIDTH_INCHES and ROBOT_LENGTH_INCHES first.");
        }
    }

    private static int cycle(int current, int length, int direction) {
        int next = (current + direction) % length;
        return next < 0 ? next + length : next;
    }

    public static MecanumConfig mecanumConfig() {
        return new MecanumConfig(c -> {
            c.frontLeftName.set(FRONT_LEFT);
            c.frontRightName.set(FRONT_RIGHT);
            c.backLeftName.set(BACK_LEFT);
            c.backRightName.set(BACK_RIGHT);
            c.frontLeftDirection.set(FRONT_LEFT_DIRECTION);
            c.frontRightDirection.set(FRONT_RIGHT_DIRECTION);
            c.backLeftDirection.set(BACK_LEFT_DIRECTION);
            c.backRightDirection.set(BACK_RIGHT_DIRECTION);
            c.manualBrakeMode.set(true);
        });
    }

    public static PinpointConfig pinpointConfig() {
        return new PinpointConfig(c -> {
            c.name.set(ODOMETRY);
            c.yPodOffset.set(PINPOINT_Y_POD_OFFSET_INCHES);
            c.xPodOffset.set(PINPOINT_X_POD_OFFSET_INCHES);
            c.yPodDirection.set(PINPOINT_Y_DIRECTION);
            c.xPodDirection.set(PINPOINT_X_DIRECTION);
        });
    }
}
