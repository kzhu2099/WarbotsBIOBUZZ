package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/** TeleOp 2 plus mechanisms on gamepad 2 (controls listed in TeleOp2Odometry). */
@TeleOp(name = "3 Full robot", group = "Match")
public class TeleOp3Full extends TeleOp2Odometry {
    @Override
    protected boolean useMechanisms() {
        return true;
    }
}
