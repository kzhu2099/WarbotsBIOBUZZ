# Pedro setup and path test

Pedro is optional for basic manual driving. TeleOp - Manual (No Pedro) does not create a Pedro follower or use Pinpoint. Use it while the Pedro settings are still being measured.

## Files to edit

| File | What belongs there |
|---|---|
| **pedro/Constants.java** | Drive names/directions, Pinpoint configuration, Foresight configuration, and calibration flags |
| **RobotConfig.java** | Mechanism settings, robot dimensions, field waypoints, and path-test timing |
| **pedro/Tuning.java** | Mecanum, Pinpoint, Foresight, and Tests procedures |

Do not copy calibration values from a sample robot. Generate them on this robot and put the complete tuner output in Constants.java.

## Tuning order

Run the AutoTune page while connected to the Robot Controller:

    http://192.168.43.1:10158

1. **Mecanum Tuner:** check the four drive motor names and directions. Copy the generated configuration into Constants.java.
2. **Pinpoint Tuner:** select the pod type and follow the offset and direction steps. Replace the whole localizer configuration with the generated result, including units and pod type.
3. **Foresight Tuner:** run this after the drivetrain and localizer are set. Copy its configuration into Constants.java.
4. **Tests:** check the localizer, drivetrain, and Follower.

The two readiness flags in Constants.java start as false, and the Foresight configuration starts empty. Leave them that way until the matching tuner output has been copied and checked. Constants.create() will refuse to create a Follower until all three settings are ready.

See the [Pedro tuning guide](https://pedropathing.com/docs/pathing/tuning), including the pages for [Mecanum](https://pedropathing.com/docs/pathing/tuning/drivetrain/mecanum), [Pinpoint](https://pedropathing.com/docs/pathing/tuning/localization/pinpoint), [Foresight](https://pedropathing.com/docs/pathing/tuning/foresight), and [Tests](https://pedropathing.com/docs/pathing/tuning/test).

## Field coordinates

PathTestAuto uses inches and radians with Pedro's field coordinate system:

- The origin is the lower-left corner.
- X increases to the right and Y increases upward.
- Headings increase counterclockwise: 0 points right, π/2 points up, and π points left.

A pose is the estimated position of the robot's center. Calling setPose() tells the localizer where the robot starts; it does not measure or move the robot. Place the robot at the selected start pose before starting the OpMode.

The three start presets are in PathTestAuto.java. The lane coordinates are entered for Red; Blue reflects X across the field centerline. Check that transform and every start pose against the official field CAD.

## Before running Path Test

The OpMode will not start until these values are set:

- Robot width and length, including bumpers
- Inside and outside lane waypoint X/Y coordinates
- Flower approach standoff

These are in RobotConfig.java. The current Hive offset, Cell separation, Hive approach distance, and Flower positions are inherited estimates. Check them against the current field CAD and measure the Flower standoff on the assembled robot.

PathTestAuto follows this route:

1. Start to the selected lane point.
2. Lane to the Hive approach, then pause.
3. Hive to the selected Flower approach, then pause.
4. Return through the lane to the Hive approach, then pause again.

The pauses stand in for the preload shot, intake, and second shot. The OpMode does not run intake, transfer, outtake, or camera code. It is a path test, not a scoring autonomous.

The follower needs to update throughout each path. Do not put a blocking sleep or long loop around path following; use the existing timed pauses between segments.
