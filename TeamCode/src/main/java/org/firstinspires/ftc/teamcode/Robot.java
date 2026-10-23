package org.firstinspires.ftc.teamcode;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;

/** Shared access to the drivetrain, localization, vision, Cell/ball tracking, inventory, and mechanisms. */
public class Robot {

    public final Follower follower;
    public final Localization localization;

    public final GameVision gameVision;
    public final CellVision cellVision;
    public final ScoringMechanism mechanism;

    public final Inventory inventory;
    public final BallMap ballMap;
    public final CellMap cellMap;

    private final Telemetry telemetry;
    private final Gamepad gamepad1;
    private final Gamepad gamepad2;

    public boolean redAlliance;
    private boolean fieldCentric = true;
    private boolean endgameAlerted = false;
    private boolean cellAimHeld = false;
    private double cellAimHoldX;
    private double cellAimHoldY;
    private boolean cellAimFallbackActive;

    private String[] startPoseOptions = new String[0];
    private int startPoseIndex = 0;

    /** The most recent "now" passed to updateWorldModel() - the single
     *  clock every subsystem's timing (mechanism timeouts included) is
     *  measured against. Step helpers below read this instead of taking
     *  their own timestamp parameter, specifically so a Step's start()
     *  (which can run an arbitrary number of loop ticks after the Step
     *  object was constructed) always uses the SAME clock as
     *  updateWorldModel/ScoringMechanism - mixing clocks here previously
     *  would have made every mechanism timeout meaningless. */
    private double lastKnownNowSeconds = 0;

    public Robot(HardwareMap hardwareMap, Telemetry telemetry, Gamepad gamepad1, Gamepad gamepad2, boolean redAlliance) {
        this.telemetry = telemetry;
        this.gamepad1 = gamepad1;
        this.gamepad2 = gamepad2;
        this.redAlliance = redAlliance;

        follower = Constants.create(hardwareMap);
        localization = new Localization(follower);
        // Every fresh Robot starts by wanting a resync: right after
        // init/reset is exactly when odometry is most likely to be wrong
        // (never zeroed, or zeroed at the wrong spot) and a trusted tag
        // sighting should fully win rather than be ignored or lightly
        // blended in - see Localization's Javadoc for why that's the
        // default behavior rather than continuous correction.
        localization.requestResync();

        gameVision = new GameVision(hardwareMap);
        cellVision = new CellVision(hardwareMap);
        mechanism = new ScoringMechanism(hardwareMap);

        inventory = new Inventory();
        ballMap = new BallMap();
        cellMap = new CellMap();
        cellMap.selectOurAlliance(redAlliance);

        telemetry.addData("Status", "Initialized");
        telemetry.update();
    }

    public static Robot resumeFromAuto(HardwareMap hardwareMap, Telemetry telemetry, Gamepad gamepad1, Gamepad gamepad2) {
        boolean alliance = AutoToTeleOp.hasAlliance() && AutoToTeleOp.redAlliance();
        Robot robot = new Robot(hardwareMap, telemetry, gamepad1, gamepad2, alliance);

        if (AutoToTeleOp.hasPose()) {
            robot.setStartPose(AutoToTeleOp.pose());
        }
        if (AutoToTeleOp.hasInventory()) {
            robot.inventory.restoreFrom(AutoToTeleOp.pollenCount(), AutoToTeleOp.nectarCount());
        }

        return robot;
    }

    public void saveForTeleOp() {
        AutoToTeleOp.save(follower.pose(), redAlliance, inventory.pollenCount(), inventory.nectarCount());
    }

    // ---- The closed loop: PERCEIVE ---------------------------------------

