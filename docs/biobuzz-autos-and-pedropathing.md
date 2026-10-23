# BIOBUZZ autos and PedroPathing setup

## New autonomous modes

- **BIOBUZZ - Leave Wall** initializes a loading-zone pose, drives 8 inches inward, and stops. The aim is to end contact with the perimeter wall. Confirm the robot's actual bumper and wheelbase before running it at an event.
- **BIOBUZZ - Shoot, Collect 1 Pollen, Shoot** feeds one preload through the existing intake/transfer mechanism, drives to the alliance-side perimeter Flower, intakes one Pollen, returns to the currently selected upward-facing alliance Cell, aims, and shoots. It stops if a stage times out, the active Cell changes repeatedly, or the 28-second safety cutoff is reached.

These modes use the existing mechanism state machine. The robot has no beam-break, distance, or other mechanism sensor, so intake, transfer, and outtake completion are timed assumptions. Software cannot verify that Pollen was physically collected or that a shot landed.

## How PedroPathing is used here

Robot builds a Pedro Follower in the Pedro constants file. Each active loop calls Robot.updateWorldModel(), which calls follower.update() to refresh odometry and advance path following. A target is a Pose(x, y, heading); this project expresses position in inches and heading in radians. The code builds line paths with Paths.line(start, target).constant(target.heading()), then passes them to follower.follow(...). Movement is asynchronous: the OpMode keeps looping and checks follower.isBusy() instead of blocking while the robot moves. follower.hold(pose) asks the controller to maintain a pose and heading.

The X/Y coordinates are for the localizer's field reference point, not a robot corner. They correspond to the chassis center only if the Pinpoint offsets are calibrated to that reference. Setting a starting Pose does not move or detect the robot; it overwrites the software's position estimate.

The loading-zone seed places Blue near the right alliance wall at heading 180 degrees. Red is reflected across the field's vertical centerline at the same Y and heading 0 degrees. **VERIFY THESE VALUES** against the official field CAD and the event field before using them in a match.

## Selecting and aiming at the Cell

Gamepad 1 **Y** is the hold-to-aim control in TeleOp. While Y is held, joystick translation is suppressed and the robot rotates toward the currently selected upward-facing Cell for its alliance.

The official manual places each AprilTag cluster on the Cell's bottom face, pointing down toward the tiles. From a camera below the Hive, that tag identifies the Cell whose opening is facing upward, so CellMap targets the observed Cell directly. If no alliance-cell tag has been observed, the code uses nominal coordinates and picks the nearer estimate; that fallback cannot confirm the Hive's current tipped state. Telemetry displays the selected Cell and tracking state.

The official separation between the two Cells in a Hive is 18.8 inches. The nominal positions in FieldTagLibrary are estimates rather than published exact Cell-center coordinates. The offset between a tag cluster and the Cell center must be confirmed using field CAD and physical observations.

## VERIFY THESE VALUES before a powered test

1. **Foresight is not configured in the current repository.** Constants.foresightConfig is null and Constants.create() intentionally throws. The two new modes display that blocker instead of moving with invented tuning values. The branch registers the official Pedro Mecanum, Pinpoint, and Foresight tuner procedures in pedro/Tuning.java. Follow the [PedroPathing Foresight tuning guide](https://pedropathing.com/docs/pathing/tuning/foresight), verify motor directions and localization first, then complete the Foresight procedure on the real robot and copy its generated ForesightConfig into Constants.java. The config cannot be filled in correctly without measuring your physical robot.
2. **Starting pose:** the 144-inch field size is the official nominal field dimension, but check the actual field origin/orientation, loading-zone Y, and the robot's starting wall contact. The current loading-zone Y is an estimate.
3. **Robot geometry:** WIDTH_INCHES, LENGTH_INCHES, intake extension, motor directions, Pinpoint X/Y pod offsets and directions, and the upward camera mount position/angles must be measured or tested on the assembled robot.
4. **Flower approach:** the Flower is on the perimeter. The current routine estimates its center at Y = 72 inches and the robot center 15 inches from the side wall. Verify the retrieval opening, intake direction, and actual intake reach before driving up to the wall.
5. **Hive / Cell poses:** the official spacing is 18.8 inches between a Hive's two Cells, but the X-center offsets in FieldTagLibrary are estimated. Verify tag IDs, tag mounting orientation, tag-to-Cell-center offsets, and whether field Y increases away from the audience.
6. **Scoring distances and times:** approach standoff 24 inches, scoring standoff 16 inches, intake 2.5 seconds, transfer 1.5 seconds, outtake 1.0 second, clearing delay, and aim timeout are robot-specific tuning values, not official field dimensions. Tune them using repeated, supervised tests with the actual shooter and game pieces.
7. **Autonomous timing:** the second routine has a 28-second cutoff, but that does not guarantee both shots will finish in time. Test each driving and mechanism stage under the 30-second autonomous period.
8. **Drive directions and paths:** first verify forward, backward, strafe, and rotation directions at low risk. Then test path following at low speed in an empty field, with an emergency-stop operator present.

## Official references and measured game values

The current official manual (TU06) specifies a nominal 144 in by 144 in FIELD; two CELLS in each HIVE approximately 18.8 in apart; POLLEN nominally 2.8 in in diameter and NECTAR nominally 3.6 in; and a FLOWER POLLEN opening approximately 2.79 in across. The AprilTag clusters are on the bottom of each CELL, face down toward the tiles, and are grouped 30-33 (red, opposite the audience), 34-37 (red, audience side), 38-41 (blue, audience side), and 42-45 (blue, opposite the audience). The autonomous period is 30 seconds. The exact field setup tolerances and scoring rules remain the manual's authority.

- [2026-2027 BIOBUZZ Competition Manual, TU06](https://ftc-resources.firstinspires.org/ftc/game/cm-html/BIOBUZZ%20Competition%20Manual%20-%20TU06.htm): Sections 9.1, 9.6, 9.7, 9.8, 9.9, and 10.4 cover these values and geometry.
- [PedroPathing: Foresight tuning](https://pedropathing.com/docs/pathing/tuning/foresight): official procedure for measuring the robot-specific Foresight configuration.
