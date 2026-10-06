package com.pomodoro;

import javafx.application.Application;

/** Plain launcher (does not extend Application) so the app also runs from a classpath / jpackage. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        Application.launch(PomodoroApp.class, args);
    }
}
