# BIOBUZZ autos: setup and tuning

## What the two autos do

**BIOBUZZ - Leave and Park**
1. Starts at the configured side wall.
2. Drives 8 inches away from the wall.
3. Moves to a lane near the alliance-side wall and follows that lane to the Loading Zone.

**BIOBUZZ - Shoot, Collect 1 Pollen, Shoot, Park**
1. Leaves the wall and looks for an alliance Cell tag.
2. Feeds the preload while driving to the tracked Cell, aims, and shoots.
3. Chooses the closest of the four estimated Flower approach points, drives there, and intakes one Pollen.
4. Returns to a Cell with a recent tag, aims, and shoots.
5. Drives around the edge toward the alliance Loading Zone.

The second routine skips scoring if the Cell camera or a mechanism motor is missing. If a Cell cannot be tracked, a path times out, or the active Cell changes repeatedly, it stops scoring and attempts to park. It reserves the last part of autonomous for the parking path.

## Ranking points

The official BIOBUZZ manual awards 3 match points for LEAVE and 5 for AUTO PARK. Together, one robot can earn 8 match points. For ordinary events, the SWARM Ranking Point threshold is 16 combined LEAVE + PARK points, so both alliance robots normally need to leave and park to reach that threshold. Check the current event's rules and published threshold before relying on it.

A robot only earns LEAVE if it stops contacting the perimeter wall. PARK requires at least part of the robot to be inside its alliance Loading Zone. A target coordinate is not proof that the field criterion was met; watch the robot's final position and adjust it if needed.

## Robot dimensions and the Pinpoint

The Pedro `Pose` is an `(x, y, heading)` estimate for a chosen point on the robot. It is not inherently the chassis center or a corner. This project treats that point as the robot's center of rotation.

The Pinpoint's pod offsets are the locations of the tracking pods relative to the robot's center of rotation. The localizer uses those offsets to account for the pods moving when the robot turns. They do not set the robot's dimensions or automatically move the pose origin. Keep the pose origin and the measured pod offsets consistent.

`RobotGeometry.java` currently has placeholder width and length values of 18 inches. The FTC manual limits the robot's starting configuration to an 18 × 18 × 18 inch volume; that is a maximum, not a requirement that the actual chassis be exactly 18 × 18 inches. Measure the robot, including bumpers, and replace both `WIDTH_INCHES` and `LENGTH_INCHES`. The half-dimensions and intake reach are calculated from them. Also measure intake extension and overhangs. After the match starts, check the fully deployed robot against the 18 × 24 inch footprint limit; the current 6-inch intake-extension estimate must not be treated as proof of compliance.

The uploaded `am-5850 BIOBUZZ.stl` has bounds of about 3586 × 3586 × 15 in the file's raw units and contains no unit metadata. It appears to be the thin field floor, not a robot chassis, so it cannot supply your robot's width or length.

## Starting pose and coordinates

The field is 144 × 144 inches nominally, measured inside the perimeter. The code uses the bottom-left inside corner as (0, 0), X increasing right and Y increasing up. There are two alliance Loading Zones: red is on the left wall near the upper end, blue is on the right wall near the lower end. The current zone-center estimates are (10, 108) for red and (134, 36) for blue, assuming an 18-inch robot. The actual poses use the robot dimensions and put only part of the robot inside the zone. Verify the coordinates against the official diagram and field CAD.

The start choices are `SIDE_WALL`, `NEAR_CORNER`, and `FAR_CORNER`. Each is outside both Loading Zones, on its own alliance side, and facing into the field. Use D-pad left/right in INIT to choose one. The new autos start at `SIDE_WALL` by default, then route to the correct alliance zone when parking. The park routine goes to the nearest inset perimeter entry, follows edge waypoints toward the correct wall, and then moves into the zone.

The manual also requires the ROBOT to start outside the LOADING ZONE and wholly on its own ALLIANCE side. Rule R102 limits its starting configuration to an 18-inch cube; the robot does not have to be exactly 18 inches wide or long.

- `StartPosition.pose(redAlliance)`: set the robot-center estimate for the selected perimeter start. The robot must be physically placed against the matching wall, not in the Loading Zone.
- `FLOWER_LEFT_Y_INCHES`, `FLOWER_TOP_X_INCHES`, `FLOWER_RIGHT_Y_INCHES`, `FLOWER_BOTTOM_X_INCHES`: replace with positions measured from the official field CAD or a field whose layout has been checked. The current nominal estimates are left-wall Y = 48 in, top-wall X = 48 in, right-wall Y = 96 in, and bottom-wall X = 96 in.
- `loadingZonePose()`: uses the robot's measured half-dimensions to place the pose near each alliance Loading Zone. The official Loading Zone is about 23 inches wide by 11 inches deep, so verify the final pose places part of the physical robot inside the taped zone.
- `PARK_LANE_CLEARANCE_INCHES`: keeps the park route away from the perimeter Flowers. Increase it if the robot or a deployed mechanism needs more clearance.
- The field CAD is the authority for the exact Hive/Cell, AprilTag, Flower, and Loading Zone positions. Don't treat nominal coordinates as measured ones.

The four Flower targets are estimates from the official diagram, not surveyed coordinates. The manual shows their placement but does not give exact X/Y centers in its prose. The code compares the robot's pose to each Flower's approach pose and selects the shortest straight-line distance. It does not account for moving obstacles, so check the chosen path on the actual field.

## Cell aiming

Gamepad 1 **Y** holds the robot's X/Y position and turns toward the latest visible alliance Cell tag. The cluster is on the bottom of the Cell and faces the tiles; with the upward camera, it identifies the Cell whose opening is facing upward.

