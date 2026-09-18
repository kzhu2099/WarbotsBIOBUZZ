package org.firstinspires.ftc.teamcode;

import java.util.function.Supplier;

public interface Step {

    default void start() {}

    boolean isDone();

    default void stop() {}

    default String name() {
        return "step";
    }

    static Step named(String label, Step step) {
        return new Step() {
            public void start() {
                step.start();
            }

            public boolean isDone() {
                return step.isDone();
            }

            public void stop() {
                step.stop();
            }

            public String name() {
                return label;
            }
        };
    }

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

    static Step pause(double seconds) {
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

    static Step parallel(Step... steps) {
        return new Step() {
            public void start() {
                for (Step s : steps) {
                    s.start();
                }
            }

            public boolean isDone() {
                for (Step s : steps) {
                    if (!s.isDone()) return false;
                }
                return true;
            }

            public void stop() {
                for (Step s : steps) {
                    s.stop();
                }
            }
        };
    }

    static Step race(Step... steps) {
        return new Step() {
            public void start() {
                for (Step s : steps) {
                    s.start();
                }
            }

            public boolean isDone() {
                for (Step s : steps) {
                    if (s.isDone()) return true;
                }
                return false;
            }

            public void stop() {
                for (Step s : steps) {
                    s.stop();
                }
            }
        };
    }
}