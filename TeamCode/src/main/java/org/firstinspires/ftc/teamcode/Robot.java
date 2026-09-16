package org.firstinspires.ftc.teamcode;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * Everything shared between TeleOp and Autonomous lives here: hardware init,
 * the Follower, driving, and telemetry. Add this season's subsystems
 * (intake, outtake, whatever the game needs) as more methods here once the
 * robot is built, the way the old Robot class held everything in one place.
 * testing: @10415warbotscoding is present; branch merge successful.
 */
public class Robot {

    public final Follower follower;

    private final Telemetry telemetry;
    private final Gamepad gamepad1;
    private final Gamepad gamepad2;

    public boolean redAlliance;
    private boolean fieldCentric = true;

    public Robot(HardwareMap hardwareMap, Telemetry telemetry, Gamepad gamepad1, Gamepad gamepad2, boolean redAlliance) {
        this.telemetry = telemetry;
        this.gamepad1 = gamepad1;
        this.gamepad2 = gamepad2;
        this.redAlliance = redAlliance;

        follower = Constants.create(hardwareMap);

        telemetry.addData("Status", "Initialized");
        telemetry.update();
    }

    // ---- Driving ----

    public void teleOpDrive() {
        follower.update();

        if (gamepad1.bWasPressed()) {
            fieldCentric = !fieldCentric;
        }

        DrivePowers powers = new DrivePowers(
                -gamepad1.left_stick_y,
                -gamepad1.left_stick_x,
                -gamepad1.right_stick_x
        );

        if (fieldCentric) {
            // Offset by 180 degrees on red so "forward" always means "away
            // from your own alliance wall", same idea as the old sign-flip.
            double allianceOffset = redAlliance ? Math.PI : 0;
            powers = ManualDrive.fieldCentric(powers, follower.pose().heading(), allianceOffset);
        }

        follower.manual(powers);
    }

    // ---- Lifecycle ----

    public void start() {
        follower.update();
    }

    public void stop() {
        follower.stop();
    }

    // ---- Telemetry ----

    public void updateTelemetry() {
        telemetry.addData("pose", follower.pose());
        telemetry.addData("field centric (b)", fieldCentric);
        telemetry.addData("alliance", redAlliance ? "RED" : "BLUE");
        telemetry.update();
    }
}