# BIOBUZZ robot code: current hardware tests

This is a small bring-up codebase for the current month. It is not a scoring-autonomous framework.

## Hardware inventory

The code uses the following configured devices:

| Device | Config name | Count | Used by |
|---|---|---:|---|
| Drive motor | `fl`, `fr`, `bl`, `br` | 4 | Drive TeleOps and forward auto |
| Intake motor | `intake` | 1 | Mechanism TeleOps |
| Transfer motor | `transfer` | 1 | Mechanism TeleOps |
| Outtake motors | `outtakeLeft`, `outtakeRight` | 2 | Manual/Pedro TeleOps; held at zero in No Shooting |
| GoBilda Pinpoint | Not mapped | 0 | Not used |
| Webcam | `Webcam 2` | 1 | Camera test only |
| Servo | None configured | **0** | Not used |

That is **8 DC motors, 0 servo outputs, 0 odometry devices, and 1 webcam** used by this code. The names are inherited from the team's existing source code; the Control Hub Hardware configuration is not stored in this repository. Compare this table with the actual Driver Station Hardware page before running.

No OpMode creates or commands a Servo. A servo on the physical robot, if present, is unused by this code.

## Which devices each OpMode touches

| Driver Station OpMode | Motor use | Other hardware |
|---|---|---|
| TeleOp - PedroPathing | All 8 motors | None |
| TeleOp - Manual (No Pedro) | All 8 motors | None |
| TeleOp - No Shooting | All 8 are initialized and zeroed; only 6 can run | Outtakes held at zero |
| Auto - Forward Test | 4 drive motors | None |
| Camera - Visibility Test | 0 motors | Webcam |

The camera is not opened by any drive or mechanism OpMode. No OpMode opens the Pinpoint. A missing camera therefore cannot break a drivetrain test.

## OpModes and exactly what they do

### TeleOp - PedroPathing

Uses Pedro's Mecanum drivetrain and the four mechanism motors. It does not initialize a Pinpoint or camera. Pedro requires a Localizer object even for manual driving, so this test supplies a no-op localizer. That localizer contains no real position data.

Driving is robot-centric: left stick drives and strafes; right stick X turns. Pedro's manual-drive method receives the stick values and the follower is updated each loop.

This tests Pedro's manual drivetrain path only. It does not measure robot position, follow paths, use Foresight, or make autonomous decisions. If a drive motor fails to initialize or a Pedro update throws an error, the code writes zero power directly to all four drive motors and reports the error.

### TeleOp - Manual (No Pedro)

Uses the four drive motors directly, without Pedro, Pinpoint, or camera. It also controls intake, transfer, and both outtake motors. Use this to check that the motors and wheel directions work independently of Pedro.

### TeleOp - No Shooting

Uses direct drive, intake, and transfer. The two outtake motors are initialized and commanded to zero at startup and stop, but no button in this OpMode can run them. Telemetry warns if either outtake motor could not be initialized and therefore could not be forced to zero.

### Auto - Forward Test

Uses only the four drive motors. At start, it drives forward at `AUTO_FORWARD_POWER` for `AUTO_FORWARD_TIME_MS`, then stops. It does not use encoders, a Pinpoint, cameras, mechanisms, or Pedro.

This is a motion check, not an autonomous scoring routine. It does not guarantee LEAVE and does not attempt PARK.

### Camera - Visibility Test

Uses only the configured webcam. The Driver Station camera stream shows AprilTag detections and color outlines for yellow, red, and blue objects. BIOBUZZ Manual TU07 §9.8, page 72, describes yellow Pollen and red/blue Nectar. Color outlines are a visibility aid, not reliable game-piece classification.

It does not drive, run mechanisms, read Hive state, or select a scoring target.

## Gamepad controls

### Gamepad 1 (all TeleOps)

- Left stick Y: forward/back.
- Left stick X: strafe.
- Right stick X: turn.

Inputs smaller than 0.05 in magnitude are ignored to prevent stick drift. There is no field-centric toggle, precision-speed mode, auto-aim, or tracking assist.

