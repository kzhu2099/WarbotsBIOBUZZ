# Field coordinates and path layout

This file explains where the test poses are and what the robot is expected to do. The path test is a drivetrain test, not a scoring autonomous.

## Field coordinates

The field is 144 by 144 inches.

- `X = 0`: Red wall.
- `X = 144`: Blue wall.
- `Y = 0`: Audience wall.
- `Y = 144`: Scoring wall.
- `(72, 72)`: Field center.

Red is on the left side of the field. Blue is on the right. X increases from Red to Blue; Y increases from Audience to Scoring. A heading of 0 points toward +X. Headings are in radians in code and shown in degrees on the INIT screen.

FIRST's field setup guide places one Flower at the midpoint of each perimeter wall. The code uses the field midpoint for the Flower's coordinate along that wall. The actual robot stopping distance from the wall is a separate measured value.

## What the named poses mean

| Pose | Meaning |
|---|---|
| **Start robot center** | Center of the chassis at the selected start position. |
| **Lane entry** | Point where the robot reaches the selected lane from the start. |
| **Lane at Hive row** | Same lane X coordinate at the selected Hive cell's Y coordinate. |
| **Hive cell center** | Center of the selected cell in the Red or Blue Hive. |
| **Hive approach** | Robot-center position outside the frame, facing the selected cell. |
| **Hive bypass** | Position outside the frame on the Audience or Scoring side. Only used for an opposite-wall Flower route. |
| **Flower approach** | Robot-center position at the measured standoff from the selected Flower. |

Inside and Outside are two X centerlines on the alliance half of the field. The values are measured from the Red wall and reflected across the field center for Blue. The **inside lane is the one closer to the Hive/frame** (larger Red-side X); the **outside lane is closer to the alliance wall** (smaller Red-side X). Both lanes must fit the robot between the wall and the frame. These are not arbitrary target Y values: the robot follows a fixed-X lane between the entry and Hive row.

## Start selections

The test has three start choices:

- **SIDE_WALL_CENTER** puts the robot halfway along the Red or Blue alliance wall.
- **AUDIENCE_CORNER** puts it near the Audience-side end of that alliance wall.
- **SCORING_CORNER** puts it near the Scoring-side end.

All three start poses face toward the field center. The configured wall clearance is measured from the outside of the robot, including bumpers, to the wall.

## Route shape

The robot center follows axis-aligned segments. A lane run keeps X fixed and changes Y. A cross-field segment keeps Y fixed and changes X.

1. Move from the selected start to the selected lane.
2. Follow the lane to the selected Hive cell's row.
3. Move across to the Hive approach point and pause.
4. Back out to the lane.
5. Follow the lane to the Flower row.
6. Approach the Flower and pause.
7. Return along the same route to the Hive row.
8. Approach the Hive again and pause.

For a Flower on the opposite wall, the route adds three points: move to the selected Audience/Scoring-side bypass, cross behind the Hive frame, then follow the far-side lane to the Flower row. It does not drive through the frame.

The INIT screen shows every step and its target coordinate. Read the list before pressing Play. If the planned line does not match the field in front of you, do not run it.

## Measurements required before the test will start

Open `FieldConfig.java` and enter the following values from the assembled robot and the current field CAD:

| Setting | How to measure it |
|---|---|
| `ROBOT_WIDTH_INCHES` | Outside-to-outside width including bumpers |
| `ROBOT_LENGTH_INCHES` | Front-to-back length including bumpers |
| `START_WALL_CLEARANCE_INCHES` | Gap from the robot's outside edge, including bumpers, to the wall at the selected start |
| `HIVE_CELL_X_OFFSET_INCHES` | X distance from field center to the center of a Hive cell |
| `HIVE_APPROACH_INCHES` | X distance from the cell center to the robot center at approach |
| `INSIDE_LANE_X_INCHES` | Red wall to robot-center X coordinate for the inside lane |
| `OUTSIDE_LANE_X_INCHES` | Red wall to robot-center X coordinate for the outside lane |
| `FLOWER_STANDOFF_INCHES` | Perimeter wall to robot-center distance at the Flower |

All robot dimensions include bumpers. Lane, Hive, and Flower coordinates refer to the robot center. The code checks the robot outline against the field perimeter and the nominal Hive frame before allowing the path to start.

The code uses nominal frame dimensions and a clearance margin. The field guide notes that some field dimensions can vary between events. A passing geometry check does not guarantee a collision-free path on a particular field, especially when localization or follower error is present. Start slowly, keep an operator on the stop control, and watch the robot throughout the test.

## What the software does not know

`follower.setPose(startPose)` tells Pedro where the robot is assumed to be. It does not measure the robot's actual position. Put the robot at the displayed center and heading before pressing Play.

The test does not operate the intake, transfer, outtakes, or camera. It does not confirm game-piece collection or scoring. Tune Mecanum, Pinpoint, and Foresight first.