    /**
     * Pulls in everything the cameras and mechanism sensors currently
     * know and folds it into the one shared world model. Call this
     * exactly once per OpMode loop() iteration, before making any
     * decisions that tick - AutonomousController does this at the start
     * of every update(), and TeleOp does the same in its loop().
     */
    public void updateWorldModel(double nowSeconds) {
        this.lastKnownNowSeconds = nowSeconds;
        follower.update();
        localization.update(nowSeconds);

        if (cellVision.isAvailable()) {
            for (AprilTagObservation observation : cellVision.observations(nowSeconds)) {
                if (observation.role == AprilTagRole.CELL_TAG) {
                    cellMap.updateFromTag(observation, localization.pose(), nowSeconds);
                } else if (observation.role == AprilTagRole.NAVIGATION_TAG) {
                    Pose knownTagFieldPose = FieldTagLibrary.knownFieldPoseOf(observation.tagId);
                    if (knownTagFieldPose != null) {
                        localization.applyAprilTagCorrection(observation, knownTagFieldPose, nowSeconds);
                    }
                }
            }
        }
        cellMap.tick(nowSeconds);

        if (gameVision.isAvailable()) {
            ballMap.observe(gameVision.detections(localization.pose(), nowSeconds, redAlliance),
                    localization.pose(), gameVision.fieldOfView(), nowSeconds);
        }

        mechanism.update(nowSeconds);
        // "mechanisms change the world -> sensors observe again -> the
        // world model reflects it": inventory only ever changes here,
        // from a mechanism event that was actually verified (or, lacking
        // a sensor, explicitly and visibly assumed - see ScoringMechanism)
        // - never from an assumption made anywhere else in the project.
        if (mechanism.justReachedReadyToScore()) {
            inventory.acquire(mechanism.lastTransferredType());
        }
        if (mechanism.justCompletedScoreCycle()) {
            inventory.scoreAll();
        }
    }

    /** A read-only view of the current world model for the planner/controller. */
    public WorldState worldState(double timeRemainingSeconds, double nowSeconds) {
        return new WorldState(localization, inventory, ballMap, cellMap,
                gameVision.isAvailable(), cellVision.isAvailable(), timeRemainingSeconds, nowSeconds);
    }

    // ---- Alliance / starting configuration -------------------------------

