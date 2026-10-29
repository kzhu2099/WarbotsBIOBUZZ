package org.firstinspires.ftc.teamcode.practice;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.AprilTagObservation;
import org.firstinspires.ftc.teamcode.AprilTagRole;
import org.firstinspires.ftc.teamcode.FieldTagLibrary;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

import java.util.ArrayList;
import java.util.List;

/** undefined */
public class CellVisionPractice extends CameraPractice {

    private final AprilTagProcessor tagProcessor;

    public CellVisionPractice(HardwareMap hardwareMap) {
        // TODO: implement THIS LINE (the super(...) call below is a
        // placeholder with zero processors, not a working implementation -
        // it compiles, but a camera with no processor attached will never
        // report any detections). Replace it with a real
        // AprilTagProcessor built via AprilTagProcessor.Builder(), with:
        //   - .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES) so
        //     every number downstream is a unit you can trust rather than
        //     whatever the SDK's default happens to be.
        //   - .setCameraPose(position, orientation) - see RobotGeometry's
        //     CELL_CAMERA_* constants for the numbers, and its comments
        //     for the FTC SDK's position/orientation axis convention
        //     (short version: this camera needs yaw=pitch=roll=0, since
        //     that convention's "zero rotation" literally means "camera
        //     pointing straight up").
        // Pass your built processor as the last argument to super(...),
        // then store it here via tagProcessor = processor(0);
        super(hardwareMap, "Webcam 2", new Size(320, 240));
        tagProcessor = null; // TODO: implement - see comment above
    }

    /**
     * Every AprilTag visible this frame, turned into a structured
     * AprilTagObservation. For each detection from
     * tagProcessor.getDetections() (you'll need to store tagProcessor from
     * processor(0) in the constructor, once it's actually built above):
     *   - skip it if detection.ftcPose == null (pose wasn't solvable this
     *     frame)
     *   - if it's an AprilTagSingleDetection: look up
     *     FieldTagLibrary.roleOf(single.id) for CELL_TAG/NAVIGATION_TAG/
     *     IRRELEVANT, and use single.metadata != null as a rough
     *     "is this a known tag" confidence signal
     *   - if it's an AprilTagClusterDetection: there's no simple numeric
     *     ID, only cluster.metadata.name (a String) - see CellVisionKey's
     *     comments on why this project doesn't route clusters anywhere
     *     yet, and just record the reading generically for now
     *   - either way, pull distance/bearing/x/y/yaw straight from
     *     detection.ftcPose, and - if detection.robotPose is non-null -
     *     convert it into a Pose (position .x/.y, orientation
     *     .getYaw(AngleUnit.DEGREES) in radians) to pass as the last
     *     AprilTagObservation constructor argument
     */
    public List<AprilTagObservation> observations(double nowSeconds) {
        return new ArrayList<>(); // TODO: implement
    }

    /** True if the given AprilTag ID is visible in this frame (as a single
     *  tag detection - a numeric ID isn't a meaningful question to ask of
     *  a cluster reading). */
    public boolean isTagVisible(int tagId) {
        return false; // TODO: implement
    }
}
