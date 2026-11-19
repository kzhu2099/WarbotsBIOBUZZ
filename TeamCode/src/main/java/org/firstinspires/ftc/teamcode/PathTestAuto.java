package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Drive and odometry test for the complete BIOBUZZ two-lane route.
 * Shooting is represented by a timed stop; no scoring motors are initialized.
 */
@Autonomous(name = "TEST - Two Lane Flower Path", group = "BIOBUZZ TEST")
public class PathTestAuto extends BioBuzzPathAuto {
    @Override
    protected void initializeScoringHardware() {
        // Intentionally no shooter, intake, transfer, or camera hardware.
    }

    @Override
    protected void beginShooting() {
        // The base class holds the robot at the shooting pose for AUTO_SHOOT_MS.
    }

    @Override
    protected void endShooting() {
        // No scoring hardware to stop.
    }

    @Override
    protected String shootingMode() {
        return "SIMULATED - drive motors + Pinpoint only";
    }
}
