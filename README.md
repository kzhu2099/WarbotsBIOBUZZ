# Warbots BIOBUZZ

FTC 2026–27 robot code. The Android Studio project is at the repository root. Team OpModes and robot settings are in `TeamCode`.

## First time setup

1. Open the repository root in Android Studio.
2. Let Gradle sync finish.
3. Check the Driver Station hardware names against the constants in this project.
4. Build from the repository root:

   ```bash
   ./gradlew --no-daemon :TeamCode:assembleDebug
   ```

The same build is run for pull requests.

## Driver Station OpModes

| Name | Purpose |
|---|---|
| **TeleOp - Manual (No Pedro)** | Manual mecanum drive and mechanism controls. Does not start Pedro or Pinpoint. |
| **TeleOp - Pedro + Pinpoint** | Manual drive using Pedro and Pinpoint odometry. Requires completed tuning. |
| **TeleOp - No Shooting** | Manual drive and intake; outtakes stay at zero. |
| **Auto - Forward Test** | Short, timed drivetrain check. Not a scoring autonomous. |
| **TEST - Pedro Path (No Mechanisms)** | Tests the lane, Hive, Flower, and return route. Does not run mechanisms. |
| **TEST - Individual Motor** | Runs one selected motor at a time, at limited power. |
| **Camera - AprilTag Test** | Opens Webcam 2 and reports tag detections. |

## Files to know

| File | What belongs there |
|---|---|
| `RobotConfig.java` | Hardware names for mechanisms, motor directions, powers, and timed actions |
| `FieldConfig.java` | Robot dimensions and field measurements used by the path test |
| `pedro/Constants.java` | Drive motor names/directions and Pedro drivetrain, Pinpoint, and Foresight configs |
| `pedro/Tuning.java` | Pedro AutoTune procedures |
| `RobotHardware.java` | Drive and mechanism control |
| `PathTestAuto.java` | Named path steps and test selections |

Keep field measurements out of the mechanism settings. Keep measured hardware settings out of the path code.

## Guides

- [Meet quick start](docs/MEET_QUICKSTART.md)
- [PedroPathing 3 setup](docs/PEDROPATHING_V3.md)
- [Robot tests and controls](docs/ROBOT_TESTS.md)
- [FIRST BIOBUZZ field resources and CAD](https://ftc-resources.firstinspires.org/ftc/field)
- [FIRST FTC documentation](https://ftc-docs.firstinspires.org/)
- [PedroPathing documentation](https://pedropathing.com/docs/pathing/)

## Important

The field path is disabled until the required values in `FieldConfig.java` are measured. Do not replace them with estimates. The Pedro OpModes are disabled until the Mecanum, Pinpoint, and Foresight configurations are tuned and copied into `pedro/Constants.java`.

The mechanism presets use timers. The code does not sense whether a game piece was collected, transferred, or scored. A successful Android build only proves that the app compiles. It does not prove wiring, calibration, or field movement.
