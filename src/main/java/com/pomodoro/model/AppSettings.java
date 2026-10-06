package com.pomodoro.model;

/** Plain mutable settings holder. Durations are in minutes. */
public final class AppSettings {
    public int focusMinutes = 25;
    public int shortBreakMinutes = 5;
    public int longBreakMinutes = 20;
    public int longBreakAfter = 4;
    public boolean turnOffMonitor = true;
    public boolean showPopup = true;
    public boolean soundEnabled = false;

    // Remembered window geometry (NaN = unknown). Width/height are the scene size.
    public double x = Double.NaN;
    public double y = Double.NaN;
    public double width = 380;
    public double height = 560;

    public AppSettings copy() {
        AppSettings c = new AppSettings();
        c.copySettingsFrom(this);
        c.x = x;
        c.y = y;
        c.width = width;
        c.height = height;
        return c;
    }

    /** Copies only the user-facing options, not the window geometry. */
    public void copySettingsFrom(AppSettings o) {
        focusMinutes = o.focusMinutes;
        shortBreakMinutes = o.shortBreakMinutes;
        longBreakMinutes = o.longBreakMinutes;
        longBreakAfter = o.longBreakAfter;
        turnOffMonitor = o.turnOffMonitor;
        showPopup = o.showPopup;
        soundEnabled = o.soundEnabled;
    }
}
