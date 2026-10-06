package com.pomodoro;

import com.pomodoro.controller.TimerController;
import com.pomodoro.model.AppSettings;
import com.pomodoro.model.TimerMode;
import com.pomodoro.system.DisplayController;
import com.pomodoro.ui.CharacterPopup;
import com.pomodoro.ui.CharacterView;
import com.pomodoro.ui.SettingsDialog;
import com.pomodoro.util.SettingsManager;
import com.pomodoro.util.Sound;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.pomodoro.ui.AppIcon;

public class PomodoroApp extends Application {
    private AppSettings settings;
    private TimerController timer;
    private Stage stage;
    private Scene scene;
    private StackPane root;

    private final Label modeLabel = new Label();
    private final Label timeLabel = new Label();
    private final Label hintLabel = new Label();
    private final HBox dots = new HBox(8);
    private final Button startButton = new Button();
    private final Button resetButton = new Button("Reset");
    private CharacterView character;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        settings = SettingsManager.load();
        timer = new TimerController(settings);

        buildUi();
        wireTimer();

        stage.setTitle("MochiTimer");
        stage.getIcons().addAll(AppIcon.load());
        stage.setScene(scene);
        stage.setMinWidth(320);
        stage.setMinHeight(460);
        if (!Double.isNaN(settings.x) && !Double.isNaN(settings.y)
                && !Screen.getScreensForRectangle(settings.x, settings.y, 50, 50).isEmpty()) {
            stage.setX(settings.x);
            stage.setY(settings.y);
        }
        stage.setOnHidden(e -> Platform.exit());
        stage.show();
    }

    @Override
    public void stop() {
        saveGeometry();
        DisplayController.releaseWakeLock();
    }

    // ---- UI ----

    private void buildUi() {
        character = new CharacterView(150);
        character.setSleeping(false);

        modeLabel.getStyleClass().add("mode-pill");
        timeLabel.getStyleClass().add("time-label");
        hintLabel.getStyleClass().add("hint-label");
        dots.setAlignment(Pos.CENTER);

        startButton.getStyleClass().add("primary-button");
        startButton.setMinWidth(130);
        startButton.setOnAction(e -> timer.toggle());
        startButton.disableProperty().bind(timer.transitioningProperty());

        resetButton.getStyleClass().add("secondary-button");
        resetButton.setOnAction(e -> {
            timer.reset();
            DisplayController.releaseWakeLock();
        });

        HBox buttons = new HBox(12, startButton, resetButton);
        buttons.setAlignment(Pos.CENTER);

        VBox content = new VBox(14, character, modeLabel, timeLabel, dots, buttons, hintLabel);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24));

        Button settingsButton = new Button("\u2699");
        settingsButton.getStyleClass().add("icon-button");
        settingsButton.setTooltip(new Tooltip("Settings"));
        settingsButton.setOnAction(e -> openSettings());
        StackPane.setAlignment(settingsButton, Pos.TOP_RIGHT);
        StackPane.setMargin(settingsButton, new Insets(10));

        root = new StackPane(content, settingsButton);
        root.getStyleClass().add("app-root");

        scene = new Scene(root, settings.width, settings.height);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
    }

    private void wireTimer() {
        timer.remainingProperty().addListener((o, a, b) -> refreshTime());
        timer.modeProperty().addListener((o, a, b) -> {
            refreshMode();
            pulseTime();
        });
        timer.runningProperty().addListener((o, a, b) -> refreshButtons());
        timer.startedProperty().addListener((o, a, b) -> refreshButtons());
        timer.transitioningProperty().addListener((o, a, b) -> refreshButtons());
        timer.sessionsDoneProperty().addListener((o, a, b) -> {
            refreshDots();
            refreshButtons();
        });

        timer.setHooks(new TimerController.Hooks() {
            @Override
            public void focusStarted() {
                DisplayController.releaseWakeLock();
            }

            @Override
            public void focusEnded(TimerMode next, int breakSeconds, Runnable beginBreak) {
                if (settings.soundEnabled) Sound.chime();
                if (settings.showPopup) {
                    CharacterPopup.show(next, breakSeconds, beginBreak);
                } else {
                    beginBreak.run();
                }
            }

            @Override
            public void breakStarted(TimerMode mode) {
                if (!settings.turnOffMonitor) return;
                // Short delay so the monitor is not woken by a mouse movement that is still settling.
                PauseTransition delay = new PauseTransition(Duration.millis(800));
                delay.setOnFinished(e -> {
                    if (timer.modeProperty().get().isBreak() && timer.runningProperty().get()) {
                        DisplayController.turnOffMonitor();
                    }
                });
                delay.play();
            }

            @Override
            public void breakEnded() {
                if (settings.soundEnabled) Sound.chime();
                // The monitor is intentionally NOT woken here; Windows wakes it on user input.
            }
        });

        refreshTime();
        refreshMode();
        refreshDots();
        refreshButtons();
    }

    private void refreshTime() {
        String text = TimerController.format(timer.remainingProperty().get());
        timeLabel.setText(text);
        stage.setTitle(text + " \u00b7 " + timer.modeProperty().get().label());
    }

    private void refreshMode() {
        TimerMode m = timer.modeProperty().get();
        modeLabel.setText(m.label());
        root.getStyleClass().removeAll(
                TimerMode.FOCUS.styleClass(), TimerMode.SHORT_BREAK.styleClass(), TimerMode.LONG_BREAK.styleClass());
        root.getStyleClass().add(m.styleClass());
        character.setSleeping(m.isBreak());
        refreshTime();
        refreshButtons();
    }

    private void refreshButtons() {
        boolean running = timer.runningProperty().get();
        boolean started = timer.startedProperty().get();
        startButton.setText(running ? "Pause" : started ? "Resume" : "Start");

        TimerMode m = timer.modeProperty().get();
        if (started && !running && !timer.transitioningProperty().get()) {
            hintLabel.setText("Paused");
        } else if (m.isBreak()) {
            hintLabel.setText("Break remaining");
        } else {
            int n = settings.longBreakAfter;
            hintLabel.setText("Session " + Math.min(timer.sessionsDoneProperty().get() + 1, n) + " of " + n);
        }
    }

    private void refreshDots() {
        dots.getChildren().clear();
        int done = timer.sessionsDoneProperty().get();
        for (int i = 0; i < settings.longBreakAfter; i++) {
            Circle c = new Circle(5);
            c.getStyleClass().add("dot");
            if (i < done) c.getStyleClass().add("dot-on");
            dots.getChildren().add(c);
        }
    }

    private void pulseTime() {
        ScaleTransition st = new ScaleTransition(Duration.millis(300), timeLabel);
        st.setFromX(1.1);
        st.setFromY(1.1);
        st.setToX(1);
        st.setToY(1);
        st.play();
    }

    private void openSettings() {
        SettingsDialog.show(stage, settings).ifPresent(updated -> {
            settings.copySettingsFrom(updated);
            SettingsManager.save(settings);
            timer.applySettings();
            refreshDots();
            refreshButtons();
        });
    }

    private void saveGeometry() {
        if (stage == null || scene == null || stage.isIconified() || stage.isMaximized()) return;
        settings.x = stage.getX();
        settings.y = stage.getY();
        settings.width = scene.getWidth();
        settings.height = scene.getHeight();
        SettingsManager.save(settings);
    }
}
