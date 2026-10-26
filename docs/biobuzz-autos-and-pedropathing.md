# BIOBUZZ autonomous setup

Read this before testing either new auto. The code has not been tested on the physical robot yet.

## Which auto to use

**BIOBUZZ - Leave and Park** is the short routine. It leaves the wall, follows the inset field edge, and tries to finish inside the alliance Loading Zone.

**BIOBUZZ - Shoot, Flower, Shoot, Park** runs this sequence:

1. Leave the wall.
2. Feed and shoot the preload at an alliance Cell.
3. Follow the field edge to a Flower and collect one Pollen.
4. Return to the Cell and shoot that Pollen.
5. Follow the edge to the alliance Loading Zone.

At 21 seconds, the second auto stops scoring and starts parking so it has time to return to the Loading Zone. A timeout also ends scoring early and attempts to park. Without a manual Flower choice, it picks the Flower nearest the selected start.

## Select the start and Flower

Do this while the Driver Station shows INIT:

- **X** switches between red and blue.
- **D-pad left/right** selects `SIDE_WALL`, `NEAR_CORNER`, or `FAR_CORNER`.
- In the scoring auto, **D-pad up/down** selects `LEFT_WALL`, `TOP_WALL`, `RIGHT_WALL`, or `BOTTOM_WALL`. If you don't change it, the nearest Flower to the selected start is used.

Check the displayed alliance, start pose, and Flower before pressing Play. A pose in the code is only the starting estimate. It does not locate the robot for you.

## Where the code lives

| File | What to change here |
|---|---|
| `OpModes/LeaveOnlyAuto.java` | Leave-and-park sequence |
| `OpModes/ShootCollectOnePollenAuto.java` | Preload, Flower pickup, second shot, and park |
| `StartPosition.java` | The three starting poses |
| `BioBuzzAutoConfig.java` | Leave distance, timers, Flower positions, zone poses, and the route around the field |
| `RobotGeometry.java` | Chassis size, intake reach, and camera mounting position |
| `CellMap.java` | Cell tracking and the distances used for approach/scoring poses |
| `FieldTagLibrary.java` | AprilTag IDs and nominal Cell positions |
| `ScoringMechanism.java` | Intake/transfer/outtake order, motor powers, and timings |
| `pedro/Constants.java` | Mecanum directions, Pinpoint setup, and tuned Foresight values |

Keep field coordinates in inches and headings in radians. Avoid copying a coordinate into multiple OpModes; change it in the file that owns it.

## Before the robot will move

`Constants.foresightConfig` is deliberately null until you run the tuner on this robot. The autos stop with an error message rather than trying to drive on made-up control values. A code edit cannot replace measuring the actual drivetrain.

1. **Measure the chassis.** In `RobotGeometry.java`, replace the 18-inch width and length placeholders with the assembled robot's actual dimensions, including bumpers. Measure the deployed intake reach and camera position too. FTC's 18-inch starting size limit is a maximum, not a requirement to build an exactly 18 × 18 inch chassis.
2. **Check the drivetrain directions.** Lift the robot so the wheels can't drive it off the stand. Open `http://192.168.43.1:10158` and run **Mecanum Tuner**. Use the hardware names `fl`, `fr`, `bl`, and `br`. Copy the generated directions into `Constants.java`.
3. **Tune the Pinpoint.** Run **Pinpoint Tuner** with hardware-map name `odom` (that's the name used in this project). Select the pod type actually installed. Follow the tuner prompts and copy its offsets and directions into `Constants.localizerConfig`.
4. **Check localization.** Move the robot forward, left, and counterclockwise by hand. X should increase forward, Y should increase left, and heading should increase counterclockwise. Fix any mismatch before running paths.
5. **Run Foresight Tuner.** Use the same tuning page. The routine moves the robot, so clear the area and have someone ready to stop it. Copy its generated `ForesightConfig` into `Constants.foresightConfig`.
6. **Test the leave auto first.** Run it slowly and check its displayed pose, perimeter route, and final position. Only then try the scoring auto.

The Mecanum, Pinpoint, and Foresight procedures are registered in `pedro/Tuning.java`. The Pinpoint offsets in `Constants.java` are still estimates until the tuner has been run. Don't copy tuning results from another robot.

## How the scoring routine aims

The Cell camera maps the four AprilTag groups to Cells. When a recent tag is visible, the auto uses the corresponding Cell. If no usable tag is available, it picks the closer nominal Cell position so the robot can still make an attempt; the Driver Station warning says when it is using an estimate. That fallback can point at the wrong Cell if the active opening is unknown, so it is not a guarantee of scoring.

The Cell approach distance and scoring distance are in `CellMap.java`. The first gives the robot room to turn; the second is where it fires. Tune these by moving the robot closer/farther and trying repeated shots. The values currently in code are starting points, not official field dimensions.

The four Flower positions in `BioBuzzAutoConfig.java` are estimates from the field diagram. Measure their centers from the current official field CAD and update the four constants. The auto follows the inset perimeter waypoints toward the selected Flower, then makes a short approach toward the wall. It doesn't plan around another robot, so test each Flower route separately.

## The mechanism is timed

`ScoringMechanism.java` runs this order:

`INTAKING → TRANSFERRING → READY_TO_SCORE → OUTTAKING → CLEARING → INTAKE_OFF`

Its motor powers and times are near the top of the file. Tune one stage at a time: intake first, transfer second, then outtake. Both outtake motors currently run together. There are no sensors to confirm a Pollen was collected or landed in a Cell, so completion of a timed stage is only an assumption.

## Official references

- [Current BIOBUZZ game materials and manual](https://ftc-resources.firstinspires.org/ftc/game) — use the newest manual/team update available at your event. The field is nominally 144 × 144 inches, there are four Flowers, and the red and blue Loading Zones are on opposite side walls.
- [Official field CAD and setup guides](https://ftc-resources.firstinspires.org/ftc/field) — use these for the exact Flower, Cell, tag, and Loading Zone coordinates.
- [PedroPathing Pinpoint tuning](https://pedropathing.com/docs/pathing/tuning/localization/pinpoint)
- [PedroPathing Foresight tuning](https://pedropathing.com/docs/pathing/tuning/foresight)
