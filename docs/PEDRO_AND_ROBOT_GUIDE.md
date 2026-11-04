# How this robot and PedroPathing are set up

This guide describes the code in this repository. Hardware names and settings are the values the software expects; confirm them against the assembled robot and Driver Station before powering anything.

The project uses **PedroPathing revhub 3.0.1**. Keep the project's Pedro configuration in the `pedro` package instead of constructing a different Follower inside each OpMode.

## 1. How the robot code fits together

| File | Responsibility |
|---|---|
| `RobotConfig.java` | Hardware-map names, motor directions, Pinpoint name/offsets/directions, mechanism power/timing, and path-test field values |
| `RobotHardware.java` | Direct four-motor mecanum drive, motor setup, and timed/manual mechanism controls |
| `pedro/Constants.java` | Pedro Mecanum config, Pinpoint config, Foresight config, and the two shared Follower constructors |
| `pedro/Tuning.java` | Registers the Mecanum, Pinpoint, and Foresight AutoTune procedures |
| `pedro/procedures/*.java` | Pedro Quickstart tuning procedures used by AutoTune |
| `PathTestAuto.java` | Path-only autonomous test; no mechanism or camera control |
| `TeleOpPedro.java` | Manual drive through Pedro and Pinpoint, plus the separate mechanism controls |
| `TeleOpManual.java` | Direct manual drive without Pedro |
| `TeleOpNoShoot.java` | Direct drive and intake test; outtakes are held at zero |
| `IndividualMotorTest.java` | Selects and powers one mapped motor at a time |
| `AutoMovementTest.java` | Short, timed drivetrain movement check |
| `CameraTest.java` | AprilTag-only camera diagnostic |

The long file in `pedro/procedures/ForesightTuner.java` is Pedro Quickstart's calibration routine, not robot-control logic. Team members normally edit `RobotConfig.java` and `pedro/Constants.java`; they should not need to work inside the tuning procedure source.\n\nAn FTC OpMode follows the same basic lifecycle: `init()` maps/configures hardware, `init_loop()` displays setup and accepts selections, `start()` begins the selected test, `loop()` runs repeatedly while the test is active, and `stop()` commands outputs off.

## 2. How this robot is configured

### Drive and odometry

The drivetrain is a four-motor mecanum setup. The expected Driver Station names are `fl`, `fr`, `bl`, and `br`. Motor directions are in `RobotConfig.java`. The direct-drive code uses the standard mecanum mix:

- Front-left: forward − strafe − turn
- Front-right: forward + strafe + turn
- Back-left: forward + strafe − turn
- Back-right: forward − strafe + turn

Pedro uses the same four motors through its `Mecanum` drivetrain class.

The configured odometry computer is the GoBilda Pinpoint named `odom`. Its two odometry pods supply the robot's measured movement to Pedro's `PinpointLocalizer`. The current X/Y offsets and encoder directions are starting values in `RobotConfig.java`; they must be checked and measured on the assembled robot. Incorrect pod direction or offset produces an incorrect pose even if the drive motors move correctly.

### Mechanisms and camera

The configured mechanism names are `intake`, optional `transfer`, `ol`, and `or`. The transfer name is deliberately blank by default, so it is not looked up or required. Enter its real Driver Station name only after confirming that motor is installed and connected.

Mechanism actions are timer-based. The code does not confirm that a game piece was acquired, transferred, or shot. The code does not configure servo behavior; this is not a claim about what hardware is physically installed.

The AprilTag test calls its webcam `cam1` in code, mapped to the Driver Station name `Webcam 2`. It is separate from drive and mechanism control.

## 3. How PedroPathing works in this project

Pedro connects three things:

1. **Mecanum drivetrain:** turns the Follower's requested movement into power for the four drive motors.
2. **Pinpoint localizer:** estimates the robot's current field pose (X, Y, heading) from the odometry pods.
3. **Foresight algorithm:** controls how the robot follows a path and brakes near its endpoint.

Those three components are assembled in one place: `pedro/Constants.java`.

- `Constants.create(hardwareMap)` creates a path-following Follower and requires a tuned Foresight configuration.
- `Constants.createManual(hardwareMap)` creates a manual-drive Follower without Foresight. This is what `TeleOpPedro` uses.

Do not create a separate Mecanum or Pinpoint configuration in a new OpMode. Change the shared settings, then use the appropriate Constants method.

### Tuning order

Do the tuning on the real robot, not with guessed values. Secure the robot and use a clear test area; the tuning procedures can move and rotate it.

1. **Mecanum Tuner:** check the drive motor names and directions. Update the four direction values in `RobotConfig.java`.
2. **Pinpoint Tuner:** identify the pod type, directions, and offsets. Copy the measured values into the Pinpoint settings in `RobotConfig.java`.
3. **Foresight Tuner:** run only after the drivetrain directions and Pinpoint localization are correct. It needs the same shared drivetrain and localizer used by the robot.
4. Open the AutoTune Java tab and copy the generated `ForesightConfig` into `Constants.foresightConfig` in `pedro/Constants.java`.

