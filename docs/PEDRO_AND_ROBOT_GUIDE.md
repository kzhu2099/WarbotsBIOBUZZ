# Robot and PedroPathing setup

This guide describes the code in this repository. Hardware names and the values currently in source are configuration starting points; they are not proof that the assembled robot has been calibrated or tested.

The project uses FTC SDK 12.0.0, PedroPathing RevHub 3.0.2, and Pedro AutoTune 1.0.3. These dependencies are declared in `build.dependencies.gradle`.

## 1. Where each configuration belongs

| File | Edit it for |
|---|---|
| `pedro/Constants.java` | All Pedro settings: four drive motor names/directions, Pinpoint name/pod settings/offsets/directions/units, and the tuned Foresight configuration |
| `pedro/Tuning.java` | Which Pedro AutoTune procedures are registered; normally add or remove registrations here, not calibration values |
| `pedro/procedures/` | Pedro's Mecanum, Pinpoint, Foresight, and Follower test procedures; these are imported tuning utilities, not the robot's path or mechanism code |
| `RobotConfig.java` | Non-Pedro robot settings: intake/transfer/outtake hardware names and directions, mechanism powers/times, joystick deadband, and field/path-test dimensions and waypoints |
| `RobotHardware.java` | Direct mecanum drive and timed/manual mechanism behavior |
| `PathTestAuto.java` | The selected start poses, waypoints, and ordered path-test sequence |
| `TeleOpPedro.java` | Manual drive through Pedro, plus the mechanism controls |
| `docs/ROBOT_TESTS.md` | Hardware bring-up and Driver Station test instructions |

**Keep the Pedro configuration in `pedro/Constants.java`.** This follows Pedro's documented three-part structure: drivetrain config, localizer config, and algorithm config. Its `create(HardwareMap)` method assembles the matching localizer, drivetrain, and algorithm into one Follower. See the [Pedro Constants guide](https://pedropathing.com/docs/pathing/tuning/constants).

### Current starting values in Constants.java

These are the values inherited from the existing robot code. They must be checked with the tuners before using Pedro drive or paths.

| Setting | Current source value | Status |
|---|---|---|
| Front-left motor | `fl` | Verify Hardware page name |
| Front-right motor | `fr` | Verify Hardware page name |
| Back-left motor | `bl` | Verify Hardware page name |
| Back-right motor | `br` | Verify Hardware page name |
| Drive directions | FL reverse, FR reverse, BL reverse, BR forward | Starting values; run Mecanum Tuner |
| Pinpoint name | `odom` | Verify Hardware page name |
| X pod offset | 0.20 in | Starting value; run Pinpoint Tuner |
| Y pod offset | 3.75 in | Starting value; run Pinpoint Tuner |
| X pod direction | Reversed | Starting value; run Pinpoint Tuner |
| Y pod direction | Reversed | Starting value; run Pinpoint Tuner |
| Pinpoint pod type | Not set in the placeholder | Select the actual installed pod type in Pinpoint Tuner |
| Foresight | `null` | Must be generated for this robot |

The two Pinpoint pods are represented by the X and Y pod settings. The actual pod type, directions, offsets, and resolution must come from this robot's Pinpoint Tuner results. Replace the entire placeholder `localizerConfig` initializer with the generated block, add any imports it requires, and set `LOCALIZER_CONFIG_TUNED = true`. Do not copy sample values from another robot.

The two readiness booleans, `DRIVETRAIN_CONFIG_TUNED` and `LOCALIZER_CONFIG_TUNED`, intentionally start as `false`. After pasting each complete generated config into `Constants.java`, set its corresponding boolean to `true`. `Constants.create()` refuses to build a drive Follower while either flag is false or Foresight is missing. This prevents the path test and Pedro TeleOp from silently using the old unverified starting values.

## 2. Pedro setup and tuning order

Use the same hardware names and configurations for the tuners and the robot Follower. Work in a clear area, secure the robot as appropriate, and have an operator ready to stop a procedure. The tuning procedures can spin wheels, drive the robot, and rotate it.

