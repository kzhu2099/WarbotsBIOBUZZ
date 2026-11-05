# Meet quick start

Use this for a small, controlled code change between practice matches.

## Before changing anything

1. Stop the robot and disconnect it from the field.
2. Open the project in Android Studio.
3. Check the branch and working tree:

   ```bash
   git status
   git branch --show-current
   ```

4. Keep the match version safe. Make changes on a separate branch, not directly on `master`.

Do not pull over uncommitted edits. Commit them or make a copy first.

## Where to make the change

| What needs changing | File |
|---|---|
| Drive motor name or direction | `pedro/Constants.java` |
| Intake, transfer, or outtake name/direction | `RobotConfig.java` |
| Intake, transfer, or outtake power/time | `RobotConfig.java` |
| Robot size, start wall clearance, lane position, Hive approach, Flower standoff | `FieldConfig.java` |
| Order of the path test stops | `PathTestAuto.java` |
| Pedro tuning | Run AutoTune, then copy its output into `pedro/Constants.java` |

The settings use full names such as `LEFT_OUTTAKE_MOTOR_NAME` and `INSIDE_LANE_X_INCHES`. Change the setting that describes the physical thing you are adjusting.

## Make and check the change

1. Change one value or one small behavior.
2. Save the file.
3. Review exactly what changed:

   ```bash
   git diff
   ```

4. Build the app:

   ```bash
   ./gradlew --no-daemon :TeamCode:assembleDebug
   ```

5. Install the updated Robot Controller app from Android Studio.
6. On the Driver Station, select the test OpMode again. Read its telemetry before pressing Play.

## Test in this order

1. Raise the robot. Use **TEST - Individual Motor** and confirm the named physical motor is the only one moving. The screen also shows its configured direction.
2. Use **Auto - Forward Test** at its configured low power and short duration.
3. Use **TeleOp - Manual (No Pedro)** to check normal driving.
4. Use **TeleOp - Pedro + Pinpoint** only after all three Pedro configs are tuned. The Pinpoint HardwareMap name in AutoTune should be `odom`.
5. Use **TEST - Pedro Path (No Mechanisms)** only after the field measurements are entered and the robot is placed at the displayed start pose.

Keep hands, cables, and game pieces clear. Start with low power. Stop immediately if the robot moves in an unexpected direction.

## Record the result

Write down the setting changed, the old and new values, and what the robot did. Commit a working change with a short message before changing something else. If a change makes the robot worse, revert that change instead of stacking another guess on top of it.
