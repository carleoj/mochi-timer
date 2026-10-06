package com.pomodoro.ui;

import com.pomodoro.controller.TimerController;
import com.pomodoro.model.TimerMode;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/** Small undecorated "Goodbye for now!" card, centered on the primary monitor. */
public final class CharacterPopup {
    private static final Duration VISIBLE = Duration.seconds(2.5);

    private CharacterPopup() {}

    /** Shows the popup, then closes it and runs onDone (which should start the break). */
    public static void show(TimerMode next, int breakSeconds, Runnable onDone) {
        CharacterView character = new CharacterView(130);

        Label title = new Label("Goodbye for now!");
        title.getStyleClass().add("popup-title");
        Label time = new Label(TimerController.format(breakSeconds));
        time.getStyleClass().add("popup-time");
        Label kind = new Label(next.label());
        kind.getStyleClass().add("popup-kind");

        VBox card = new VBox(6, character, title, time, kind);
        card.setAlignment(Pos.CENTER);
        card.setMinWidth(300);
        card.getStyleClass().add("popup-card");

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(36)); // room for the drop shadow
        root.getStyleClass().addAll("popup-root", next.styleClass());

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(CharacterPopup.class.getResource("/css/styles.css").toExternalForm());

        // Invisible owner keeps the popup out of the taskbar. It is NOT the main window, so
        // minimizing the main window does not hide the popup.
        Stage owner = new Stage(StageStyle.UTILITY);
        owner.setOpacity(0);
        owner.setWidth(1);
        owner.setHeight(1);
        owner.setX(-10_000);
        owner.setY(-10_000);
        owner.show();

        Stage stage = new Stage(StageStyle.TRANSPARENT);
        stage.initOwner(owner);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.setScene(scene);

        card.setOpacity(0);
        card.setScaleX(0.92);
        card.setScaleY(0.92);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(260), card);
        fadeIn.setToValue(1);
        ScaleTransition pop = new ScaleTransition(Duration.millis(260), card);
        pop.setToX(1);
        pop.setToY(1);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), card);
        fadeOut.setToValue(0);
        SequentialTransition sequence = new SequentialTransition(
                new ParallelTransition(fadeIn, pop), new PauseTransition(VISIBLE), fadeOut);
        sequence.setOnFinished(e -> {
            character.stop();
            stage.close();
            owner.close();
            onDone.run();
        });

        stage.setOnShown(e -> {
            Rectangle2D b = Screen.getPrimary().getBounds();
            stage.setX(b.getMinX() + (b.getWidth() - stage.getWidth()) / 2);
            stage.setY(b.getMinY() + (b.getHeight() - stage.getHeight()) / 2);
            sequence.play();
        });
        stage.show();
    }
}
