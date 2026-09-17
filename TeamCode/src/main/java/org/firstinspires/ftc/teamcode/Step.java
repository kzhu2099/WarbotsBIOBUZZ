package org.firstinspires.ftc.teamcode;

import java.util.function.Supplier;

public interface Step {

    default void start() {}

    boolean isDone();

    default void stop() {}

    static Step run(Runnable action) {
        return new Step() {
            public void start() {
                action.run();
            }

            public boolean isDone() {
                return true;
            }
        };
    }

    static Step wait(double seconds) {
        return timed(seconds, () -> {});
    }

    static Step timed(double seconds, Runnable action) {
        return new Step() {
            long startTime;

            public void start() {
                action.run();
                startTime = System.nanoTime();
            }

            public boolean isDone() {
                return (System.nanoTime() - startTime) / 1e9 >= seconds;
            }
        };
    }

    static Step branch(Supplier<Boolean> condition, Step ifTrue, Step ifFalse) {
        return new Step() {
            Step chosen;

            public void start() {
                chosen = condition.get() ? ifTrue : ifFalse;
                chosen.start();
            }

            public boolean isDone() {
                return chosen.isDone();
            }

            public void stop() {
                chosen.stop();
            }
        };
    }

    static Step until(Supplier<Boolean> condition) {
        return new Step() {
            public boolean isDone() {
                return condition.get();
            }
        };
    }
}