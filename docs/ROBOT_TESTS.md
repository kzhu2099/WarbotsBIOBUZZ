# Robot tests

Use the Driver Station names exactly as shown here.

| OpMode | What it checks | Needs |
|---|---|---|
| **TeleOp - Manual (No Pedro)** | Manual mecanum drive and mechanism controls | Four drive motors; installed mechanisms |
| **TeleOp - Pedro + Pinpoint** | Manual drive and pose telemetry | Tuned Mecanum, Pinpoint, and Foresight |
| **TeleOp - No Shooting** | Drive and intake without outtake power | Four drive motors and intake |
| **Auto - Forward Test** | Short forward movement | Four drive motors |
| **TEST - Pedro Path (No Mechanisms)** | Path following through the selected lane, Hive, Flower, and back | Tuned Pedro setup and measured `FieldConfig.java` values |
| **TEST - Individual Motor** | One motor at a time | Installed motors |
| **Camera - AprilTag Test** | Webcam preview and tag detection | Webcam 2 |

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

Gamepad 2 controls the mechanisms in TeleOp - Manual and TeleOp - Pedro:

- Left bumper: timed intake sequence
- Right bumper: timed outtake sequence
- Hold Back for direct testing: left stick Y controls intake, right stick Y controls transfer, triggers control the outtakes

TeleOp - No Shooting never sends nonzero power to either outtake.

For TEST - Individual Motor:

- D-pad left/right selects the motor.
- The screen shows the physical position and Driver Station hardware name.
- Hold A and move the left stick Y to run that motor.
- Maximum test power is set in `RobotConfig.java`.

## First hardware check

1. Secure the robot on blocks so the wheels cannot touch the floor.
2. Open TEST - Individual Motor.
3. Check that the selected physical motor is the only one that moves. Start with a small stick movement.
4. Check the motor's direction. Correct the direction in the configuration before driving on the floor.
5. Run Auto - Forward Test at its configured low power and short duration.
6. Use TeleOp - Manual (No Pedro) before trying Pedro.

## Path test

The path test has three lane/start selections and four Flower choices. In INIT:

- X changes Red/Blue alliance.
- D-pad left/right changes the start position.
- D-pad up/down changes the Flower.
- A switches Inside/Outside lane.
- B switches Audience-side/Field-side Hive cell.

The telemetry names the selected positions and prints the coordinate of each important pose. Place the robot's center at the displayed start pose and point it at the displayed heading before pressing Play.

The path test sets the follower's starting pose. That is an instruction to the localizer, not a measurement. The test is not ready until robot dimensions and the required field measurements are entered in `FieldConfig.java`.

## Mechanisms

Intake, transfer, and shooting presets are timed. There are no sensors confirming that a game piece moved.

A blank transfer name disables transfer. If transfer is configured but the motor is missing, the intake sequence will not start. Shooting needs both outtake motors. When either is missing, the shooting sequence will not start.
