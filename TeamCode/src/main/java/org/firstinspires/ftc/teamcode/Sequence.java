package org.firstinspires.ftc.teamcode;

public class Sequence {

    private final Step[] steps;
    private int index = 0;
    private boolean started = false;

    public Sequence(Step... steps) {
        this.steps = steps;
    }

    public void update() {
        if (isDone()) return;

        Step current = steps[index];

        if (!started) {
            current.start();
            started = true;
        }

        if (current.isDone()) {
            current.stop();
            index++;
            started = false;
        }
    }

    public boolean isDone() {
        return index >= steps.length;
    }

    public int index() {
        return index;
    }
}