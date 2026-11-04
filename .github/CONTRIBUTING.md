# Working on the robot code

Use master as the shared branch. Start each change from the latest version:

    git switch master
    git pull --ff-only
    git switch -c teleop-camera-fallback

Use one branch for one change. Give it a name that says what you are working on; avoid branches like fix, fixes, or rebuild. After the pull request is merged, delete the working branch.

## Commits

Keep commits at useful checkpoints. A commit should say what changed, not just that something changed.

Good examples:
- Keep manual drive available without Pedro
- Stop the intake sequence when transfer is missing
- Add a Pinpoint path test

Avoid subjects such as "update", "misc fixes", or "final version". Don't make a commit for every small edit, but don't squash a whole piece of work into one commit when the steps are useful to understand later.

Comments should explain something a teammate cannot tell from the code: a hardware assumption, a coordinate convention, a calibration value, or a safety check. Don't add a comment just to restate the next line.

## Pull requests

Keep each pull request focused. Include the OpModes or hardware affected, the build result, and what was actually tested on the robot. Say plainly when something has only been compiled or has not been tested yet.

The Android build command is:

    ./gradlew --no-daemon :TeamCode:assembleDebug

A passing build confirms compilation. It does not confirm wiring, motor directions, odometry offsets, field coordinates, or safe robot movement.
