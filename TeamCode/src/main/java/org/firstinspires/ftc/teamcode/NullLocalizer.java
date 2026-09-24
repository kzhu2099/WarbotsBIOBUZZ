package org.firstinspires.ftc.teamcode;

import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Velocity;

/**
 * A {@link Localizer} that does nothing: it reports whatever pose was last
 * set with setPose() (starting at (0,0,0)) and never actually tracks robot
 * motion, because it isn't backed by any real sensor.
 *
 * WHY THIS EXISTS: PedroPathing's Follower requires a Localizer and an
 * Algorithm at construction (see pedro.Constants.create()), and this
 * project's real one (PinpointLocalizer + Foresight) needs odometry pods
 * physically wired up AND Foresight tuned before it can be built at all.
 * That is the correct, real requirement for actually following paths - but
 * it means a brand new chassis with just four drive motors on it couldn't
 * be driven with PedroPathing's own mecanum/DrivePowers code at all until
 * both of those were done. NullLocalizer, combined with a null Algorithm
 * (see OpModes.DriveOnlyTeleOp), unblocks that: a Follower built from
 * NullLocalizer + the real Mecanum drivetrain + no Algorithm can still run
 * in MANUAL mode, because {@code Follower.update()}'s MANUAL case only
 * ever calls {@code drivetrain.drive(powers, true)} - it never reads
 * localizer state or touches the algorithm at all for plain manual driving.
 *
 * DO NOT use this for anything beyond that. Its pose is not real: nothing
 * that needs to know where the robot actually is - aiming, autonomous,
 * follower.follow()/hold(), telemetry claiming to show robot position -
 * should ever be built on top of it.
 */
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
