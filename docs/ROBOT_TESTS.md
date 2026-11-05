# Robot tests

Use the Driver Station hardware names shown in this guide.

| OpMode | What it checks | Needs |
|---|---|---|
| **TeleOp - Manual (No Pedro)** | Manual mecanum drive and mechanism controls | Four drive motors; installed mechanisms |
| **TeleOp - Pedro + Pinpoint** | Manual drive and pose telemetry | Tuned Mecanum, Pinpoint, and Foresight |
| **TeleOp - No Shooting** | Drive and intake without outtake power | Four drive motors and intake |
| **Auto - Forward Test** | Short forward movement | Four drive motors |
| **TEST - Pedro Path (No Mechanisms)** | Lane, Hive, Flower, and return route | Tuned Pedro setup and measured `FieldConfig.java` values |
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
6. Tune Pedro before using TeleOp - Pedro + Pinpoint or TEST - Pedro Path.

## Path test selections

In INIT:

- X changes Red/Blue.
- D-pad left/right changes start position.
- D-pad up/down changes Flower.
- A switches Inside/Outside lane.
- B switches Audience-side/Field-side Hive cell.

The INIT screen displays each waypoint and its robot-center coordinate. The coordinate convention and route are in [FIELD_PATHS.md](FIELD_PATHS.md).

The path test sets the follower's starting pose. That sets the localizer's estimate; it does not locate or move the robot. Put the robot's center at the displayed start pose and heading before pressing Play.

## Mechanism behavior

Intake, transfer, and shooting presets are timed. There are no sensors confirming that a game piece moved.

A blank transfer name disables transfer. If transfer is configured but the motor is missing, the intake sequence will not start. Shooting needs both outtake motors. If either is missing, the shooting sequence will not start.