1. **Mecanum Tuner.** Run the Mecanum procedure to confirm the four motor names and directions. Use its generated output to update the four motor-name and direction constants at the top of `Constants.java`. `drivetrainConfig` is built from those shared values, which are also used by direct drive and the individual motor test. Set `DRIVETRAIN_CONFIG_TUNED = true` only after checking the generated result.
2. **Pinpoint Tuner.** Run the Pinpoint procedure. Select the real pod type and follow its push/rotation instructions. Copy the complete generated `PinpointConfig` into `Constants.localizerConfig`, including pod type (or custom scalar), hardware name, offsets, encoder directions, and units. Set `LOCALIZER_CONFIG_TUNED = true`.
3. **Foresight Tuner.** Run this only after the Mecanum directions and Pinpoint localization are correct. It uses the same drivetrain and localizer configs from `Constants.java`. Copy the complete generated `ForesightConfig` into `Constants.foresightConfig`; do not use a sample robot's values.
4. **Tests procedure.** Run the Pedro Tests procedure to check driving, localization, pose, and then Follower path behavior. Line/curve/hold tests require the Foresight config. The Tests procedure still lists its test types, but selecting a test whose required drivetrain, localizer, or Foresight configuration is not ready will abort with a prerequisite error. Enable the relevant readiness flags only after pasting and checking the corresponding tuner output.

The tuners are registered in `pedro/Tuning.java`. Connect to the Robot Controller for setup and open:

`http://192.168.43.1:10158`

