# Robot tests

These are the OpModes currently in the Driver Station menu.

| OpMode | What it checks | Needs |
|---|---|---|
| TeleOp - Manual (No Pedro) | Mecanum drive and mechanism controls | Four drive motors; installed mechanism motors |
| TeleOp - Pedro + Pinpoint | Manual drive and pose telemetry through Pedro | Tuned Mecanum, Pinpoint, and Foresight settings |
| TeleOp - No Shooting | Drive and intake without outtake power | Four drive motors; intake; transfer only if configured |
| Auto - Forward Test | Brief forward movement | Four drive motors |
| Auto - Path Test (No Mechanisms) | Pedro path following with pauses at test points | Tuned Pedro setup and measured field/robot dimensions |
| Motor - Individual Test | One motor at a time | Any motor being tested |
| Camera - AprilTag Test | Webcam preview and tag detection | Webcam 2 |

## Hardware names

The drive names and directions are in **pedro/Constants.java**:

- Front left: **fl**
- Front right: **fr**
- Back left: **bl**
- Back right: **br**
- Pinpoint: **odom**

Mechanism names are in **RobotConfig.java**:

- Intake: **intake**
- Transfer: blank by default, so it is not looked up
- Left outtake: **ol**
- Right outtake: **or**
- Webcam: **Webcam 2**

The code names and the Driver Station names must match. Check the actual robot configuration rather than assuming that these names are already correct.

## Controls

Gamepad 1 is the driver in the manual TeleOps:

- Left stick: forward, back, and strafe
- Right stick X: turn

In TeleOp - Manual (No Pedro) and TeleOp - Pedro + Pinpoint, Gamepad 2 controls the mechanisms:

- Left bumper: run the intake sequence
- Right bumper: run both outtakes
- Hold Back for direct testing: left stick Y controls intake, right stick Y controls transfer, left trigger controls the left outtake, and right trigger controls the right outtake

TeleOp - No Shooting uses the intake controls only. It never sends nonzero power to either outtake.

## First test on the robot

1. Check the hardware names in the Driver Station.
2. Raise or secure the robot so the wheels and mechanisms cannot hit the floor, field, or anyone nearby.
3. Open Motor - Individual Test. Select each installed motor with D-pad left/right, hold A, and start with a small amount of left-stick movement. Only the selected motor should run.
4. Use Auto - Forward Test for a short movement check. Its starting power and duration are in RobotConfig.java; adjust them only after checking the robot is secure.
5. Use TeleOp - Manual (No Pedro) to check driving and the installed mechanisms.
6. Set up Pedro before using TeleOp - Pedro + Pinpoint or Auto - Path Test.
7. Run the camera test separately.

## Mechanism behavior

The intake, transfer, and shooting presets are timed. The code cannot tell whether a game piece actually moved.

A blank transfer name disables the transfer motor and lets the intake run by itself. If a transfer name is entered but that motor is missing, the combined intake sequence will not start. The shooting preset needs both outtake motors; if either is missing, it will not run.

## Forward test

Auto - Forward Test drives forward at the configured power for the configured time, then stops. It is only a drivetrain check. It does not guarantee that the robot will earn LEAVE points or reach the Loading Zone for PARK.

For the Pedro path test, see [PEDRO_AND_ROBOT_GUIDE.md](PEDRO_AND_ROBOT_GUIDE.md).
