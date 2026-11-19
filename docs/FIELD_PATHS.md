# BIOBUZZ field coordinates and drive-only path test

## Coordinate system

All coordinates are measured from the inside faces of the field perimeter.

- Origin (0, 0): inside corner at the Red/left wall and Audience/front wall.
- +X points right, from Red toward Blue.
- +Y points back, from Audience toward Scoring.
- Heading 0°: robot front points right / Blue.
- Heading 90°: robot front points back / Scoring.
- Heading 180°: robot front points left / Red.
- Heading 270°: robot front points toward Audience / front.

Every Pose is the robot CENTER, not the bumper, wheel axle, Flower center, or Hive Cell center. Physical field objects and robot stopping positions are separate.

## Field source and tolerance

The coordinate reference is FIRST's 2026-2027 BIOBUZZ 3D field CAD, model v26-27.2. The manual's 144 in figure is nominal. A measurement of the STEP model's inside wall faces gives 141.348 in (70.674 in from its center to each inner wall). Tile seam positions are not perfectly uniform, so the six start-row centers below are explicit CAD-derived values rather than an assumed 24 in grid.

References:
- Official manual: https://ftc-resources.firstinspires.org/ftc/game/manual
- Official field CAD and setup resources: https://ftc-resources.firstinspires.org/ftc/archive/2027/field
- Independent measurement notes for the official STEP: https://github.com/FTCCompSim/FTCCompSim.github.io/blob/main/docs/biobuzz/field-cad-audit.md
- Measured geometry constants: https://github.com/FTCCompSim/FTCCompSim.github.io/blob/main/src/games/biobuzz/fieldDims.gen.ts

FIRST identifies the 3D CAD as the field reference and warns that event fields can vary by approximately 1 in. Treat these as reference presets, not a guarantee that every event field is identical. Check the actual perimeter and Hive before driving.

## Robot footprint and exact start placement

The robot is 18 x 18 in including bumpers. Its half-width and half-length are both 9 in.

The six row centers are:

| Start row | Y center | Position |
|---|---:|---|
| 1 | 11.677 in | nearest Audience/front |
| 2 | 35.016 in | |
| 3 | 58.518 in | |
| 4 | 82.020 in | |
| 5 | 105.522 in | |
| 6 | 129.266 in | nearest Scoring/back |

The side bumper is placed flush against the inside face of the alliance wall:

| Alliance | Wall | Robot center X | Heading |
|---|---|---:|---:|
| Red | Left | 9.000 in | 0° (front right) |
| Blue | Right | 132.348 in | 180° (front left) |

For Red, put the LEFT bumper against the Red/left inside wall. For Blue, put the RIGHT bumper against the Blue/right inside wall. Align the robot center with the selected row's printed Y coordinate. The Driver Station prints the exact selected center, row, and front direction.

follower.setPose() sets Pedro's starting estimate; it does not move or locate the robot. The physical robot must already be at the displayed center and heading before Play.

## Physical Flower centers and robot stop poses

These are two different coordinate types. The physical Flower center is the ring-bore center measured from the CAD. The robot stop is the center of the 18 x 18 in chassis with the front facing the Flower.

| Flower | Physical center (x, y) | Robot stop (x, y, heading) |
|---|---:|---:|
| Red wall / left | (2.630, 47.282) | (19.000, 47.282, 180°) |
| Scoring wall / back | (47.282, 138.718) | (47.282, 122.348, 90°) |
| Blue wall / right | (138.718, 94.066) | (122.348, 94.066, 0°) |
| Audience wall / front | (94.066, 2.630) | (94.066, 19.000, 270°) |

The Flowers are NOT centered at the middle of their walls. Do not replace these four positions with a wall-midpoint formula.

## Hive Cell centers and keep-out

The four CAD-derived projected Cell centers are:

| Cell | Center (x, y) |
|---|---:|
| Red Audience | (57.924, 57.234) |
| Red Scoring | (57.924, 84.114) |
| Blue Audience | (83.424, 57.234) |
| Blue Scoring | (83.424, 84.114) |

These are physical Cell centers, not robot approach poses.

The CAD ground footprint of the Hive frame and feet is approximately:
- X: 45.939 to 95.409 in
- Y: 51.198 to 90.150 in

The path test moves only to a Hive SAFE STOP outside this footprint. It does not try to enter a Cell, aim, intake, or score.

## Two explicitly selected lane routes

Both alliances have two separate sets of literal robot-center lane presets:

| Lane | Red X | Blue X | Meaning |
|---|---:|---:|---|
| OUTSIDE | 18.000 in | 123.348 in | closer to the alliance wall |
| INSIDE | 30.000 in | 111.348 in | closer to the Hive frame |

Each lane has six explicit start-entry poses, two Hive-row poses, four Flower-row poses, and separate Audience/Scoring bypass poses. Blue is not computed by reflecting Red at runtime. The OpMode selects one of these named presets.

The route is:

1. Start -> selected lane entry.
2. Follow the lane to the selected Audience or Scoring Hive row.
3. Move to the Hive safe stop, then back out to the lane.
4. For a side-wall Flower on the opposite alliance half, take the selected Audience or Scoring bypass and cross behind the Hive.
5. Follow the selected lane to the Flower row and stop at the separate Flower robot-stop pose.
6. Return through the displayed waypoints to the selected start.

The INIT screen prints the full ordered route. Every robot waypoint is formatted as (x, y) in plus heading degrees. Physical Flower centers and Hive Cell centers are displayed separately.

The route checker verifies every straight segment against the CAD Hive footprint expanded by the 9 in robot half-size plus 2 in clearance. It also checks that the 18 x 18 in robot footprint stays inside the modeled field. This is a conservative software check, not a substitute for watching the real robot or checking the event field.

## Driver Station controls

During INIT, Gamepad 1 controls the preview:

- X: switch Red / Blue.
- D-pad left/right: select start row 1–6.
- D-pad up/down: select one of the four Flowers.
- A: switch OUTSIDE / INSIDE lane.
- B: switch Audience / Scoring bypass side.

INIT always displays robot placement, exact start pose, front direction, selected lane, physical Flower center, Flower robot stop, four Cell centers, and the complete ordered path. Hardware or route warnings do not hide the coordinates.

## First drive-only test procedure

The OpMode is TEST - Pedro Path (Drive Only). It initializes the four drive motors and Pedro follower only. It does not initialize or command the intake, transfer, outtakes, or cameras.

1. Raise the robot and verify each drive motor and wheel direction with the individual-motor test. Keep the robot on blocks during this check.
2. Verify the Pinpoint axes by moving the robot by hand in the path-test convention: X increases right toward Blue, Y increases from Audience toward Scoring, and heading increases counterclockwise. If any axis disagrees, do not run a floor path.
3. Select the alliance and row. Place the robot exactly as INIT describes. Red: left bumper flush to Red/left inner wall, center X=9.000. Blue: right bumper flush to Blue/right inner wall, center X=132.348.
4. Check the displayed front direction and the entire route. If the actual event Hive or perimeter differs from the CAD reference, or any route looks unsafe, do not press Play.
5. Have an operator ready on Stop. Start with a clear field, watch the complete route, and confirm localization at every waypoint.

This is a drive/localization/path test, not a scoring autonomous. A passing geometry check does not prove motor directions, Pinpoint offsets, Foresight response, or event-field measurements are correct.