Select the procedure you want to run. Follow the prompts on the page. The official instructions are [Pedro tuning](https://pedropathing.com/docs/pathing/tuning), [Mecanum](https://pedropathing.com/docs/pathing/tuning/drivetrain/mecanum), [Pinpoint](https://pedropathing.com/docs/pathing/tuning/localization/pinpoint), [Foresight](https://pedropathing.com/docs/pathing/tuning/foresight), and [Tests](https://pedropathing.com/docs/pathing/tuning/test).

**Competition-network restriction:** AutoTune is a setup/calibration tool, not a Match-time service. During a Match, FTC 2026-27 rule R704.C requires programming laptops and other devices to be disconnected from the Robot Controller Wi-Fi network. Rule R704.D limits software streaming on that network to allowed robot control/debugging/telemetry through the FTC Driver Station Application and prohibits additional logging/streaming services and continuous video. Run AutoTune during pit/practice setup, then disconnect the laptop before Match play. See the [current BIOBUZZ Competition Manual](https://ftc-resources.firstinspires.org/ftc/game).

The PedroPathing `revhub` and `tuning` libraries use the BSD 3-Clause license; its text is included at `licenses/PEDROPATHING-BSD-3-CLAUSE.txt`. The tuner procedure source files in `pedro/procedures/` are copied from the Pedro-Pathing Quickstart at commit `4f33ef5`. That Quickstart revision carries the FIRST BSD license in its root `LICENSE`, which is already included in this repository as `LICENSE`. The imported tuner procedures have not been rewritten. Team members normally edit `Constants.java`, `Tuning.java`, and the robot's OpModes, not the internals of the calibration procedures.

## 3. How this robot is configured

### Drive and odometry

The robot code expects four mecanum drive motors named `fl`, `fr`, `bl`, and `br`. Their names and directions are defined in `pedro/Constants.java`, so Pedro and the direct-drive code use the same values.

The GoBilda Pinpoint is named `odom` in the current configuration. Two odometry pods provide the movement measurements used by Pedro's `PinpointLocalizer`. The hardware name and all final pod settings belong in `Constants.java`, not `RobotConfig.java`.

The direct-drive mecanum mix in `RobotHardware.java` is:

- Front-left: forward − strafe − turn
- Front-right: forward + strafe + turn
- Back-left: forward + strafe − turn
- Back-right: forward − strafe + turn

### Mechanisms

`RobotConfig.java` holds the configured mechanism names: `intake`, optional `transfer`, `ol`, and `or`. Transfer is deliberately blank by default, so it is disabled and not required. Enter its actual Driver Station name only if that motor is installed and connected.

Mechanism presets are timer-based. Intake runs for `INTAKE_SECONDS`; when transfer is enabled and found, it runs after intake for `TRANSFER_SECONDS`. The paired outtakes run for `SHOOT_SECONDS`. There is no sensor feedback confirming that a game piece was collected, transferred, or shot.

For the full Manual and Pedro TeleOps, Gamepad 2 left bumper starts the intake sequence; right bumper starts both outtakes; holding Back enables direct testing (left stick Y = intake, right stick Y = optional transfer, left trigger = `ol`, right trigger = `or`). Powers and times are starting values in `RobotConfig.java`, not measured performance settings.

### Camera

The AprilTag diagnostic uses the Driver Station Hardware name `Webcam 2`, referenced in code by `RobotConfig.CAM1` (the local alias is `cam1`). The camera code is separate from Pedro drive/path following and does not command the robot.

## 4. How Pedro path following works here

A Pedro `Pose` is `(x, y, heading)`. This project uses inches and radians, and constructs poses with `PoseFactory.radians()`, as Pedro recommends. These coordinates use Pedro's right-handed field system, not the FTC SDK coordinate system: the origin is at the lower-left corner, X increases to the right, Y increases upward, and headings increase counterclockwise (0 faces right, `Math.PI / 2` faces up, `Math.PI` faces left, and `3 * Math.PI / 2` faces down). Each pose is the robot's estimated chassis-center location and heading.

A straight path from one pose to another is created with the Pedro path API:

```java
private final PoseFactory poses = PoseFactory.radians();

Pose startPose = poses.of(startX, startY, startHeading);
Pose endPose = poses.of(endX, endY, endHeading);

Path path = line(startPose, endPose).constant(endPose.heading());
follower.follow(path);
```

The OpMode must update the Follower continuously. In the repeated `loop()`, call:

```java
follower.update();

if (!follower.isBusy()) {
    // The current path is finished. Start the next path or stop.
}
```

Do not put a long blocking loop or `sleep()` around a path. That would prevent the OpMode from updating localization and motor output. Use timer state for pauses between path segments. Give each segment an endpoint heading deliberately. A straight line is easiest to inspect; use a curve only when the route requires one.

Pedro references: [creating a Pose](https://pedropathing.com/docs/pathing/guide/pose-creation), [coordinate system](https://pedropathing.com/docs/pathing/reference/coordinates), [creating a path](https://pedropathing.com/docs/pathing/guide/path-creation), [following paths](https://pedropathing.com/docs/pathing/guide/path-following), [interpolation](https://pedropathing.com/docs/pathing/reference/interpolation), and [follow states](https://pedropathing.com/docs/pathing/guide/follow-state).

### Starting pose

Physically place the robot at the selected field start and heading before starting autonomous. `follower.setPose(startPose)` tells the localizer what the starting pose should be; it does not measure the robot or move it into place. A bad starting estimate shifts the entire route.

The three starting presets (Side Wall, Near Corner, Far Corner) are written directly in `PathTestAuto.java` as six explicit Red/Blue `Pose` assignments created with `PoseFactory.radians()`. Keep them visible there so the team can compare them directly with the field and CAD. Enter all lane, Hive, Flower, and start coordinates in Pedro's coordinate frame; convert coordinates taken from FTC-native diagrams or tools before using them.

### Current path test

`Auto - Path Test (No Mechanisms)` uses `Constants.create()` and drives this sequence:

1. Selected start pose to the selected lane waypoint.
2. Lane waypoint to the selected Hive approach; pause in place instead of shooting the preload.
3. Hive approach to the selected Flower approach; pause instead of running intake.
4. Flower approach back to the lane waypoint, then to the Hive approach; pause instead of shooting.

The route is a test, not a scoring autonomous. It does not run intake, transfer, outtake, or camera code.

INIT controls: X changes alliance; D-pad left/right changes start preset; D-pad up/down changes Flower; A changes inside/outside lane; B changes audience/opposite-audience Hive side.

The current field constants in `RobotConfig.java` deliberately leave robot width, robot length, inside/outside lane waypoint coordinates, and Flower standoff as `Double.NaN`. The path OpMode refuses to start until these values are filled with measurements. The Hive X offset (12 in), Cell separation (18.8 in), Hive approach distance (24 in), and Flower coordinates (48/96 in) are inherited estimates and must be checked against the current official field CAD. Do not treat a waypoint as a full safe corridor. Blue currently mirrors the lane X coordinate across the 144-inch field centerline; verify that this is the correct field transform before driving.

## 5. Other Driver Station OpModes

| Driver Station name | What it does |
|---|---|
| TeleOp - Manual (No Pedro) | Direct mecanum drive and mechanism controls; use this when Pedro calibration is not ready |
| TeleOp - Pedro + Pinpoint | Uses `Constants.create()`, Pedro manual drive, Pinpoint pose telemetry, and mechanism controls |
| TeleOp - No Shooting | Direct drive and intake/optional transfer; outtakes stay at zero |
| Auto - Forward Test | Short timed movement check; not a LEAVE/PARK autonomous |
| Auto - Path Test (No Mechanisms) | Pedro line paths with timed pauses; no mechanism actions |
| Motor - Individual Test | Selects and tests one mapped motor at a time |
| Camera - AprilTag Test | Webcam/AprilTag diagnostic only |

An FTC OpMode uses `init()` for hardware setup, `init_loop()` for setup choices/telemetry, `start()` to begin, repeated `loop()` calls while active, and `stop()` for shutdown. Every active Pedro drive loop must call `follower.update()`.

A successful Android build checks source compilation only. It does not verify the wiring, motor directions, Pinpoint calibration, Foresight calibration, robot dimensions, field geometry, or physical path safety.
