# BIOBUZZ robot code: current hardware tests

This is a small bring-up codebase for the current testing stage. It is not a future autonomous framework.

## Hardware configuration

The software has editable names for the devices it currently knows about. The names are not proof that every device is physically installed; compare them with the Driver Station Hardware page.

| Device | Config name | Status in this code |
|---|---|---|
| Four drive motors | `fl`, `fr`, `bl`, `br` | Required for every drive test |
| Intake motor | `intake` | Used by mechanism TeleOps if present |
| Transfer motor | Blank by default | Optional and disabled until a real motor is configured |
| Left outtake motor | `ol` | Used by shooting preset; harmlessly reported missing if not installed |
| Right outtake motor | `or` | Used by shooting preset; harmlessly reported missing if not installed |
| GoBilda Pinpoint + odometry pods | `odom` | Used by Pedro + Pinpoint TeleOp |
| Webcam | `Webcam 2` | Camera test only |
| Servos | No servo names configured | No servo is assumed or commanded yet |

Do not treat this table as a final physical inventory. The actual number of installed mechanism motors and servos must come from the assembled robot and its Driver Station configuration. This code intentionally does not invent servo names or servo jobs.

## OpModes and hardware used

| Driver Station OpMode | Hardware used |
|---|---|
| TeleOp - Pedro + Pinpoint | Four drive motors, Pinpoint/odometry pods, configured mechanism motors |
| TeleOp - Manual (No Pedro) | Four drive motors, configured mechanism motors |
| TeleOp - No Shooting | Four drive motors, intake, optional transfer; outtakes are initialized and held at zero |
| Auto - Forward Test | Four drive motors only |
| Camera - Visibility Test | Webcam only |

Camera is not initialized by any drivetrain or mechanism OpMode. Pinpoint is used by Pedro + Pinpoint, but not by direct-drive TeleOps or the simple forward test.

## Pedro + Pinpoint

This mode uses the real GoBilda Pinpoint localizer with the two odometry pods. The device name, pod offsets, and directions are configured in `RobotConfig.java`.

The offset values currently carried over from the team's earlier configuration are starting values, not verified measurements. Confirm that the pod mounting orientation matches those directions and remeasure offsets from the robot's tracking center. The pose telemetry is only useful after this check.

This mode calls Pedro manual drive and updates the Follower. It does not follow autonomous paths or use Foresight. Do not interpret this mode as path-following being tuned.

If Pinpoint or Pedro fails during initialization/update, the OpMode reports the failure and directly commands zero power to the four drive motors. Use TeleOp - Manual (No Pedro) to separate drivetrain motor testing from localization/Pedro problems.

## Direct-drive TeleOps

### TeleOp - Manual (No Pedro)

Uses the four drive motors directly and does not initialize Pinpoint or the camera. It is the basic fallback for checking the mecanum wheels. It also offers manual and timed mechanism testing.

### TeleOp - No Shooting

Uses the four drive motors and intake. Transfer only runs when its config name is set and the motor is found. Both outtake motors are looked up and set to zero so they cannot be left running from a prior OpMode. This mode never sends nonzero power to either outtake.

## Gamepad controls

### Gamepad 1 (all TeleOps)

- Left stick Y: forward/back.
- Left stick X: strafe.
- Right stick X: turn.

Inputs smaller than 0.05 are ignored to prevent stick drift. There is no field-centric toggle, precision-speed mode, auto-aim, or tracking assist.

### Gamepad 2 (Pedro and Manual TeleOps)

- Left bumper: run intake for the configured time. If a transfer motor is enabled and found, it runs after intake.
- Right bumper: run both outtake motors for the configured shooting time.
- Hold Back: manual motor test. Left stick Y runs intake, right stick Y runs transfer if configured, left trigger runs `ol`, and right trigger runs `or`.

### No-shoot TeleOp

- Left bumper: run intake, then optional transfer.
- Hold Back: manually test intake and optional transfer.
- Outtake motors remain at zero regardless of gamepad input.

## Optional transfer behavior

