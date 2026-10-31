# BIOBUZZ robot tests

These are the only team OpModes for the current testing stage:

| Driver Station name | Purpose |
|---|---|
| TeleOp - Pedro + Pinpoint | Test the drivetrain through Pedro and the Pinpoint |
| TeleOp - Manual (No Pedro) | Test direct drive, intake, transfer, and shooting |
| TeleOp - No Shooting | Test direct drive, intake, and transfer; outtake is disabled |
| Auto - Forward Test | Drive forward at one power for one time, then stop |
| Camera - Visibility Test | Check the webcam, AprilTags, and game-piece colors |

## Change hardware and test values

Edit `RobotConfig.java` only:

- Hardware names.
- Motor directions.
- Forward-test power and time.
- Intake, transfer, and outtake powers and times.
- Pinpoint offsets and directions.

Motor directions and Pinpoint offsets are inherited starting values, not verified measurements. Check them on the physical robot.

## Driver controls

### Gamepad 1 (all TeleOps)

- Left stick: forward and strafe.
- Right stick X: turn.

There is no field-centric toggle, precision mode, route selection, or extra drive control.

### Gamepad 2 (TeleOp - Manual)

- Left bumper: run intake, then transfer.
- Right bumper: run both outtake motors for the shooting preset.
- Hold Back: manual mechanism test. Left stick Y runs intake, right stick Y runs transfer, left trigger runs left outtake, and right trigger runs right outtake.

### Gamepad 2 (TeleOp - No Shooting)

- Left bumper: run intake, then transfer.
- Hold Back: manually test intake and transfer.
- Shooting and outtake motors are disabled in this OpMode.

The presets are timer-based. There are no sensors confirming a game piece was collected, transferred, or launched. Adjust the powers and times in `RobotConfig.java`.

## Pedro test

The Pedro TeleOp calls `follower.manual(forward, strafe, turn)` and `follower.update()` each loop. It is robot-centric only.

Path following, Foresight, field-centric control, and auto route planning are deliberately not included. Pinpoint offsets and directions must be physically checked before relying on the pose.

If Pedro fails to initialize or update, the OpMode reports the error. Use TeleOp - Manual (No Pedro) to test the same drive motors without Pedro.

## Camera test

The camera is not used by any drivetrain, mechanism, or autonomous control.

Camera - Visibility Test is only a diagnostic. Open Camera Stream on the Driver Station:
- AprilTag Processor displays visible tag IDs and BIOBUZZ cluster names.
- Yellow blob detection is a visual check for Pollen.
- Red and blue blob detection is a visual check for Nectar.
- Contour overlays show which colored objects were detected.

BIOBUZZ Manual TU07 §9.8, page 72, identifies Pollen as yellow (about 2.8 inches) and Nectar as red or blue (about 3.6 inches). The color detector is a visibility aid, not a game-piece classifier. Lighting, exposure, and field conditions may change the result.

No camera reading changes the expected Hive tip or chooses a Cell. Any future scoring auto should use a simple manual INIT choice for the Cell targeted before and after a preload.

## Forward test and scoring references

Auto - Forward Test applies constant forward power for the configured time and then stops. It has no ramping, acceleration profile, path follower, distance estimate, or extra movement steps.

BIOBUZZ Manual TU07:
- §10.5.4, page 88: LEAVE requires the robot to stop contacting the perimeter wall. PARK requires at least part of the robot to be in the Loading Zone.
- Table 10-2, page 89: LEAVE is worth 3 AUTO points. PARK is worth 5 AUTO points and 5 TELEOP points.
- §10.5, page 84: LEAVE and AUTO PARK are assessed at the end of AUTO; TELEOP PARK is assessed at the end of the MATCH.

The forward test does not guarantee LEAVE points and does not attempt PARK. A real Leave-and-Park routine needs field measurements and physical testing.

## First hardware test

1. Secure the robot on a stand so the wheels cannot touch the floor.
2. Check the configured hardware names and wheel directions.
3. Run Auto - Forward Test at the low configured power and time. Confirm all four wheels turn the intended direction; stop immediately if they do not.
4. Test TeleOp - Manual with the wheels still raised.
5. Test each mechanism separately while holding Back. Keep hands and loose objects clear of moving parts.
6. Run the automatic intake/transfer and shooting presets only after confirming each motor's direction.
7. Test Pedro + Pinpoint last, after confirming the Pinpoint name, offsets, and pod directions.
8. Run Camera - Visibility Test separately and inspect the Driver Station camera stream.
