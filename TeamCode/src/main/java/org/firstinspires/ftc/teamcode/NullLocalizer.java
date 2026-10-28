package org.firstinspires.ftc.teamcode;

import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Velocity;

/** A no-op localizer for manual driving tests. Its pose is not real, so do not use it for aiming or autonomous paths. */
public class NullLocalizer implements Localizer {

    private Pose pose;

    public NullLocalizer() {
        this(new Pose(0, 0, 0));
    }

    public NullLocalizer(Pose startPose) {
        this.pose = startPose;
    }

    @Override
    public void setPose(Pose pose) {
        this.pose = pose;
    }

    @Override
    public MotionState state() {
        return MotionState.ofVelocity(pose, Velocity.zero());
    }

    @Override
    public void update() {
        // No real sensor backs this localizer - nothing to update.
    }

    @Override
    public void reset() {
        // Nothing to reset.
    }
}
