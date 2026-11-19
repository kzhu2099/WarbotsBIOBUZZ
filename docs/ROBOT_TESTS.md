# Robot tests

Use the Driver Station hardware names shown in this guide.

| OpMode | What it checks | Needs |
|---|---|---|
| **TeleOp - Manual (No Pedro)** | Manual mecanum drive and mechanism controls | Four drive motors; installed mechanisms |
| **TeleOp - Pedro + Pinpoint** | Manual drive and pose telemetry | Tuned Mecanum, Pinpoint, and Foresight |
| **TeleOp - No Shooting** | Drive and intake without outtake power | Four drive motors and intake |
| **Auto - Forward Test** | Short forward movement | Four drive motors |
| **TEST - Pedro Path (Drive Only)** | Lane, Hive, Flower, and return route | Tuned Pedro setup and measured `FieldConfig.java` values |
| **TEST - Individual Motor** | One motor at a time | Installed motors |
| **Camera - AprilTag Test** | Webcam preview and tag detection | Configured webcam |

## Hardware names

Drive names and directions are in `pedro/Constants.java`:

- Front left: `fl`
- Front right: `fr`
- Back left: `bl`
- Back right: `br`
- Pinpoint: `odom`

Mechanism names and directions are in `RobotConfig.java`:

- Intake: `intake`
- Transfer: blank by default, so it is not looked up
- Left outtake: `ol`
- Right outtake: `or`
- Webcam: `Webcam 2`

The Driver Station names must match. Check the actual robot configuration.

## Controls

Gamepad 1 drives in each manual TeleOp:

- Left stick: forward, back, and strafe
- Right stick X: turn

Gamepad 2 controls mechanisms in TeleOp - Manual and TeleOp - Pedro:

- Left bumper: timed intake sequence
- Right bumper: timed outtake sequence
- Hold Back for direct testing: left stick Y controls intake, right stick Y controls transfer, triggers control the outtakes

TeleOp - No Shooting never sends nonzero power to either outtake.

## Individual motor test

- D-pad left/right selects the motor.
- The screen shows its physical position, Driver Station name, and configured direction.
- Hold A and move the left stick Y to run it.
- Test power is limited by `MOTOR_TEST_MAX_POWER` in `RobotConfig.java`.

Raise the robot and keep everyone clear of moving parts. Confirm the selected physical motor is the only motor that moves before increasing power.

## First drive test

1. Secure the robot on blocks.
2. Use TEST - Individual Motor to check each wheel and mechanism.
3. Correct motor directions before putting the robot on the floor.
4. Use Auto - Forward Test for a short movement check.
5. Use TeleOp - Manual (No Pedro) to check normal driving.
6. Verify the saved Mecanum, Pinpoint, and Foresight settings on the assembled robot before using TeleOp - Pedro + Pinpoint or TEST - Pedro Path (Drive Only).

## Path test selections

In INIT:

- X switches Red/Blue.
- D-pad left/right selects one of six explicit start rows (row 1 is nearest Audience).
- D-pad up/down selects Red wall, Scoring wall, Blue wall, or Audience wall Flower.
- A switches OUTSIDE/INSIDE lane.
- B switches Audience/Scoring Hive bypass.

The INIT screen displays the exact robot-center start coordinate, heading, bumper-to-wall instruction, physical Flower center, separate robot stop, and the complete ordered route. See [FIELD_PATHS.md](FIELD_PATHS.md) for the six row coordinates and coordinate convention.

For Red, put the LEFT bumper flush against the Red/left inside wall; the center is X=9.000 in. For Blue, put the RIGHT bumper flush against the Blue/right inside wall; the center is X=132.348 in in the CAD-derived 141.348 in inside-wall span. Align the robot center to the selected row Y. The front direction is shown on INIT. The path test sets the follower's starting estimate; it does not move or locate the robot.

## Mechanism behavior

Intake, transfer, and shooting presets are timed. There are no sensors confirming that a game piece moved.

A blank transfer name disables transfer. If transfer is configured but the motor is missing, the intake sequence will not start. Shooting needs both outtake motors. If either is missing, the shooting sequence will not start.
