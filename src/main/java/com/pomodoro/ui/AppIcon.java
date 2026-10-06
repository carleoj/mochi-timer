package com.pomodoro.ui;

import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Window / taskbar icon. Uses src/main/resources/images/app-icon.png if it exists
 */
public final class AppIcon {
    private AppIcon() {} 

    public static List<Image> load() {
        URL url = AppIcon.class.getResource("/images/app-icon.png");
        if (url != null) {
            return List.of(new Image(url.toExternalForm()));
        }
        List<Image> icons = new ArrayList<>();
        for (int size : new int[] {16, 32, 48, 64, 128, 256}) {
            CharacterView view = new CharacterView(size);
            view.stop();
            StackPane box = new StackPane(view);
            box.setPrefSize(size, size);
            new Scene(box, size, size, Color.TRANSPARENT); // gives the node a size for layout
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            icons.add(box.snapshot(params, new WritableImage(size, size)));
        }
        return icons;
    }
}