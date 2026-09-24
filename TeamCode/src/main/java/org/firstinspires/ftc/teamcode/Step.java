package org.firstinspires.ftc.teamcode;

import java.util.function.Supplier;

/**
 * AUDITED, UNCHANGED: this class is generic (no game-specific or hardware-
 * specific logic), so it needed no rewrite - see project brief section 29.
 * It is used both by the simple Step/Sequence-based autonomous templates
 * (AutoTemplate, LeaveAndPark) and, in a much smaller way, inside Robot's
 * own step*() helpers. The dynamic autonomous system (AutonomousController)
 * does NOT build a Sequence at all - it is a real state machine that can
 * change its mind, which a fixed array of Steps fundamentally cannot do
 * (see project brief section 29's warning against a 40-step static
 * Sequence pretending to be dynamic).
 */
public interface Step {

    default void start() {}

    default void update() {}

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

            public void update() {
                step.update();
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

            public void update() {
                chosen.update();
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

            public void update() {
                for (Step s : steps) {
                    s.update();
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

            public void update() {
                for (Step s : steps) {
                    s.update();
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