The INIT screen reports whether the expected drive and mechanism motors were found before the OpMode starts.

### Gamepad 2 (PedroPathing and Manual TeleOps)

- Left bumper: run intake, then transfer automatically.
- Right bumper: run both outtake motors for the shooting preset.
- Hold Back: direct motor test. Left stick Y runs intake, right stick Y runs transfer, left trigger runs the left outtake, and right trigger runs the right outtake.

### Gamepad 2 (No Shooting TeleOp)

- Left bumper: run intake, then transfer.
- Hold Back: test intake and transfer.
- Outtake motors stay at zero. The right bumper and outtake triggers have no effect.

## What the mechanism presets do and do not do

- Intake runs for `INTAKE_SECONDS`, then stops and runs transfer for `TRANSFER_SECONDS`.
- Shooting runs both outtake motors together for `SHOOT_SECONDS`.
- A new shoot press can interrupt intake or transfer. Pressing shoot again while a shot is running does not restart its timer.
- Intake/transfer requires both motors. Automatic shooting requires both outtake motors.
- Hold Back bypasses the presets for individual motor checks. It can run one outtake motor even if the other is missing. Use this only for controlled bench testing.

There are no game-piece sensors. The code cannot confirm that a piece was collected, transferred, or launched. Shooting assumes a piece is already in the shooter; it does not automatically run transfer, aim, or wait for camera confirmation.

## Change only the values that are useful now

All mapped motors are set to `RUN_WITHOUT_ENCODER` and BRAKE at zero power. These tests use open-loop motor power; they do not measure distance from drive-motor encoders.

Edit `RobotConfig.java` for:
- Hardware-map names.
- Motor directions.
- Joystick deadband.
- Intake, transfer, and outtake powers/times.
- Forward-test power/time.

Do not add field poses, path gains, Hive predictions, route lanes, camera-based choices, Pinpoint settings, or servo abstractions until a tested feature needs them.

## Hardware failure behavior

- If any drive motor is missing or fails configuration, direct-drive and auto code sends zero to all available drive motors. It does not try to drive on three wheels.
- Pedro initializes only if all four drive motors are available. If its update fails, the code writes zero power directly to all four drive motors.
- If either intake or transfer is missing, the automatic intake/transfer preset will not start.
- If either outtake motor is missing, the automatic shooting preset will not start.
- The No Shooting mode initializes and zeros the outtake motors but never activates them. It warns if an outtake cannot be initialized and zeroed.
- A missing webcam affects only Camera - Visibility Test.

## BIOBUZZ scoring reference

BIOBUZZ Manual TU07:
- §10.5.4, page 88: LEAVE means the robot is no longer contacting the perimeter wall. PARK means at least part of the robot is in the Loading Zone.
- Table 10-2, page 89: LEAVE is worth 3 AUTO points. PARK is worth 5 AUTO points and 5 TELEOP points.
- §10.5, page 84: LEAVE and AUTO PARK are assessed at the end of AUTO; TELEOP PARK is assessed at the end of the MATCH.

The forward test only checks forward motion. Do not treat it as a Leave-and-Park routine.

## First hardware test

1. Compare the Hardware Inventory table with the actual Driver Station Hardware page.
2. Raise and secure the robot so no wheel or mechanism can contact the floor or a person.
3. Run Auto - Forward Test at the configured low power and time. Confirm wheel direction and stop immediately if any wheel runs the wrong way.
4. Test TeleOp - Manual (No Pedro) for forward, backward, left/right strafe, and turning.
5. Hold Back and test each mechanism motor individually. Keep hands clear.
6. Test the intake/transfer preset, then shooting, only after each motor turns in the correct direction.
7. Run TeleOp - No Shooting and confirm no outtake motor runs.
8. Test TeleOp - PedroPathing after direct drive works. It does not require odometry.
9. Run Camera - Visibility Test separately and inspect the Driver Station camera stream.
