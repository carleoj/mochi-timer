package com.pomodoro.ui;

import com.pomodoro.model.AppSettings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;

/** Modal settings window. Returns the edited copy, or empty if cancelled. */
public final class SettingsDialog {
    private SettingsDialog() {}

    public static Optional<AppSettings> show(Window owner, AppSettings current) {
        Spinner<Integer> focus = spinner(1, 180, current.focusMinutes);
        Spinner<Integer> shortBreak = spinner(1, 60, current.shortBreakMinutes);
        Spinner<Integer> longBreak = spinner(1, 120, current.longBreakMinutes);
        Spinner<Integer> longAfter = spinner(1, 12, current.longBreakAfter);

        CheckBox monitor = new CheckBox("Turn off monitor during breaks");
        monitor.setSelected(current.turnOffMonitor);
        CheckBox popup = new CheckBox("Show character popup");
        popup.setSelected(current.showPopup);
        CheckBox sound = new CheckBox("Play a sound");
        sound.setSelected(current.soundEnabled);

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(12);
        addRow(grid, 0, "Focus (minutes)", focus);
        addRow(grid, 1, "Short break (minutes)", shortBreak);
        addRow(grid, 2, "Long break (minutes)", longBreak);
        addRow(grid, 3, "Long break after (sessions)", longAfter);

        VBox checks = new VBox(12, monitor, popup, sound);

        Label title = new Label("Settings");
        title.getStyleClass().add("dialog-title");

        AppSettings[] result = new AppSettings[1];
        Stage stage = new Stage();

        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("secondary-button");
        cancel.setCancelButton(true);
        cancel.setOnAction(e -> stage.close());

        Button save = new Button("Save");
        save.getStyleClass().add("primary-button");
        save.setDefaultButton(true);
        save.setOnAction(e -> {
            AppSettings s = current.copy();
            s.focusMinutes = value(focus, 1, 180);
            s.shortBreakMinutes = value(shortBreak, 1, 60);
            s.longBreakMinutes = value(longBreak, 1, 120);
            s.longBreakAfter = value(longAfter, 1, 12);
            s.turnOffMonitor = monitor.isSelected();
            s.showPopup = popup.isSelected();
            s.soundEnabled = sound.isSelected();
            result[0] = s;
            stage.close();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox buttons = new HBox(10, spacer, cancel, save);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(20, title, grid, checks, buttons);
        root.setPadding(new Insets(26));
        root.getStyleClass().add("app-root");

        Scene scene = new Scene(root);
        scene.getStylesheets().add(SettingsDialog.class.getResource("/css/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Settings");
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
        stage.showAndWait();

        return Optional.ofNullable(result[0]);
    }

    private static void addRow(GridPane grid, int row, String label, Spinner<Integer> spinner) {
        Label l = new Label(label);
        l.getStyleClass().add("field-label");
        grid.add(l, 0, row);
        grid.add(spinner, 1, row);
    }

    private static Spinner<Integer> spinner(int min, int max, int value) {
        Spinner<Integer> s = new Spinner<>(min, max, value);
        s.setEditable(true);
        s.setPrefWidth(96);
        return s;
    }

    /** Reads typed text too, so a value typed without pressing Enter is not lost. */
    private static int value(Spinner<Integer> s, int min, int max) {
        try {
            int v = Integer.parseInt(s.getEditor().getText().trim());
            return Math.max(min, Math.min(max, v));
        } catch (NumberFormatException e) {
            return s.getValue();
        }
    }
}
