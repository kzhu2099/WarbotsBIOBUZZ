# BIOBUZZ robot code: current tests

This is a small bring-up codebase for the current testing stage. Each Driver Station test has its own OpMode file. For an explanation of how the robot software works and how to use PedroPathing, see [PEDRO_AND_ROBOT_GUIDE.md](PEDRO_AND_ROBOT_GUIDE.md).

## Start here

- `RobotConfig.java`: hardware names, motor directions, powers/times, measured robot dimensions, lane waypoints, and Pinpoint values.
- `TeleOpManual.java`: simplest drive-and-mechanism control.
- `RobotHardware.java`: drive motor setup, mecanum powers, and timed/manual mechanism controls.
- `TeleOpPedro.java`: manual Pedro drive with the real Pinpoint localizer.
- `PathTestAuto.java`: path-only test. It does not use intake, transfer, outtake, or a camera.
- `IndividualMotorTest.java`: one motor at a time.
- `CameraTest.java`: AprilTag-only camera check.

## Hardware names

Names must match the Driver Station Hardware page.

| Device | Name in code | Notes |
|---|---|---|
| Drive motors | `fl`, `fr`, `bl`, `br` | All four are required to drive |
| Intake motor | `intake` | Used by mechanism TeleOps and individual motor test |
| Transfer motor | Blank in `RobotConfig` | Optional; disabled until configured |
| Left outtake | `ol` | Used by shooting controls and individual motor test |
| Right outtake | `or` | Used by shooting controls and individual motor test |
| GoBilda Pinpoint | `odom` | Used by Pedro TeleOp and Path Test |
| Webcam | `cam1` in code | Driver Station device name is `Webcam 2` |

Do not treat this list as a complete physical inventory. Check the assembled robot and the Driver Station configuration.

## OpModes

| Driver Station name | What it tests |
|---|---|
| TeleOp - Manual (No Pedro) | Direct mecanum drive and mechanism controls |
| TeleOp - Pedro + Pinpoint | Pedro manual drive using Pinpoint odometry and mechanism controls |
| TeleOp - No Shooting | Direct drive and intake; outtakes stay at zero |
| Auto - Forward Test | Short, constant-power forward movement |
| Auto - Path Test (No Mechanisms) | Pedro path following only; timed pauses replace mechanism actions |
| Motor - Individual Test | One selected motor at a time |
| Camera - AprilTag Test | Webcam 2 live view and AprilTag detections only |

## Path-only autonomous test

`PathTestAuto.java` uses the Pedro Follower with the Pinpoint localizer. It follows a fixed list of waypoints and never initializes the mechanism controller or camera.

The route is intentionally visible in the file:

1. Start pose to the selected lane point.
2. Lane point to the selected Hive approach, then pause instead of shooting the preload.
3. Hive approach to the selected Flower approach, then pause instead of intaking.
4. Return through the selected lane point to the same Hive approach, then pause instead of shooting again.

This is a path test, not a scoring autonomous. It does not run the intake, transfer, or outtake, and does not verify that a game piece was collected or scored.

### INIT selections

- **X:** Red/Blue alliance.
- **D-pad left/right:** start preset: Side Wall, Near Corner, Far Corner.
- **D-pad up/down:** Flower: Left Wall, Top Wall, Right Wall, Bottom Wall.
- **A:** lane: Inside Hive or Outside Hive.
- **B:** audience-side or opposite-audience-side Cell.

The start poses are written directly in `PathTestAuto.java` as six explicit `new Pose(x, y, heading)` assignments. They are not hidden inside a start-pose generator. Coordinates are chassis-center poses.

### Values required before path testing

The path OpMode intentionally refuses to start while any required value is missing:

- `RobotConfig.FORESIGHT_CONFIG`: generate this using Pedro's Foresight tuner on this robot, then paste the generated configuration here. A null algorithm is fine for manual drive but is not used for path following.
- `ROBOT_WIDTH_INCHES` and `ROBOT_LENGTH_INCHES`: measure the assembled robot, including bumpers.
- `INSIDE_LANE_X/Y` and `OUTSIDE_LANE_X/Y`: set the actual lane waypoint coordinates from field CAD / measured field layout. These are RED-side coordinates; the path test mirrors X for Blue.
- `FLOWER_STANDOFF_INCHES`: measure the correct robot-center distance for the intake approach. It is not assumed from an 18-inch chassis or an unverified intake extension.

