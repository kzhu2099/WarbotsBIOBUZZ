# Warbots BIOBUZZ

Robot code for the 2026–27 FTC season. This Android Studio project includes the FTC Robot Controller app; the team's OpModes and robot settings are in TeamCode.

## Start here

Open the repository root in Android Studio and let Gradle sync. Before running an OpMode, check the device names on the Driver Station Hardware page against the names in the files below.

To build the app from the repository root:

    ./gradlew --no-daemon :TeamCode:assembleDebug

The same build runs for pull requests through GitHub Actions.

## OpModes

| Driver Station name | What it is for |
|---|---|
| TeleOp - Manual (No Pedro) | Drive, intake, transfer, and shoot without starting Pedro, Pinpoint, or a camera |
| TeleOp - Pedro + Pinpoint | Manual drive through Pedro with Pinpoint odometry; needs this robot's tuned settings |
| TeleOp - No Shooting | Manual drive and intake; the outtakes stay at zero |
| Auto - Forward Test | A short timed drive check, not a scoring autonomous |
| Auto - Path Test (No Mechanisms) | Follows the test route and pauses at the Hive and Flower; no mechanism actions |
| Motor - Individual Test | Checks one selected motor at a time |
| Camera - AprilTag Test | Opens Webcam 2 and reports AprilTag detections |

The camera test is separate. The manual TeleOps do not need a webcam. Use TeleOp - Manual (No Pedro) when Pedro or Pinpoint has not been set up yet.

## Where settings live

- **RobotConfig.java:** intake, transfer, and outtake names; motor directions; powers and times; robot dimensions and path-test coordinates.
- **pedro/Constants.java:** drive motor names and directions, Pinpoint settings, Foresight settings, and the flags that allow Pedro to run.
- **pedro/Tuning.java:** the Pedro procedures available through AutoTune.

Transfer is optional. Leave its name blank until the motor is installed and mapped on the Driver Station.

## Before using the robot

Check motor names and directions with the robot raised. Start with the individual motor test at low power, then use the forward test and manual TeleOp. Run the Pedro tuners before using the Pedro TeleOp or path test. Measure the robot and check the path waypoints against the current field CAD before running the path test.

The mechanism presets use timers. There are no sensors confirming that a game piece was collected, transferred, or scored. A successful Android build only confirms that the project compiles; it does not check wiring, calibration, or movement.

## Guides

- [Robot tests and controls](docs/ROBOT_TESTS.md)
- [Pedro setup and path test](docs/PEDRO_AND_ROBOT_GUIDE.md)
- [FTC documentation](https://ftc-docs.firstinspires.org/)
- [PedroPathing documentation](https://pedropathing.com/docs/pathing/)

This project is based on the official [FTC Robot Controller](https://github.com/FIRST-Tech-Challenge/FtcRobotController) project. See that repository for SDK release notes.