If neither Cell has a usable recent tag, holding Y aims at the midpoint between the two nominal Cell positions for the selected alliance. The Driver Station telemetry shows an **AIM WARNING** until a usable tag is tracked. The midpoint is only a fallback direction; it cannot tell which Cell is currently tipped upward.

The nominal Cell centers in `FieldTagLibrary.java` are approximate. The manual gives the 18.8-inch spacing between Cells and the AprilTag ID groups, but the exact tag-to-Cell center relationship and camera pose must be checked from CAD and the built robot.

## Run the Pedro tuners

`Constants.foresightConfig` is intentionally null until the real robot has been tuned. The autonomous OpModes refuse to drive without it. Do these steps in order, with the robot supervised and room to move.

1. **Measure the robot.** Set `RobotGeometry.WIDTH_INCHES`, `LENGTH_INCHES`, `INTAKE_EXTENSION_INCHES`, overhangs, and all camera mounting coordinates. Enter the actual dimensions in inches.
2. **Check the four drive motors.** Lift the robot so the wheels are clear. Connect to the Robot Controller and open `http://192.168.43.1:10158`. Select **Mecanum Tuner**. Enter the hardware names from `HardwareConfig.java` exactly (`fl`, `fr`, `bl`, `br`). Run each wheel test and copy the generated directions and names into `Constants.drivetrainConfig`.
3. **Tune the Pinpoint.** Select **Pinpoint Tuner**. The Pinpoint HardwareMap name in this project is `odom`, not the procedure's default `pinpoint`. Select the pod type that matches the hardware. Follow the prompts: push forward, push left, then rotate 180 degrees counterclockwise about the robot's center of rotation. Copy the generated directions and offsets into `Constants.localizerConfig`.
4. **Check localization before driving paths.** Select **Tests**, then its localization test. Move forward and confirm X changes in the expected direction; move left and confirm Y changes; turn counterclockwise and confirm heading increases. If these are wrong, fix the drive or Pinpoint config before continuing.
5. **Tune Foresight.** Place the robot on a clear, level field with an operator ready to stop it. Select **Foresight Tuner** and complete the requested forward, strafe, braking, and heading tests. The routine moves the robot. Copy the generated `ForesightConfig` into `Constants.foresightConfig`.
6. **Test paths slowly.** Run the Pedro line/hold tests first, then run **Leave and Park** at reduced risk. Confirm that the robot moves inward when commanded, follows the side lane, and stops partially inside the correct Loading Zone. Use an emergency-stop operator for every first powered test.
7. **Measure the Cell and Flower positions.** With the robot stationary, compare the pose and observed tags against the taped field and CAD. Update `FieldTagLibrary.java` and the four Flower coordinate constants. Verify the camera's position and angles, too.

The tuners can produce configuration code, but they cannot decide the right field points or mechanism settings. Those must be measured on your robot.

## Mechanism timing and motor direction

After reaching the approach pose, the robot turns toward the Cell, follows `CellTarget.scoringPose`, and turns to the tracked Cell again before firing. `APPROACH_STANDOFF_INCHES` and `SCORING_STANDOFF_INCHES` in `CellMap.java` are the two distances used by those poses; their current values are starting estimates.

The sequence in `ScoringMechanism.java` is fixed:

`INTAKING → TRANSFERRING → READY_TO_SCORE → OUTTAKING → CLEARING → INTAKE_OFF`.

The motor powers and durations are now editable near the top of that file:

- `INTAKE_POWER` and `INTAKE_ACQUIRE_TIME_SECONDS`
- `TRANSFER_POWER` and `TRANSFER_TIME_SECONDS`
- `OUTTAKE_LEFT_POWER`, `OUTTAKE_RIGHT_POWER`, and `OUTTAKE_DURATION_SECONDS`
- `CLEARING_DURATION_SECONDS`

Power can be reduced or made negative if a motor's direction is wrong, but verify direction with the robot safely lifted first. The left and right outtake motors currently run at the same time. The code cannot confirm that a Pollen moved or scored because this robot has no mechanism sensor; successful transfers and shots remain timed assumptions.

Tune one value at a time. Run several cycles, record whether the ball reaches the scoring position, then change the relevant power or time. Do not shorten the intake time to fix a transfer problem, or change the transfer time to fix an outtake problem; they are separate stages.

## Official references

- [BIOBUZZ Competition Manual TU06 (October 22, 2026)](https://ftc-resources.firstinspires.org/ftc/game/cm-html/BIOBUZZ%20Competition%20Manual%20-%20TU06.htm): Sections 9.1–9.9 describe field dimensions, Loading Zones, Hives, Flowers, scoring elements, and AprilTags; Sections 10.3, 10.5, and Rule R102 cover start conditions, LEAVE/PARK scoring, and the starting-size limit. Check the current [FIRST game materials page](https://ftc-resources.firstinspires.org/ftc/game) for later updates.
- [BIOBUZZ Playing Field Resources and CAD](https://ftc-resources.firstinspires.org/ftc/field): the official field CAD and setup guides are the sources for exact coordinates.
- [PedroPathing Pinpoint tuning](https://pedropathing.com/docs/pathing/tuning/localization/pinpoint): pod direction and offset tuning.
- [PedroPathing localization testing](https://pedropathing.com/docs/pathing/tuning/localization): how to check the localizer.
- [PedroPathing Foresight tuning](https://pedropathing.com/docs/pathing/tuning/foresight): robot-specific motion-controller tuning.
