# BIOBUZZ robot code: current hardware tests

This is a small bring-up codebase for the current testing stage, not an autonomous framework.

## Start here

- Change hardware names, motor directions, powers, times, and Pinpoint measurements in `RobotConfig.java`.
- Read `TeleOpManual.java` for the simplest drive-and-mechanism control.
- Read `RobotHardware.java` for motor setup, mecanum drive, and the short timed mechanism controls.
- Read `TeleOpPedro.java` only when testing Pedro with the real Pinpoint localizer.
- Read `CameraTest.java` for the AprilTag camera check.

## Hardware names

Names must match the Driver Station Hardware page.

| Device | Name in code | Notes |
|---|---|---|
| Drive motors | `fl`, `fr`, `bl`, `br` | All four are required to drive |
| Intake motor | `intake` | Used when present |
| Transfer motor | Blank in `RobotConfig` | Optional; disabled until configured |
| Left outtake | `ol` | Used by the shooting test |
| Right outtake | `or` | Used by the shooting test |
| GoBilda Pinpoint | `odom` | Used by Pedro TeleOp with the two odometry pods |
| Webcam | `cam1` in code | The actual Driver Station device name is `Webcam 2` |

Do not treat this list as a complete physical inventory. Check the assembled robot and the Driver Station configuration. No servo names or servo behavior are assumed in this code.

## OpModes

| Driver Station name | What it tests |
|---|---|
| TeleOp - Manual (No Pedro) | Direct mecanum drive and mechanism controls |
| TeleOp - Pedro + Pinpoint | Pedro manual drive using Pinpoint odometry and mechanism controls |
| TeleOp - No Shooting | Direct drive and intake; outtakes stay at zero |
| Auto - Forward Test | A short, constant-power forward movement |
| Camera - AprilTag Test | Webcam 2, live camera view, and AprilTag detections only |

The camera is separate from all drive and mechanism OpModes. It does not determine Hive state or control the robot.

## Gamepad controls

### Gamepad 1

- Left stick Y: forward/back.
- Left stick X: strafe.
- Right stick X: turn.
- Inputs smaller than 0.05 are ignored.

### Gamepad 2 (Manual and Pedro TeleOps)

- Left bumper: run intake for `INTAKE_SECONDS`. If transfer is enabled and found, it runs after intake.
- Right bumper: run both outtakes for `SHOOT_SECONDS`.
- Hold Back: manual motor test. Left stick Y runs intake, right stick Y runs transfer, left trigger runs `ol`, and right trigger runs `or`.

### Gamepad 2 (No Shooting)

- Left bumper: run intake, then optional transfer.
- Hold Back: manually test intake and optional transfer.
- Outtakes are initialized and held at zero. This OpMode never sends them nonzero power.

## Transfer motor

`RobotConfig.TRANSFER` is blank by default. Intake therefore runs by itself and no transfer motor is looked up or required. When a real transfer motor is installed, enter its exact Driver Station name. If a configured transfer motor cannot be found, the intake preset will not start.

## Pedro and Pinpoint

The Pedro TeleOp uses the GoBilda Pinpoint localizer and the two odometry pods. The device name, pod directions, and offsets are in `RobotConfig.java`.

The current offsets and directions are carried over starting values, not verified measurements. Check the pod mounting direction and measure the offsets from the robot's tracking center before trusting pose telemetry. This OpMode uses Pedro manual drive; it does not follow autonomous paths.

If Pedro or Pinpoint fails, the OpMode stops the drive motors directly. Use the no-Pedro TeleOp to test the drive motors separately.

## Camera

The camera's Driver Station hardware name is `Webcam 2`; the code refers to it as `cam1` through `RobotConfig.CAM1`. The Camera - AprilTag Test OpMode only initializes an AprilTag processor and shows the live view and detected tag/cluster names. It does not run color detection, make game decisions, or control motors.

## Mechanism limits

- Intake and transfer use timers. No sensor confirms that a game piece moved.
- Shooting runs `ol` and `or` together for the configured time.
- The shooting preset requires both outtake motors to be present.
- Powers and times are starting values. Test one mechanism at a time with the robot secured.
- No conveyor, transfer motor, servo, or sensor should be assumed to exist until its physical installation and hardware name are confirmed.

## Forward test

Auto - Forward Test uses constant power for one configured duration, then stops. It is only a movement check. It does not guarantee LEAVE and does not attempt PARK.

BIOBUZZ Manual TU07:
- §10.5.4, page 88: LEAVE means the robot is no longer contacting the perimeter wall. PARK means at least part of the robot is in the Loading Zone.
- Table 10-2, page 89: LEAVE is worth 3 AUTO points. PARK is worth 5 AUTO points and 5 TELEOP points.
- §10.5, page 84: LEAVE and AUTO PARK are assessed at the end of AUTO; TELEOP PARK is assessed at the end of the MATCH.

## First hardware test

1. Check every configured name against the Driver Station Hardware page.
2. Raise and secure the robot so no wheel or mechanism can contact the floor or a person.
3. Run Auto - Forward Test at the configured low power and time; verify wheel direction.
4. Test each drive direction in TeleOp - Manual (No Pedro).
5. Hold Back and test each installed mechanism motor separately.
6. Test intake with transfer left blank. Configure transfer only when that motor is installed.
7. Test the shooting preset only after confirming `ol` and `or` are the correct outtake ports.
8. Test Pedro + Pinpoint and verify odometry direction and measured offsets.
9. Run Camera - AprilTag Test separately.
