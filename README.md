# Warbots BIOBUZZ

2026-2027 robot code, with some instructions to add it to your own Android Studio if you aren't working in the lab.

## Setup Reminders

1. Clone the repository 
2. Let Gradle sync finish
3. Check to make sure the code is the right version & your physical driver station has the correct configuration
4. Connect ADB and the robot wifi

## Driver Station OpModes

If you need to select one to test:

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

| File | Small summary|
|---|---|
| `RobotConfig.java` | Mechanism names, directions, powers, and timed actions |
| `FieldConfig.java` | Robot dimensions and measured field coordinates |
| `pedro/Constants.java` | Drive names/directions and Pedro, Pinpoint, and Foresight settings |
| `pedro/Tuning.java` | Pedro AutoTune procedures |
| `RobotHardware.java` | Drive and mechanism control |
| `PathTestAuto.java` | Test selections and named path steps |
The path test will not start until the robot dimensions and required measurements in `FieldConfig.java` are set. Do not replace them with estimates. Read [FIELD_PATHS.md](docs/FIELD_PATHS.md) before entering coordinates.

The mechanism presets use timers. The code cannot tell whether a game piece was collected, transferred, or scored. A successful Android build proves that the app compiles; it does not prove wiring, calibration, or field movement.