The registered procedures are in `pedro/Tuning.java`. With the Robot Controller connected to the tuning network, open:

`http://192.168.43.1:10158`

Choose Mecanum, Pinpoint, and then Foresight in that order. Follow each procedure's on-screen instructions. The official instructions are in the [PedroPathing tuning guide](https://pedropathing.com/docs/pathing/tuning) and [Foresight guide](https://pedropathing.com/docs/pathing/tuning/foresight).

Until the measured Foresight configuration is pasted into `Constants.java`, the path test refuses to start. A null algorithm is acceptable for manual Pedro driving; it is not a tuned path follower.

## 4. How to write and run a path

A Pedro `Pose` is `(x, y, heading)`: X and Y are field coordinates in inches in this project, and heading is in radians. Use `Math.toRadians(degrees)` when entering an angle in degrees.

A straight path from one pose to another is built with:

```java
Path path = line(startPose, endPose).constant(endPose.heading());
follower.follow(path);
```

The path does not run to completion by itself. In the OpMode's repeated `loop()`, call:

```java
follower.update();

if (!follower.isBusy()) {
    // This path has finished; start the next path or stop.
}
```

Call `update()` once on every active loop. Do not put a long blocking loop or `sleep()` around path following; that prevents the Follower from updating localization and drive output. Use a timer state for pauses between path segments.

For a sequence, write the target poses in the order the robot should visit them, call `follow()` for the current segment, and start the next segment after `isBusy()` becomes false. Give each segment an endpoint heading deliberately. Use a curve only when the robot needs a curved route; a straight line is easier to inspect and debug.

### Starting pose matters

Before autonomous starts, physically place the robot at the selected field position and angle. The code calls `follower.setPose(startPose)` to tell the localizer what pose to use. **That call does not measure the robot or move it into position.** A wrong starting estimate shifts every path.

The three starting choices in `PathTestAuto.java` are explicit pose assignments for Side Wall, Near Corner, and Far Corner, for both alliances. They are intentionally visible there instead of being hidden in a pose-generation function.

### What the current path test does

`Auto - Path Test (No Mechanisms)` uses the shared `Constants.create()` Follower and the Pinpoint localizer. It follows this visible sequence:

1. Start pose to the selected Inside or Outside lane waypoint.
2. Lane waypoint to the selected Hive/Cell approach, then pause instead of shooting the preload.
3. Hive approach to the selected Flower approach, then pause instead of intaking.
4. Flower approach back through the same lane waypoint to the Hive approach, then pause instead of shooting again.

INIT controls: X selects alliance; D-pad left/right selects start; D-pad up/down selects Flower; A switches lane; B switches the selected Hive side.

The lane coordinates are single waypoint values, not a fully mapped corridor. Set `INSIDE_LANE_X/Y` and `OUTSIDE_LANE_X/Y` in `RobotConfig.java` from field CAD and verify the paths physically. Blue mirrors the lane X coordinate across the field centerline. The Flower standoff, robot dimensions, Hive position, and Flower positions also need measurement/field-CAD verification. The current unmeasured values are deliberately left unset where the test requires them; do not replace them with arbitrary guesses.

## 5. How the other tests work

- **TeleOp - Pedro + Pinpoint:** reads the gamepad, calls `follower.manual(...)`, then calls `follower.update()`. Pedro is being used for manual drive and pose telemetry; this mode does not follow paths.
- **TeleOp - Manual (No Pedro):** reads the driver sticks and directly sets the four mecanum motor powers through `RobotHardware`.
- **TeleOp - No Shooting:** direct drive and intake control; it never sends nonzero power to `ol` or `or`.
- **Motor - Individual Test:** D-pad left/right selects a motor. Hold A and move the left stick Y to power that motor. All other mapped motors are commanded to zero.
- **Auto - Forward Test:** a short constant-power drive check, not a scoring autonomous.
- **Camera - AprilTag Test:** opens `Webcam 2` as `cam1` and reports AprilTag detections. It does not make robot decisions.

On Gamepad 2 in the full manual/Pedro TeleOps, left bumper starts the timed intake sequence, right bumper starts the timed paired outtakes, and holding Back enables the direct mechanism test: left stick Y for intake, right stick Y for optional transfer, left trigger for `ol`, and right trigger for `or`.

## 6. Test sequence and limits

1. Confirm each Hardware page name against `RobotConfig.java`.
2. Raise/secure the robot and use Individual Motor Test at low power to check each installed motor and direction.
3. Verify mecanum forward, strafe, and turn with the robot raised before driving on the floor.
4. Run Mecanum and Pinpoint tuning; verify pose direction and offsets.
5. Run Foresight tuning and paste its output into `pedro/Constants.java`.
6. Measure robot dimensions and field waypoints. Check start pose and route coordinates against the actual field.
7. Run Path Test at low speed in a clear area with an operator ready to stop the OpMode.

A successful Android build only checks compilation. It does not verify wiring, motor direction, Pinpoint calibration, Foresight values, field geometry, or actual path accuracy.
