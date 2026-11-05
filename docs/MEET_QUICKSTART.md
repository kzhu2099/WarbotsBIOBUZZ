# Meet quick start

Use this when you need to make a small, controlled code change between practice matches.

## Before changing anything

1. Stop the robot and disconnect it from the field.
2. Open the project in Android Studio.
3. Check the branch and working tree:

   ```bash
   git status
   git branch --show-current
   ```

4. Keep the match version safe. Make changes on a separate branch, not directly on `master`.

If you have edits you have not committed, do not pull over them. Commit them or make a copy first.

## Where to make the change

| What needs changing | File |
|---|---|
| A motor's Driver Station name or direction | `pedro/Constants.java` for drive; `RobotConfig.java` for mechanisms |
| Intake, transfer, or outtake power/time | `RobotConfig.java` |
| Robot length or width, lane position, Hive approach, Flower standoff | `FieldConfig.java` |
| Order of the path test's stops | `PathTestAuto.java` |
| Pedro tuning | Run AutoTune, then copy its output into `pedro/Constants.java` |

Do not change path code just to correct a measured coordinate. Do not change several motor directions or distances at once.

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
6. On the Driver Station, select the test OpMode again and read its telemetry before pressing Play.

## Test in this order

1. Raise the robot. Use **TEST - Individual Motor** and confirm the physical wheel or mechanism named on the screen is the only one moving.
2. Use **Auto - Forward Test** at the configured low power and short duration.
3. Use **TeleOp - Manual (No Pedro)** to check normal driving.
4. Use **TeleOp - Pedro + Pinpoint** only after all three Pedro configs are tuned.
5. Use **TEST - Pedro Path (No Mechanisms)** only after the field values in `FieldConfig.java` are measured and the robot is placed at the displayed start pose.

Keep hands, cables, and game pieces clear of moving parts. Start with low power. Be ready to stop the OpMode immediately if the robot moves in an unexpected direction.

## Record the result

Write down the value you changed, the old and new values, and what the robot did. Commit a working change with a short message before making the next one. If a change makes the robot worse, revert that one change instead of stacking another guess on top of it.