    public void toggleAlliance() {
        if (gamepad1.xWasPressed()) {
            redAlliance = !redAlliance;
            cellMap.selectOurAlliance(redAlliance);
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

    // ---- TeleOp driving ---------------------------------------------------

    public void teleOpDrive() {
        if (gamepad1.bWasPressed()) {
            fieldCentric = !fieldCentric;
        }

        if (gamepad1.y) {
            Pose pose = follower.pose();
            if (!cellAimHeld) {
                cellAimHoldX = pose.x();
                cellAimHoldY = pose.y();
                cellAimHeld = true;
            }

            Cell targetCell = cellMap.bestOurCell(pose);
            double targetX;
            double targetY;
            cellAimFallbackActive = targetCell == null
                    || targetCell.state() == CellState.UNKNOWN
                    || targetCell.state() == CellState.STALE;

            if (cellAimFallbackActive) {
                java.util.List<Cell> cells = cellMap.ourCells();
                targetX = (cells.get(0).fieldPose().x() + cells.get(1).fieldPose().x()) / 2.0;
                targetY = (cells.get(0).fieldPose().y() + cells.get(1).fieldPose().y()) / 2.0;
            } else {
                targetX = targetCell.fieldPose().x();
                targetY = targetCell.fieldPose().y();
            }

            double heading = Math.atan2(targetY - pose.y(), targetX - pose.x());
            follower.hold(new Pose(cellAimHoldX, cellAimHoldY, heading));
            return;
        }
        cellAimHeld = false;
        cellAimFallbackActive = false;

        if (gamepad1.left_bumper && gameVision.seesPollen()) {
            trackPollen();
            return;
        }

        double precision = 1 - gamepad1.right_trigger * 0.75;
        DrivePowers powers = new DrivePowers(
                -gamepad1.left_stick_y * precision,
                -gamepad1.left_stick_x * precision,
                -gamepad1.right_stick_x * precision);

        if (fieldCentric) {
            double allianceOffset = redAlliance ? Math.PI : 0;
            powers = ManualDrive.fieldCentric(powers, follower.pose().heading(), allianceOffset);
        }

        // driveOrHold needs the tuned Foresight controller; diagnostic OpModes use manual().
        ManualDrive.driveOrHold(follower, powers);
    }

    public void trackPollen() {
        double offset = gameVision.pollenOffset();
        double forward = 0.3 * (1 - Math.abs(offset));
        follower.manual(new DrivePowers(forward, 0, -offset * 0.5));
    }

    // ---- Aiming / navigation helpers ---------------------------------------

    public double headingTo(double x, double y) {
        Pose pose = follower.pose();
        return Math.atan2(y - pose.y(), x - pose.x());
    }

    public void aimAt(double x, double y) {
        Pose pose = follower.pose();
        follower.hold(new Pose(pose.x(), pose.y(), headingTo(x, y)));
    }

    public void aimAt(String pointName) {
        Pose target = Points.get(pointName);
        aimAt(target.x(), target.y());
    }

    public void aimAtCell(Cell cell) {
        if (cell == null) return;
        aimAt(cell.fieldPose().x(), cell.fieldPose().y());
    }

    public void aimAtOwnCell() {
        aimAtCell(cellMap.bestOurCell(follower.pose()));
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

    // ---- Lifecycle ----------------------------------------------------------

    public void stop() {
        follower.stop();
        mechanism.stopAll();
        gameVision.close();
        cellVision.close();
    }

    public void updateTelemetry() {
        telemetry.addData("pose", follower.pose());
        telemetry.addData("field centric (b)", fieldCentric);
        telemetry.addData("alliance (x)", redAlliance ? "RED" : "BLUE");
        Cell targetCell = cellMap.bestOurCell(follower.pose());
        telemetry.addData("aim (hold Y)", targetCell == null ? "no Cell" :
                targetCell.alliance + " " + targetCell.fieldSide + " " + targetCell.state());
        boolean targetUntracked = targetCell == null
                || targetCell.state() == CellState.UNKNOWN
                || targetCell.state() == CellState.STALE;
        telemetry.addData("AIM WARNING", targetUntracked
                ? "Cell not tracked; hold Y aims between our Cell estimates" : "none");
        telemetry.addData("inventory", inventory);
        telemetry.addData("mechanism", mechanism.state());
        if (selectedStartPose() != null) {
            telemetry.addData("start pose (dpad)", selectedStartPose());
        }
        telemetry.update();
    }

    public void updateTelemetry(double matchTimeSeconds) {
        if (!endgameAlerted && matchTimeSeconds >= 90) {
            // A few short blips reads as a distinct "something changed"
            // alert better than one continuous buzz - both rumble(ms) and
            // rumbleBlips(count) are real Gamepad methods; this just picks
            // the more informative one for this specific alert.
            gamepad1.rumbleBlips(3);
            gamepad2.rumbleBlips(3);
            endgameAlerted = true;
        }

        telemetry.addData("match time", "%.0f", matchTimeSeconds);
        telemetry.addData("endgame", matchTimeSeconds >= 90);
        updateTelemetry();
    }

    // ---- Step helpers (simple linear autonomous / teaching sequences) -----
    //
    // These build on the real ScoringMechanism state machine now, not raw
    // motor power - see project brief section 29: a Step should represent
    // an executable action, and dynamic autonomous (AutonomousController)
    // is what actually needs to interrupt/rebuild a sequence, not this
    // fixed-Sequence path. These remain useful for LeaveAndPark and the
    // teaching-oriented AutoTemplate.

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

    public Step stepAimAtOwnCell(double seconds) {
        return Step.timed(seconds, this::aimAtOwnCell);
    }

    /**
     * Runs an intake attempt through the real mechanism state machine
     * until a ball reaches the ready position (or the attempt gives up -
     * see ScoringMechanism). Requires updateWorldModel() to be called each
     * loop tick by the OpMode (it drives mechanism.update() so this step
     * can observe progress) - see AutoTemplate/LeaveAndPark for the
     * pattern.
     */
    public Step stepIntake(BallType expectedType) {
        return new Step() {
            public void start() {
                mechanism.startIntake(expectedType, lastKnownNowSeconds);
            }

            public boolean isDone() {
                return mechanism.state() == ScoringState.READY_TO_SCORE
                        || mechanism.state() == ScoringState.INTAKE_OFF
                        || mechanism.state() == ScoringState.FAULT;
            }
        };
    }

    /** Fires the outtake and waits for the mechanism to finish its cycle. */
    public Step stepScore() {
        return new Step() {
            public void start() {
                mechanism.triggerOuttake(lastKnownNowSeconds);
            }

            public boolean isDone() {
                return mechanism.state() == ScoringState.INTAKE_OFF
                        || mechanism.state() == ScoringState.READY_TO_SCORE;
            }
        };
    }
}
