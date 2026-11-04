# BIOBUZZ robot tests

This file describes the current test OpModes and the hardware checks needed before using them. For the complete code layout and PedroPathing configuration workflow, see [PEDRO_AND_ROBOT_GUIDE.md](PEDRO_AND_ROBOT_GUIDE.md).

## Configuration locations

| File | Settings |
|---|---|
| `pedro/Constants.java` | Pedro drive motor names/directions, Pinpoint hardware name/pod type/offsets/directions/units, Foresight config, and calibration-ready flags |
| `RobotConfig.java` | Intake/transfer/outtake names and directions, mechanism powers/times, joystick deadband, robot dimensions, field coordinates, and path-test timing |
| `pedro/Tuning.java` | Mecanum, Pinpoint, Foresight, and Tests AutoTune registrations |

The current Pedro motor names are `fl`, `fr`, `bl`, and `br`; the Pinpoint name is `odom`. They are starting values and must match the Driver Station Hardware page. Pedro calibration values must be generated for this robot and saved in `pedro/Constants.java`.

## Hardware names in RobotConfig.java

| Device | Code name | Notes |
|---|---|---|
| Intake motor | `intake` | Mechanism TeleOps |
| Transfer motor | Blank by default | Optional; no lookup until configured |
| Left outtake | `ol` | Paired shooting control |
| Right outtake | `or` | Paired shooting control |
| Webcam | `cam1` in code | Driver Station name is `Webcam 2` |

This table is not a complete physical inventory. Check the assembled robot and Driver Station configuration.

## Driver Station OpModes

| Driver Station name | Function |
|---|---|
| TeleOp - Manual (No Pedro) | Direct drive and mechanism controls |
| TeleOp - Pedro + Pinpoint | Pedro manual drive, Pinpoint pose telemetry, and mechanism controls; requires tuned Pedro configs |
| TeleOp - No Shooting | Direct drive and intake/optional transfer; outtakes remain at zero |
| Auto - Forward Test | Short, constant-power drivetrain check |
| Auto - Path Test (No Mechanisms) | Pedro paths only; pauses replace mechanism actions |
| Motor - Individual Test | Selects and powers one mapped motor |
| Camera - AprilTag Test | Camera and AprilTag diagnostics only |

## Pedro tuning and path readiness

Run Pedro setup in this order:

1. Mecanum Tuner: verify the four motor names and directions. Update the four shared motor-name/direction values at the top of `Constants.java`; `drivetrainConfig` is built from those values and the direct-drive tests use them too. Then set `DRIVETRAIN_CONFIG_TUNED = true`.
2. Pinpoint Tuner: select the actual odometry pod type and follow the direction/offset procedure. Replace the whole placeholder `localizerConfig` initializer with the complete generated config, including pod type and units; then set `LOCALIZER_CONFIG_TUNED = true`.
3. Foresight Tuner: run only after the drivetrain and localizer are correct. Paste the generated result into `Constants.foresightConfig`.
4. Tests procedure: check the localizer, drivetrain, and tuned Follower.

Pedro's tuning page is `http://192.168.43.1:10158` when connected to the Robot Controller during setup. Read the [Pedro tuning guide](https://pedropathing.com/docs/pathing/tuning).