The existing Hive and Flower reference coordinates, Hive X offset, Cell separation, and Hive approach distance in `RobotConfig.java` are carried over from earlier project code. They are marked as estimates and must be verified against the official field CAD. The earlier code only displayed the Inside/Outside lane selection; it did not contain executable route geometry for both lanes. The new test requires the team to enter those two lane waypoints explicitly rather than inventing them.

The three starting poses are team presets from the earlier code. Verify each on the assembled field before driving. Setting a starting pose changes Pedro's estimate; it does not detect or move the robot.

## Individual motor test

`IndividualMotorTest.java` only maps the listed motors and tests the selected one:

- D-pad left/right: choose the next motor.
- Hold **A** and move the left stick Y: set power to the selected motor.
- All other mapped motors are commanded to zero each loop.
- A blank transfer name is shown as disabled. A configured but missing motor is shown as not found.

Raise and secure the robot before running it. Check each motor direction at low power first. This OpMode does not initialize Pedro, Pinpoint, mechanisms, or a camera.

## Other controls

### Gamepad 1 (manual TeleOps)

- Left stick Y: forward/back.
- Left stick X: strafe.
- Right stick X: turn.
- Inputs smaller than 0.05 are ignored.

### Gamepad 2 (Manual and Pedro TeleOps)

- Left bumper: run intake for `INTAKE_SECONDS`. If transfer is enabled and found, it runs after intake.
- Right bumper: run both outtakes for `SHOOT_SECONDS`.
- Hold Back: manual motor test. Left stick Y runs intake, right stick Y runs transfer, left trigger runs `ol`, and right trigger runs `or`.

### No-shoot TeleOp

- Left bumper: run intake, then optional transfer.
- Hold Back: manually test intake and optional transfer.
- Outtakes are initialized and held at zero. This OpMode never sends them nonzero power.

## Pedro and Pinpoint

The Pedro TeleOp uses the GoBilda Pinpoint localizer and the two odometry pods. The device name, pod directions, and offsets are in `RobotConfig.java`.

The current offsets and directions are carried-over starting values, not verified measurements. Check the pod mounting direction and measure the offsets from the robot's tracking center before trusting pose telemetry.

## Camera

The camera's Driver Station hardware name is `Webcam 2`; the code refers to it as `cam1` through `RobotConfig.CAM1`. Camera - AprilTag Test only initializes an AprilTag processor and shows the live view and detected tag/cluster names. It does not run color detection, make game decisions, or control motors.

## Mechanism limits

- Intake and transfer use timers. No sensor confirms that a game piece moved.
- Shooting runs `ol` and `or` together for the configured time.
- The shooting preset requires both outtake motors to be present.
- Powers and times are starting values. Test one mechanism at a time with the robot secured.
- The transfer name is blank by default; no transfer motor is looked up or required until configured.

## Forward test

Auto - Forward Test uses constant power for one configured duration, then stops. It is only a movement check. It does not guarantee LEAVE and does not attempt PARK.

BIOBUZZ Manual TU07:
- §10.5.4, page 88: LEAVE means the robot is no longer contacting the perimeter wall. PARK means at least part of the robot is in the Loading Zone.
- Table 10-2, page 89: LEAVE is worth 3 AUTO points. PARK is worth 5 AUTO points and 5 TELEOP points.
- §10.5, page 84: LEAVE and AUTO PARK are assessed at the end of AUTO; TELEOP PARK is assessed at the end of the MATCH.

## First hardware test

1. Check every configured name against the Driver Station Hardware page.
2. Raise and secure the robot so no wheel or mechanism can contact the floor or a person.
3. Run Motor - Individual Test at low power and verify each motor direction.
4. Run Auto - Forward Test to verify basic drivetrain movement.
5. Use TeleOp - Manual (No Pedro) to test the drive and installed mechanisms.
6. Verify Pinpoint direction and measured offsets.
7. Run Auto - Path Test only after Foresight, robot dimensions, lane waypoints, and Flower standoff are set and checked against the field.
8. Run Camera - AprilTag Test separately.
