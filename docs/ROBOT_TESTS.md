# BIOBUZZ robot test code

The custom robot code is kept in seven Java files in one package.

## OpModes

| Driver Station name | Use |
|---|---|
| TeleOp - Pedro + Pinpoint | Pedro's manual mecanum drive with the Pinpoint localizer. No Foresight or path following is used. Press B to toggle field-centric drive after the Pinpoint is calibrated. |
| TeleOp - Manual (No Pedro) | Direct four-motor drive plus the timed intake, transfer, and outtake controls. |
| TeleOp - Drive Only | Direct four-motor drive. Does not initialize the mechanisms or camera. |
| Auto - Movement Test | A short, low-power timed sequence: forward, right, backward, with stops between moves. No localization or game-piece mechanisms. |

## Controls

Gamepad 1:
- Left stick: forward and strafe.
- Right stick X: turn.
- Right trigger: precision speed.
- B in the Pedro TeleOp: switch between robot-centric and field-centric drive.

Gamepad 2:
- Left bumper: start intake, then timed transfer.
- Right bumper: shoot after transfer has finished.
- Hold Back: manual override. Left stick Y runs intake, right stick Y runs transfer, and the triggers run the left and right outtake motors independently.

The mechanism has no sensors. The state shown on Driver Station is based on timers, not confirmation that a game piece was acquired or scored. Change all mechanism powers and times in `RobotConfig.java`.

## Setup to verify

1. Confirm the configured hardware names in `RobotConfig.java`: `fl`, `fr`, `bl`, `br`, `odom`, `Webcam 2`, `intake`, `transfer`, `outtakeLeft`, and `outtakeRight`.
2. Lift the robot so its wheels are clear. Run `Auto - Movement Test` at the default 0.20 power and 0.20-second movement time. Verify forward, right, and backward separately. Change the four drive motor directions in `RobotConfig.java` if a wheel is reversed.
3. Run `TeleOp - Manual (No Pedro)`. Verify each mechanism independently using Back override before testing the timed cycle.
4. Run the Pedro TeleOp only after checking the Pinpoint hardware name, pod direction, and offsets. The offsets in `RobotConfig.java` are inherited estimates, not measured values. Field-centric mode depends on correct localization.
5. Confirm the one AprilTag webcam name. The camera code currently reports visible tag IDs only; it does not alter the robot pose.

## Coordinate convention

Field coordinates are in inches. A pose is `(x, y, heading)` and describes the **center of the robot chassis**, not its front bumper or a wheel. The origin is the lower-left corner of the field; +X is right, +Y is upfield, and heading is counterclockwise from +X in radians.

Measure the assembled robot and set `ROBOT_WIDTH_INCHES` and `ROBOT_LENGTH_INCHES` in `RobotConfig.java`. They are deliberately unset: do not assume an 18-by-18-inch robot. `robotCenterFromWall()` converts a wall contact/target and inward heading to a chassis-center pose using those dimensions.

The field is 144 by 144 inches. Flower target coordinates are intentionally not included yet. The old values were estimates and the official manual says the 3D field CAD is the authoritative source for geometry. Measure the Flower centers from that CAD before adding targets; do not tune autonomous routes against sketch positions.

## PedroPathing status

The Pedro TeleOp is a manual-drive and Pinpoint test. It does not claim that automatic path following is ready. Pedro's follower can run manual drive without a Foresight algorithm; path following cannot be treated as calibrated until the drivetrain, localization, and Foresight configuration have been tuned on this robot.

The movement auto deliberately uses timed motor powers as a baseline. These tests isolate motor direction and basic movement before adding path following, field targets, or scoring sequences.
