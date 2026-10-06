package com.pomodoro.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

import java.net.URL;

/**
 * The mascot. If src/main/resources/images/character.png (or .jpg/.gif) exists it is shown instead of
 * the built-in placeholder, so replacing the character is just dropping a file in.
 * (JavaFX cannot decode WebP out of the box, so export to PNG.)
 */
public final class CharacterView extends StackPane {
    private static final String[] IMAGE_FILES =
            {"/images/character.png", "/images/character.jpg", "/images/character.gif"};
    private static final double DESIGN = 160;

    private final Group art;
    private final TranslateTransition bob;
    private Group leftEye;
    private Group rightEye;
    private Timeline blink;

    public CharacterView(double size) {
        setPrefSize(size, size);
        setMinSize(size, size);
        setMaxSize(size, size);

        URL img = findImage();
        if (img != null) {
            ImageView iv = new ImageView(new Image(img.toExternalForm(), size * 2, size * 2, true, true));
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setPreserveRatio(true);
            art = new Group(iv);
        } else {
            art = buildPlaceholder();
            art.setScaleX(size / DESIGN);
            art.setScaleY(size / DESIGN);
        }
        getChildren().add(art);

        bob = new TranslateTransition(Duration.seconds(1.7), art);
        bob.setByY(-5);
        bob.setAutoReverse(true);
        bob.setCycleCount(Timeline.INDEFINITE);
        bob.setInterpolator(Interpolator.EASE_BOTH);
        bob.play();
    }

    /** Sleepy face for breaks, awake face for focus. Only affects the built-in placeholder. */
    public void setSleeping(boolean sleeping) {
        if (leftEye == null) return;
        if (sleeping) {
            blink.stop();
            leftEye.setScaleY(0.12);
            rightEye.setScaleY(0.12);
        } else {
            leftEye.setScaleY(1);
            rightEye.setScaleY(1);
            blink.play();
        }
    }

    /** Stops animations (call when the view is discarded, e.g. popup closed). */
    public void stop() {
        bob.stop();
        if (blink != null) blink.stop();
    }

    private static URL findImage() {
        for (String f : IMAGE_FILES) {
            URL u = CharacterView.class.getResource(f);
            if (u != null) return u;
        }
        return null;
    }

    private Group buildPlaceholder() {
        Color body = Color.web("#ffe3ea");
        Color earIn = Color.web("#ff9db5");
        Color ink = Color.web("#2b2342");

        Polygon earL = new Polygon(32, 62, 36, 16, 74, 42);
        Polygon earR = new Polygon(128, 62, 124, 16, 86, 42);
        earL.setFill(body);
        earR.setFill(body);
        Polygon innerL = new Polygon(43, 56, 45, 31, 62, 44);
        Polygon innerR = new Polygon(117, 56, 115, 31, 98, 44);
        innerL.setFill(earIn);
        innerR.setFill(earIn);

        Ellipse head = new Ellipse(80, 96, 62, 52);
        head.setFill(body);

        leftEye = eye(58, 94, ink);
        rightEye = eye(102, 94, ink);

        Ellipse cheekL = new Ellipse(42, 109, 10, 6);
        Ellipse cheekR = new Ellipse(118, 109, 10, 6);
        cheekL.setFill(Color.web("#ff8fa3", 0.55));
        cheekR.setFill(Color.web("#ff8fa3", 0.55));

        QuadCurve mouth = new QuadCurve(71, 107, 80, 116, 89, 107);
        mouth.setFill(Color.TRANSPARENT);
        mouth.setStroke(ink);
        mouth.setStrokeWidth(2.6);
        mouth.setStrokeLineCap(StrokeLineCap.ROUND);

        blink = new Timeline(
                frame(0, 1.0),
                frame(3.4, 1.0),
                frame(3.5, 0.1),
                frame(3.62, 1.0));
        blink.setCycleCount(Timeline.INDEFINITE);
        blink.play();

        return new Group(earL, earR, innerL, innerR, head, cheekL, cheekR, leftEye, rightEye, mouth);
    }

    private static Group eye(double cx, double cy, Color ink) {
        Ellipse e = new Ellipse(cx, cy, 6.5, 8.5);
        e.setFill(ink);
        Circle shine = new Circle(cx + 2, cy - 3, 2.2, Color.WHITE);
        return new Group(e, shine);
    }

    private KeyFrame frame(double seconds, double scaleY) {
        return new KeyFrame(Duration.seconds(seconds),
                new KeyValue(leftEye.scaleYProperty(), scaleY),
                new KeyValue(rightEye.scaleYProperty(), scaleY));
    }
}
