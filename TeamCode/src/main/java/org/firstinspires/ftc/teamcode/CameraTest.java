package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Camera - Visibility Test", group = "BIOBUZZ Tests")
public class CameraTest extends OpMode {
    private CameraDiagnostics camera;

    @Override
    public void init() {
        camera = new CameraDiagnostics(hardwareMap);
    }

    @Override
    public void init_loop() {
        camera.addTelemetry(telemetry);
        telemetry.update();
    }

    @Override
    public void loop() {
        camera.addTelemetry(telemetry);
        telemetry.update();
    }

    @Override
    public void stop() {
        if (camera != null) camera.close();
    }
}
