package com.pomodoro.controller;

import com.pomodoro.model.AppSettings;
import com.pomodoro.model.TimerMode;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;

/**
 * Pomodoro state machine. Runs entirely on the JavaFX thread using a Timeline (no Thread.sleep).
 * The remaining time is derived from System.nanoTime() on every tick, so it never drifts.
 */
public final class TimerController {

    /** Lets the UI react to phase changes. All callbacks run on the JavaFX thread. */
    public interface Hooks {
        default void focusStarted() {}

        /** Focus hit 00:00. Call beginBreak when ready (e.g. after a popup); the break then starts. */
        default void focusEnded(TimerMode nextBreak, int breakSeconds, Runnable beginBreak) {
            beginBreak.run();
        }

        default void breakStarted(TimerMode mode) {}

        default void breakEnded() {}
    }

    private final AppSettings settings;
    private final ObjectProperty<TimerMode> mode = new SimpleObjectProperty<>(TimerMode.FOCUS);
    private final IntegerProperty remaining = new SimpleIntegerProperty();
    private final IntegerProperty sessionsDone = new SimpleIntegerProperty();
    private final BooleanProperty running = new SimpleBooleanProperty();
    private final BooleanProperty started = new SimpleBooleanProperty();
    private final BooleanProperty transitioning = new SimpleBooleanProperty();

    private final Timeline ticker = new Timeline(new KeyFrame(Duration.millis(200), e -> tick()));
    private Hooks hooks = new Hooks() {};
    private long endNanos;
    private int generation;

    public TimerController(AppSettings settings) {
        this.settings = settings;
        ticker.setCycleCount(Timeline.INDEFINITE);
        remaining.set(seconds(TimerMode.FOCUS));
    }

    // ---- controls ----

    public void setHooks(Hooks hooks) {
        this.hooks = hooks;
    }

    public void start() {
        if (started.get() || transitioning.get()) return;
        started.set(true);
        beginFocus();
    }

    public void pause() {
        if (!running.get()) return;
        remaining.set(Math.max(1, secondsLeft()));
        ticker.stop();
        running.set(false);
    }

    public void resume() {
        if (!started.get() || running.get() || transitioning.get()) return;
        runPhase();
    }

    /** Start / Pause / Resume depending on state. */
    public void toggle() {
        if (transitioning.get()) return;
        if (running.get()) pause();
        else if (started.get()) resume();
        else start();
    }

    public void reset() {
        generation++; // cancels any pending "begin break" callback
        ticker.stop();
        running.set(false);
        started.set(false);
        transitioning.set(false);
        sessionsDone.set(0);
        mode.set(TimerMode.FOCUS);
        remaining.set(seconds(TimerMode.FOCUS));
    }

    /** Call after settings changed. An idle timer picks up the new focus length right away. */
    public void applySettings() {
        if (!started.get()) remaining.set(seconds(TimerMode.FOCUS));
    }

    // ---- internals ----

    private void beginFocus() {
        mode.set(TimerMode.FOCUS);
        remaining.set(seconds(TimerMode.FOCUS));
        runPhase();
        hooks.focusStarted();
    }

    private void runPhase() {
        endNanos = System.nanoTime() + remaining.get() * 1_000_000_000L;
        running.set(true);
        ticker.play();
    }

    private void tick() {
        int left = secondsLeft();
        if (left > 0) {
            remaining.set(left);
            return;
        }
        remaining.set(0);
        phaseEnded();
    }

    private void phaseEnded() {
        ticker.stop();
        running.set(false);
        if (mode.get() == TimerMode.FOCUS) {
            sessionsDone.set(sessionsDone.get() + 1);
            TimerMode next = sessionsDone.get() >= settings.longBreakAfter
                    ? TimerMode.LONG_BREAK : TimerMode.SHORT_BREAK;
            mode.set(next);
            remaining.set(seconds(next));
            transitioning.set(true);
            int gen = generation;
            hooks.focusEnded(next, seconds(next), () -> {
                if (gen != generation || !transitioning.get()) return;
                transitioning.set(false);
                runPhase();
                hooks.breakStarted(next);
            });
        } else {
            boolean wasLong = mode.get() == TimerMode.LONG_BREAK;
            hooks.breakEnded();
            if (wasLong) sessionsDone.set(0);
            beginFocus();
        }
    }

    private int secondsLeft() {
        return (int) Math.ceil((endNanos - System.nanoTime()) / 1_000_000_000.0);
    }

    private int seconds(TimerMode m) {
        return 60 * switch (m) {
            case FOCUS -> settings.focusMinutes;
            case SHORT_BREAK -> settings.shortBreakMinutes;
            case LONG_BREAK -> settings.longBreakMinutes;
        };
    }

    public static String format(int totalSeconds) {
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    // ---- observable state ----

    public ObjectProperty<TimerMode> modeProperty() { return mode; }
    public IntegerProperty remainingProperty() { return remaining; }
    public IntegerProperty sessionsDoneProperty() { return sessionsDone; }
    public BooleanProperty runningProperty() { return running; }
    public BooleanProperty startedProperty() { return started; }
    public BooleanProperty transitioningProperty() { return transitioning; }
}
