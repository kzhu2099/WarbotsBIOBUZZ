# Warbots BIOBUZZ

FTC 2026–27 robot code. Open the repository root in Android Studio. Team OpModes and robot settings are in `TeamCode`.

## First time setup

1. Open the repository root in Android Studio.
2. Let Gradle sync finish.
3. Check the Driver Station hardware names against this project's constants.
4. Build from the repository root:

   ```bash
   ./gradlew --no-daemon :TeamCode:assembleDebug
   ```

The same build runs for pull requests.

## Driver Station OpModes

| Name | Purpose |
|---|---|
| **TeleOp - Manual (No Pedro)** | Mecanum drive and mechanisms. Does not start Pedro or Pinpoint. |
| **TeleOp - Pedro + Pinpoint** | Manual drive using Pedro and Pinpoint. Requires completed tuning. |
| **TeleOp - No Shooting** | Drive and intake. Outtakes stay at zero. |
| **Auto - Forward Test** | Short drivetrain check. Not a scoring autonomous. |
| **TEST - Pedro Path (No Mechanisms)** | Tests the selected lane, Hive approach, Flower route, and return. |
| **TEST - Individual Motor** | Runs one named motor at a time at limited power. |
| **Camera - AprilTag Test** | Opens the configured webcam and reports tag detections. |

## Files to know

| File | What belongs there |
|---|---|
| `RobotConfig.java` | Mechanism names, directions, powers, and timed actions |
| `FieldConfig.java` | Robot dimensions and measured field coordinates |
| `pedro/Constants.java` | Drive names/directions and Pedro, Pinpoint, and Foresight settings |
| `pedro/Tuning.java` | Pedro AutoTune procedures |
| `RobotHardware.java` | Drive and mechanism control |
| `PathTestAuto.java` | Test selections and named path steps |

Keep field measurements out of the mechanism settings. Keep measured hardware settings out of the path code.

## Guides

- [Meet quick start](docs/MEET_QUICKSTART.md)
- [Field coordinates and path layout](docs/FIELD_PATHS.md)
- [PedroPathing 3 setup](docs/PEDROPATHING_V3.md)
- [Robot tests and controls](docs/ROBOT_TESTS.md)
- [FIRST BIOBUZZ field resources and CAD](https://ftc-resources.firstinspires.org/ftc/archive/2027/field)
- [FIRST FTC documentation](https://ftc-docs.firstinspires.org/)
- [PedroPathing documentation](https://pedropathing.com/docs/pathing/)

## Before using Pedro or the path test

The Pedro OpModes will not start until the Mecanum, Pinpoint, and Foresight settings have been tuned and copied into `pedro/Constants.java`.

The path test will not start until the robot dimensions and required measurements in `FieldConfig.java` are set. Do not replace them with estimates. Read [FIELD_PATHS.md](docs/FIELD_PATHS.md) before entering coordinates.

The mechanism presets use timers. The code cannot tell whether a game piece was collected, transferred, or scored. A successful Android build proves that the app compiles; it does not prove wiring, calibration, or field movement.
