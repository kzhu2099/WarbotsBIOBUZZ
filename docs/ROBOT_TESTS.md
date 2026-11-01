# BIOBUZZ robot code: current hardware tests

This is a small bring-up codebase for the current month. It is not a scoring-autonomous framework.

## Hardware inventory

The code is written for these configured devices:

| Device | Config name | Count | Used by |
|---|---|---:|---|
| Drive motor | `fl`, `fr`, `bl`, `br` | 4 | All drive OpModes and forward auto |
| Intake motor | `intake` | 1 | Both mechanism TeleOps and Pedro TeleOp |
| Transfer motor | `transfer` | 1 | Both mechanism TeleOps and Pedro TeleOp |
| Outtake motor | `outtakeLeft`, `outtakeRight` | 2 | Manual TeleOp and Pedro TeleOp only |
| GoBilda Pinpoint | `odom` | 1 | Pedro TeleOp only |
| Webcam | `Webcam 2` | 1 | Camera test only |
| Servo | None configured | **0** | Not used by this code |

That is **8 DC motors, 0 servos, 1 Pinpoint, and 1 webcam** for the full set of tests. These names are inherited from the team's existing source code. The Control Hub configuration itself is not stored in this repository, so compare this table with the actual Driver Station Hardware page before running.

No OpMode in this code creates, maps, or commands a Servo. If the physical robot has a servo, it will stay unused.

## Which devices each OpMode touches

| Driver Station OpMode | Motors it maps | Other hardware |
|---|---:|---|
| TeleOp - Pedro + Pinpoint | All 8 | Pinpoint |
| TeleOp - Manual (No Pedro) | All 8 | None |
| TeleOp - No Shooting | 6: four drive, intake, transfer | Does not map either outtake motor |
| Auto - Forward Test | 4 drive | None |
| Camera - Visibility Test | 0 | Webcam |

The camera is not initialized by a drivetrain or mechanism OpMode. The Pinpoint is not initialized by the direct-drive TeleOps or the forward auto. This prevents a missing camera or Pinpoint from breaking tests that do not need those devices.

## OpModes and exactly what they do

### TeleOp - Pedro + Pinpoint

Uses Pedro's Mecanum drivetrain, the Pinpoint localizer, and the four mechanism motors. Driving is robot-centric: left stick drives/ strafes; right stick X turns. It calls Pedro's manual-drive method and updates the follower each loop.

This does not follow paths, use Foresight, or make autonomous decisions. If a drive motor or Pinpoint fails to initialize, or a Pedro update throws an error, it writes zero power directly to the four drive motors and reports the error. The mechanism controls can still be used if the mechanism motors are present.

### TeleOp - Manual (No Pedro)

Uses the four drive motors directly, without Pedro or Pinpoint. It is the fallback test when Pedro or odometry is not ready. It also uses intake, transfer, and both outtake motors.

### TeleOp - No Shooting

Uses only the four drive motors, intake, and transfer. The code does not even look up the outtake motors in this mode. The right bumper and outtake triggers have no effect.

### Auto - Forward Test

Uses only the four drive motors. At start, it drives forward at `AUTO_FORWARD_POWER` for `AUTO_FORWARD_TIME_MS`, then stops. It does not use encoders, Pinpoint, cameras, mechanisms, or any path follower.

This is a motion check, not an autonomous scoring routine. It does not guarantee LEAVE and does not attempt PARK.

### Camera - Visibility Test

Uses only the configured webcam. The live Driver Station camera stream shows the AprilTag processor and color outlines for yellow, red, and blue objects. BIOBUZZ Manual TU07 §9.8 p.72 describes yellow Pollen and red/blue Nectar. The color outlines are visual checks, not reliable game-piece classification.

It does not drive, run mechanisms, read Hive state, or select a scoring target.

## Gamepad controls

### Gamepad 1 (all TeleOps)

- Left stick Y: forward/back.
- Left stick X: strafe.
- Right stick X: turn.

