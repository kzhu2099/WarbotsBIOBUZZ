# PedroPathing 3 setup

This project uses PedroPathing 3.0.2 with the REV Hub drivetrain module. The team's Pedro settings and AutoTune procedures are in `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/pedro`.

## Files

| File | What belongs there |
|---|---|
| `pedro/Constants.java` | Drive motor names/directions, Pinpoint settings, Foresight settings |
| `pedro/Tuning.java` | Procedures that appear in AutoTune |
| `PathTestAuto.java` | Path-test selections and step order |
| `FieldConfig.java` | Robot dimensions and measured field coordinates |

Do not edit the copied tuning procedures just to change a motor name or offset.

## Tune in this order

1. Put the robot on blocks. Open AutoTune at `http://<Robot Controller IP>:10158`. The Pinpoint HardwareMap name defaults to `odom`, which matches this project's `Constants.java`.
2. Run **Mecanum Tuner**. Check that the wheel shown is the wheel that spins. It checks that all four HardwareMap names are present and distinct, and spins one wheel at a time at 25% power. Copy the generated motor names and directions into `Constants.java`, then set `DRIVETRAIN_CONFIG_TUNED` to `true`.
3. Run **Pinpoint Tuner**. Follow the push and rotation instructions. Copy the full generated `PinpointConfig`, including pod type or custom scalar, both offsets, both directions, and units. Then set `LOCALIZER_CONFIG_TUNED` to `true`.
4. Run **Foresight Tuner** after the drive and localizer are correct. Copy its complete generated `ForesightConfig` into `Constants.java`.
5. Run **Tests** in AutoTune. Check localization, driving, and follower behavior before using either Pedro OpMode.

Leave the readiness flags false until the corresponding tuner output is copied and checked. Pedro TeleOp and the path test will not start without all three configurations.

## How a path works

A `Pose` is the robot center in inches plus its heading in radians. The field coordinate convention is in [FIELD_PATHS.md](FIELD_PATHS.md).

A line controls the robot center's route. Interpolation controls how the robot turns while it follows the line:

```java
Path path = line(startPose, endPose).linear(startPose, endPose);
follower.follow(path);
```

Call `follower.update()` on every OpMode loop while a path is running. Do not put a long `sleep()` inside the path-following loop.

The path test uses separate orthogonal lane and approach segments. It does not connect the Hive and Flower with one diagonal shortcut. It also prints the planned route and each waypoint coordinate during INIT.

## Before the first path test

- Measure the robot length and width with bumpers.
- Measure the inside and outside lane X coordinates from the field CAD.
- Measure the red Hive cell X offset and the distance from the cell center to the robot's approach center.
- Measure the Flower standoff from the wall to the robot center.
- Measure the clearance from the robot's outside edge to the wall at the selected start.

Put these values in `FieldConfig.java`. Coordinates refer to the robot center, not the front bumper. Do not copy another team's Pinpoint offsets or assume the default motor directions are correct.

The path test calls `follower.setPose(startPose)`. That sets the software's starting estimate; it does not locate or move the robot. Place the robot at the selected start center and heading before pressing Play.

## References

- [PedroPathing 3 installation](https://pedropathing.com/docs/pathing/installation)
- [PedroPathing tuning](https://pedropathing.com/docs/pathing/tuning)
- [Creating paths](https://pedropathing.com/docs/pathing/guide/path-creation)
- [Interpolation](https://pedropathing.com/docs/pathing/reference/interpolation)
- [FIRST BIOBUZZ field resources](https://ftc-resources.firstinspires.org/ftc/archive/2027/field)


The drive mixer uses Pedro's `DrivePowers` convention: positive lateral input is the gamepad's rightward stick direction, and positive turn input is the gamepad's rightward turn stick direction. The non-Pedro manual drive and Pedro manual drive use the same wheel-power equations. Confirm the resulting physical movement with the robot raised before driving on the floor.