`RobotConfig.TRANSFER` is blank by default. With this setting:
- Intake works by itself for `INTAKE_SECONDS`, then stops.
- No transfer motor is looked up or required.
- The driver can still use intake in the no-shoot and manual TeleOps.

When a real transfer motor is installed and configured, enter its exact Driver Station name in `RobotConfig.TRANSFER`. Then the intake preset runs intake first and transfer second. If a nonblank transfer name is configured but that device is missing, the preset will not start and telemetry reports the missing motor.

No code should assume a conveyor, transfer motor, servo, or sensor exists until it is physically installed and given a real hardware-map name.

## Mechanism behavior and limits

- Intake and transfer are timed. There are no sensors confirming that a game piece moved.
- The shooting preset runs `ol` and `or` together for `SHOOT_SECONDS`.
- Automatic shooting requires both outtake motors to be present. Manual override can test either outtake independently.
- Shooting assumes a piece is already at the shooter. It does not automatically transfer, aim, or wait for camera confirmation.
- Powers and times are initial settings. Tune them with the robot secured and test one mechanism at a time.

## Values to customize

Edit `RobotConfig.java`:
- Hardware names (use the exact names configured on the Driver Station).
- Motor directions.
- Joystick deadband.
- Intake/outtake powers and times.
- Transfer name and power/time only when a transfer motor is actually installed.
- Pinpoint device name, pod offsets, and pod directions after measuring the actual mounting.
- Forward-test power and time.

Servo configuration should be added only when the physical servo, its port/name, direction or positions, and job are known. There is no placeholder servo logic.

## Motor behavior

All mapped DC motors are set to `RUN_WITHOUT_ENCODER` and BRAKE at zero power. The simple tests use open-loop motor power. Pinpoint provides odometry in the Pedro TeleOp; drive-motor encoder distance is not used.

## Hardware failure behavior

- If one of the four drive motors is missing or fails configuration, the drive modes do not drive on the remaining three.
- If Pedro/Pinpoint initialization or update fails, the Pedro mode writes zero power directly to all drive motors.
- Intake preset requires an intake motor. If transfer is enabled, it also requires the configured transfer motor. If transfer is blank, intake runs alone.
- Automatic shooting requires both `ol` and `or`.
- No-shoot mode holds available outtake motors at zero and warns if they cannot be initialized.
- A missing webcam affects only the camera diagnostic.

## Camera test

Camera - Visibility Test uses only the configured webcam. The Driver Station stream shows AprilTag detections and yellow/red/blue color blobs. BIOBUZZ Manual TU07 §9.8, p. 72, describes yellow Pollen and red/blue Nectar. Color detection is only a visual check, not authoritative game-piece classification.

The camera does not change Hive state or control the robot.

## BIOBUZZ scoring reference

BIOBUZZ Manual TU07:
- §10.5.4, page 88: LEAVE means the robot is no longer contacting the perimeter wall. PARK means at least part of the robot is in the Loading Zone.
- Table 10-2, page 89: LEAVE is worth 3 AUTO points. PARK is worth 5 AUTO points and 5 TELEOP points.
- §10.5, page 84: LEAVE and AUTO PARK are assessed at the end of AUTO; TELEOP PARK is assessed at the end of the MATCH.

Auto - Forward Test is a simple motion check. It does not guarantee LEAVE and does not attempt PARK.

## First hardware test

1. Compare configured hardware names with the actual Driver Station Hardware page.
2. Raise and secure the robot so no wheel or mechanism can contact the floor or a person.
3. Run Auto - Forward Test at the configured low power and time; verify wheel direction.
4. Test TeleOp - Manual (No Pedro) in each drive direction.
5. Hold Back and test each installed mechanism motor separately.
6. Test intake-only operation with transfer left blank. Configure transfer only if/when that physical motor exists.
7. Test the shooting preset only after confirming `ol` and `or` are the correct outtake ports and spin safely.
8. Test Pedro + Pinpoint; check odometry direction and measured offsets before trusting pose.
9. Run Camera - Visibility Test separately.
