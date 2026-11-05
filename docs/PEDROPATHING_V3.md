# PedroPathing 3 setup

This project uses PedroPathing 3.0.2 with the REV Hub drivetrain module. The Pedro code is in `teamcode/pedro`.

## What to edit

| File | Edit it for |
|---|---|
| `pedro/Constants.java` | Drive motor names and directions, Pinpoint settings, and Foresight settings |
| `pedro/Tuning.java` | Which tuning procedures appear in AutoTune |
| `PathTestAuto.java` | Test route and the order of its named waypoints |
| `FieldConfig.java` | Robot size and measured field coordinates |

Do not edit the copied tuner procedures just to change a motor name or offset.

## Tune in this order

1. Put the robot on blocks. Open the Robot Controller's AutoTune page at `http://<Robot Controller IP>:10158`.
2. Run **Mecanum Tuner**. Check that the displayed wheel is the wheel that spins. Copy the generated names and directions into `Constants.java`, then set `DRIVETRAIN_CONFIG_TUNED` to `true`.
3. Run **Pinpoint Tuner**. Follow its push and rotation instructions. Copy the complete generated `PinpointConfig` into `Constants.java`, including pod type or custom scalar, both offsets, both directions, and units. Then set `LOCALIZER_CONFIG_TUNED` to `true`.
4. Run **Foresight Tuner** only after the drive and localizer are correct. Copy its complete generated `ForesightConfig` into `Constants.java`.
5. Run **Tests** in AutoTune. Check localization, driving, and follower behavior before using either Pedro OpMode.

The two readiness flags must stay false until their tuner output is copied and checked. The Pedro TeleOp and path test will refuse to start without all three configurations.

## How a path works in Pedro 3

A `Pose` is the robot center in inches plus its heading in radians. With this project's coordinate system, X goes right, Y goes from the audience side toward the field side, and heading 0 points toward +X.

A line controls the robot center's route. Interpolation controls how the robot turns while following that line:

```java
Path path = line(startPose, endPose).linear(startPose, endPose);
follower.follow(path);
```

Call `follower.update()` on every OpMode loop while the path is running. Do not put a long `sleep()` inside the path-following loop.

The test route uses separate named steps rather than one long diagonal. It enters the selected lane, follows that lane to the Hive row, approaches the chosen Hive cell, travels to the Flower, and returns to the Hive. When the selected Flower is on the opposite wall, it crosses the field outside the Hive frame.

## Before the first path test

- Measure the robot length and width with bumpers.
- Measure the inside and outside lane X coordinates from the field CAD.
- Measure the red Hive cell X offset and the distance from the Hive cell to the robot's approach center.
- Measure the Flower standoff from the wall to the robot center.
- Check the corner inset if using a corner start.

Put those values in `FieldConfig.java`. Coordinates refer to the robot center, not its front bumper. Do not copy another team's Pinpoint offsets or assume the default motor directions are correct.

The path test calls `follower.setPose(startPose)`. That sets the software's starting estimate; it does not locate or move the robot. Place the robot at the selected start center and heading before pressing Play.

## References

- [PedroPathing 3 installation](https://pedropathing.com/docs/pathing/installation)
- [PedroPathing tuning](https://pedropathing.com/docs/pathing/tuning)
- [Creating paths](https://pedropathing.com/docs/pathing/guide/path-creation)
- [Interpolation](https://pedropathing.com/docs/pathing/reference/interpolation)
- [FTC BIOBUZZ field resources and CAD](https://ftc-resources.firstinspires.org/ftc/field)