No field-centric toggle, precision-speed mode, auto-aim, or tracking assist is active.

### Gamepad 2 (Pedro and Manual TeleOps)

- Left bumper: intake, followed automatically by transfer.
- Right bumper: run both outtake motors for the shooting preset.
- Hold Back: direct motor test. Left stick Y runs intake, right stick Y runs transfer, left trigger runs left outtake, and right trigger runs right outtake.

The no-shoot TeleOp maps no outtake motors, so the back-button triggers cannot run them there.

## What the mechanism presets do and do not do

- Intake preset runs intake for `INTAKE_SECONDS`, then stops intake and runs transfer for `TRANSFER_SECONDS`.
- Shooting preset runs both outtake motors together for `SHOOT_SECONDS`.
- A new shoot press can interrupt intake or transfer. Pressing shoot repeatedly while a shot is already running does not restart its timer.
- The shooting preset requires both outtake motors to be found. The intake/transfer preset requires both intake and transfer motors to be found.
- Hold Back bypasses the presets for individual motor checks. This is deliberate for bench testing; it can run one outtake motor even when the other is missing, so use it only when testing that motor.

There are no game-piece sensors. The code cannot confirm that a ball was collected, transferred, or launched. Shooting does not automatically run transfer, aim at a target, or wait for camera confirmation. It assumes the ball is already in position to be launched.

## Change only the values that are useful now

All motors are explicitly configured for `RUN_WITHOUT_ENCODER` and brake at zero power. The robot uses open-loop, time-based motor power; it does not use drive-motor encoder positions for distance control.

Edit `RobotConfig.java` for:
- Hardware-map names.
- Motor directions.
- Intake, transfer, and outtake powers/times.
- Forward-test power/time.
- Pinpoint offsets/directions, only when measuring or testing the Pinpoint.

Do not add field poses, path gains, Hive predictions, route lanes, camera-based choices, or servo abstractions until there is a tested feature that needs them.

## Hardware failure behavior

- If any one drive motor is missing, direct-drive and auto code sends zero to all available drive motors. It does not try to drive on three wheels.
- If Pedro cannot initialize or update, the Pedro OpMode directly zeros all four drive motors and reports the reason.
- If either motor needed for an automatic intake/transfer preset is missing, that preset will not start.
- If either outtake motor is missing, the automatic shooting preset will not start. Manual override still allows independent motor testing.
- The no-shoot mode never maps the outtake motors.
- A missing Pinpoint affects only Pedro + Pinpoint.
- A missing webcam affects only Camera - Visibility Test.

## BIOBUZZ scoring reference

BIOBUZZ Manual TU07:
- §10.5.4, page 88: LEAVE means the robot is no longer contacting the perimeter wall. PARK means at least part of the robot is in the Loading Zone.
- Table 10-2, page 89: LEAVE is 3 AUTO points. PARK is 5 AUTO points and 5 TELEOP points.
- §10.5, page 84: LEAVE and AUTO PARK are assessed at the end of AUTO; TELEOP PARK is assessed at the end of the MATCH.

The forward test only checks forward motion. Do not treat it as a Leave-and-Park routine.

## First hardware test

1. Compare the Hardware Inventory table with the actual Driver Station Hardware page.
2. Raise the robot so no wheel or mechanism can touch the floor or a person.
3. Check all four wheel names and directions, then run Auto - Forward Test at the configured low power and time.
4. Test TeleOp - Manual (No Pedro) for forward, backward, left/right strafe, and turning. Check each direction separately.
5. Hold Back and test each mechanism motor individually. Keep hands clear.
6. Test the intake/transfer preset, then shooting, only after each motor turns in the correct direction.
7. Run TeleOp - No Shooting and confirm no outtake motor is mapped or activated.
8. Test Pedro + Pinpoint last, after measuring offsets and checking pod directions.
9. Run Camera - Visibility Test separately and inspect the Driver Station camera stream.
