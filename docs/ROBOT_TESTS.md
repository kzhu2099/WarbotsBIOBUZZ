# BIOBUZZ robot test code

The custom robot code is kept in seven Java files in one package.

## OpModes

| Driver Station name | Use |
|---|---|
| TeleOp - Pedro + Pinpoint | Pedro manual mecanum drive with Pinpoint. No Foresight or path following. Press B to toggle field-centric drive after Pinpoint calibration. |
| TeleOp - Manual (No Pedro) | Direct motor drive plus timed intake, transfer, and outtake. |
| TeleOp - Drive Only | Direct four-motor drive. No mechanisms or camera. |
| Auto - Movement Test | Low-power, timed forward/right/back movement test. No localization, mechanisms, or path following. |

## Autonomous selections

The Movement Test includes the INIT selection screen that the later scoring auto will use:

- X: alliance.
- D-pad left/right: one of three start presets: side wall, near corner, far corner.
- D-pad up/down: Flower: left wall, top wall, right wall, bottom wall.
- A: lane: inside Hive or outside Hive.
- B: manual fallback for which Cell starts upward.
- Y: whether the preload sequence is expected to tip the Hive before the Flower leg.

Every selected start pose is the robot chassis center. Measure the assembled width and length, including bumpers, in `RobotConfig.java`; they are intentionally unset until measured. The start presets preserve the team's previous side-wall / near-corner / far-corner choices. They are team presets, not three official named starting locations.

The red/blue fallback Hive orientation is based on the season's starting setup. The camera overrides it after it sees the same scorable cluster repeatedly. If no stable cluster is read, telemetry labels the state `ASSUMED`. B lets the team correct that assumption.

The camera reads BIOBUZZ AprilTag clusters by their metadata name and roll, not by treating the 16 printed tags as 16 independent single tags. A cell is considered the upward-facing/scorable cell when the matching-alliance cluster is seen right-side-up (absolute roll below 90 degrees). Validate that roll interpretation with the actual installed camera orientation before relying on it.

The preload-tip setting is a prediction, not a sensor reading. It defaults to No. If set to Yes, the predicted upward-facing Cell flips before the Flower leg. A later stable camera read should replace that prediction.

## Route lanes

- **Inside Hive:** a transit route under/inside the Hive frame, confined to the robot's own alliance half. It must not cross the field midline into the partner's half.
- **Outside Hive:** a detour around the outside of the Hive frame using the perimeter-side corridor.
- The partner is assigned the opposite lane by default, so the two robots do not plan the same corridor.

The inside lane is for passing through, not waiting under the Hive for scoring elements to fall or deliberately catching them. The manual describes those behaviours as likely strategic.

The actual lane waypoints and Flower centers are not guessed in this test build. Populate them from the official field CAD after measuring the robot footprint and confirming field orientation. The test OpMode displays the plan but only drives the timed forward/right/back sequence.

## Controls

Gamepad 1:
- Left stick: forward and strafe.
- Right stick X: turn.
- Right trigger: precision speed in the TeleOps.
- B in the Pedro TeleOp: switch between robot-centric and field-centric drive.

Gamepad 2:
- Left bumper: start intake, then timed transfer.
- Right bumper: shoot after transfer has finished.
- Hold Back: manual override. Left stick Y runs intake, right stick Y runs transfer, and the triggers run the left and right outtake motors independently.

The mechanism has no sensors. Its state is based on timers, not confirmation that a game piece was acquired or scored. Change all mechanism powers and times in `RobotConfig.java`.

## First hardware test

1. Confirm hardware names in `RobotConfig.java`: `fl`, `fr`, `bl`, `br`, `odom`, `Webcam 2`, `intake`, `transfer`, `outtakeLeft`, and `outtakeRight`.
2. Raise the robot. Run `Auto - Movement Test` at 0.20 power and 0.20 seconds. Verify forward, right, and backward independently.
3. Run `TeleOp - Manual (No Pedro)`. Check each mechanism independently with Back override before the timed cycle.
4. Run Pedro + Pinpoint only after checking odometry hardware name, pod directions, and offsets. The offsets are inherited estimates, not measured values.
5. Check the AprilTag cluster names and roll values from the actual camera mount. Confirm the camera faces the direction assumed by the roll test.
