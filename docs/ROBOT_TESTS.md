# BIOBUZZ robot tests

The custom code is kept small and lives in one package.

## OpModes

| Driver Station name | What it tests |
|---|---|
| TeleOp - Pedro + Pinpoint | Pedro manual drive and Pinpoint localization |
| TeleOp - Manual (No Pedro) | Direct mecanum drive and timed mechanism presets |
| TeleOp - Drive Only | Direct drive motors only; no mechanisms or camera |
| Auto - Forward Test | Drives forward at one power for one time |
| Camera - Visibility Test | Separate camera check for AprilTags and game pieces |

## Change these values in RobotConfig.java

- Hardware names and motor directions.
- Intake, transfer, and outtake powers and run times.
- Forward-test power and run time.
- Pinpoint offsets and encoder directions.

The motor directions and Pinpoint offsets are not verified hardware measurements. Confirm them on the assembled robot.

## TeleOp controls

Gamepad 1 drives:
- Left stick: forward and strafe.
- Right stick X: turn.
- Right trigger: reduced drive speed.

Gamepad 2 runs mechanisms:
- Left bumper: run intake, then transfer automatically.
- Right bumper: run both outtake motors for the shooting preset.
- Hold Back: manual control. Left stick Y runs intake, right stick Y runs transfer, and the triggers run the two outtake motors independently.

The mechanism presets are timer-based. There are no sensors confirming that a ball was collected, transferred, or launched. Adjust the powers and times in RobotConfig.java.

## Pedro test

Pedro manual drive uses the documented Pedro 3 pattern: call follower.manual(...) with the three gamepad axes, then call follower.update() every loop. This test is robot-centric so it does not depend on a calibrated field heading.

Path following is not enabled. The Pinpoint offsets and directions still need to be measured and checked on the robot. If Pedro fails to initialize or update, the OpMode reports the error; use the direct manual TeleOp to isolate the four drive motors.

## Camera test

The camera is not used by either TeleOp or by autonomous code. Camera - Visibility Test is only a diagnostic.

It opens the configured webcam, shows AprilTag annotations, and looks for the game-piece colors in the image:
- Yellow Pollen.
- Red and blue Nectar.

The colors and sizes come from BIOBUZZ Manual TU07, Section 9.8, page 72. Open Camera Stream on the Driver Station while the test is running to inspect the live image. Color thresholds may need adjustment for the camera, lighting, and field.

Hive state is a manual match-preparation choice, not a camera decision. A scoring auto should have separate INIT selections for the Cell to target during the preload and the Cell to target for the Flower leg. The driver marks the second choice manually if the preload changed the Hive. The forward test below does not need Hive selections because it does not score.

## Forward test and manual scoring rules

Auto - Forward Test only applies constant forward power for the configured time, then stops. It has no ramping, acceleration profile, path follower, or extra movement steps. It is a drivetrain test, not a complete Leave-and-Park routine.

The scoring references are from BIOBUZZ Competition Manual TU07:
- Section 10.5.4, page 88: LEAVE means the ROBOT is no longer contacting the perimeter wall. PARK means the ROBOT is at least partially in the LOADING ZONE.
- Table 10-2, page 89: LEAVE is worth 3 AUTO points. PARK is worth 5 AUTO points and 5 TELEOP points.
- Section 10.5, page 84: LEAVE and AUTO PARK are assessed at the end of AUTO; TELEOP PARK is assessed at the end of the MATCH.

The forward test does not guarantee LEAVE points and does not attempt to PARK. Actual distances and routes must be tested on the field.