AutoTune is a setup/calibration tool, not a Match-time service. During a Match, FTC 2026-27 rule R704.C requires programming laptops and other devices to be disconnected from the Robot Controller Wi-Fi network. Rule R704.D limits software streaming on that network to allowed robot control/debugging/telemetry through the FTC Driver Station Application and prohibits additional logging/streaming services and continuous video. Run AutoTune during pit/practice setup, then disconnect the laptop before Match play. See the [current BIOBUZZ Competition Manual](https://ftc-resources.firstinspires.org/ftc/game).

`Constants.create()` will not create the robot Follower until both calibration flags are true and a Foresight config has been pasted. TeleOp - Manual (No Pedro) remains available for basic manual driving before Pedro setup is complete.

## Path-only autonomous test

`PathTestAuto.java` follows these waypoints:

1. Start pose to the selected lane point.
2. Lane point to the selected Hive approach, then pause instead of shooting.
3. Hive approach to the selected Flower approach, then pause instead of intaking.
4. Return via the lane point to the Hive approach, then pause instead of shooting again.

It does not initialize or operate intake, transfer, outtake, or camera hardware.

### INIT controls

- **X:** Red/Blue alliance.
- **D-pad left/right:** Side Wall, Near Corner, Far Corner start preset.
- **D-pad up/down:** Left Wall, Top Wall, Right Wall, Bottom Wall Flower.
- **A:** Inside Hive / Outside Hive lane.
- **B:** audience-side / opposite-audience-side Hive side.

### Values required before the path can start

The OpMode checks that these values are set:

- `Constants.DRIVETRAIN_CONFIG_TUNED`: Mecanum configuration generated for this robot.
- `Constants.LOCALIZER_CONFIG_TUNED`: complete Pinpoint configuration generated for these two pods.
- `Constants.foresightConfig`: Foresight output generated for this robot.
- `RobotConfig.ROBOT_WIDTH_INCHES` and `RobotConfig.ROBOT_LENGTH_INCHES`: measure the assembled robot, including bumpers.
- `RobotConfig.INSIDE_LANE_X/Y` and `OUTSIDE_LANE_X/Y`: lane waypoint coordinates measured from field CAD.
- `RobotConfig.FLOWER_STANDOFF_INCHES`: measured robot-center Flower approach standoff.

The existing Hive X offset, Cell separation, Hive approach distance, and Flower coordinates are inherited estimates. Verify all of them against official field CAD before a powered test. The three start presets also need physical verification. All values in PathTestAuto use Pedro's coordinate frame (origin lower-left, X right, Y up, counterclockwise headings), not the FTC SDK coordinate frame; convert CAD/tool values before entering them. Setting a software starting pose does not detect or reposition the robot.

## Individual motor test

`IndividualMotorTest.java` is separate from Pedro and Pinpoint:

- D-pad left/right selects the motor.
- Hold **A** and move the left stick Y to power the selected motor.
- The test commands every other mapped motor to zero each loop.
- A blank transfer name is shown as disabled; a configured but missing motor is shown as not found.

Raise and secure the robot. Start with low power and verify the motor direction before increasing power.

## Manual TeleOp controls

### Gamepad 1

- Left stick Y: forward/back.
- Left stick X: strafe.
- Right stick X: turn.
- Inputs smaller than 0.05 are ignored.

### Gamepad 2 (Manual and Pedro TeleOps)

- Left bumper: timed intake sequence. If transfer is configured and found, transfer runs after intake.
- Right bumper: run both outtakes for `SHOOT_SECONDS`.
- Hold Back: direct mechanism test. Left stick Y runs intake, right stick Y runs optional transfer, left trigger runs `ol`, and right trigger runs `or`.

### No-shoot TeleOp

Left bumper runs intake and optional transfer. Holding Back enables direct intake/transfer testing. This OpMode never sends nonzero power to `ol` or `or`.

## Mechanism limits

- Intake and transfer use timers; no sensor confirms a game piece moved.
- The shooting preset requires both outtake motors to be present and runs them together.
- Powers and durations in `RobotConfig.java` are starting values. Test each installed mechanism separately with the robot secured.
- A blank transfer name disables transfer; no transfer motor is looked up or required.

## Forward movement test

Auto - Forward Test uses constant power for one configured duration, then stops. It is a basic movement check. It does not guarantee LEAVE and does not attempt PARK.

BIOBUZZ Manual TU07:
- §10.5.4, page 88: LEAVE means the robot is no longer contacting the perimeter wall. PARK means at least part of the robot is in the Loading Zone.
- Table 10-2, page 89: LEAVE is worth 3 AUTO points. PARK is worth 5 AUTO points and 5 TELEOP points.
- §10.5, page 84: LEAVE and AUTO PARK are assessed at the end of AUTO; TELEOP PARK is assessed at the end of the MATCH.

## First hardware test sequence

1. Confirm every configured hardware name against the Driver Station Hardware page.
2. Secure/raise the robot so wheels and mechanisms cannot contact the floor, field, or a person.
3. Use Individual Motor Test at low power to check each installed motor and direction.
4. Use Auto - Forward Test for a brief drivetrain movement check.
5. Use TeleOp - Manual (No Pedro) to test drive and installed mechanisms.
6. Complete Mecanum and Pinpoint tuning, then Foresight tuning and the Tests procedure.
7. Fill and verify robot dimensions and field waypoints before Path Test.
8. Run the AprilTag camera test separately.
