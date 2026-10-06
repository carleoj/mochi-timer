package com.pomodoro.util;

import com.pomodoro.model.AppSettings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Loads/saves settings from ~/.cute-pomodoro/settings.properties. */
public final class SettingsManager {
    private static final Path FILE =
            Path.of(System.getProperty("user.home"), ".cute-pomodoro", "settings.properties");

    private SettingsManager() {}

    public static AppSettings load() {
        AppSettings s = new AppSettings();
        Properties p = new Properties();
        if (Files.isRegularFile(FILE)) {
            try (InputStream in = Files.newInputStream(FILE)) {
                p.load(in);
            } catch (IOException ignored) {
                // fall back to defaults
            }
        }
        s.focusMinutes = intOf(p, "focusMinutes", s.focusMinutes, 1, 180);
        s.shortBreakMinutes = intOf(p, "shortBreakMinutes", s.shortBreakMinutes, 1, 60);
        s.longBreakMinutes = intOf(p, "longBreakMinutes", s.longBreakMinutes, 1, 120);
        s.longBreakAfter = intOf(p, "longBreakAfter", s.longBreakAfter, 1, 12);
        s.turnOffMonitor = boolOf(p, "turnOffMonitor", s.turnOffMonitor);
        s.showPopup = boolOf(p, "showPopup", s.showPopup);
        s.soundEnabled = boolOf(p, "soundEnabled", s.soundEnabled);
        s.x = dblOf(p, "windowX", Double.NaN);
        s.y = dblOf(p, "windowY", Double.NaN);
        s.width = Math.max(320, dblOf(p, "windowWidth", s.width));
        s.height = Math.max(460, dblOf(p, "windowHeight", s.height));
        return s;
    }

    public static void save(AppSettings s) {
        Properties p = new Properties();
        p.setProperty("focusMinutes", String.valueOf(s.focusMinutes));
        p.setProperty("shortBreakMinutes", String.valueOf(s.shortBreakMinutes));
        p.setProperty("longBreakMinutes", String.valueOf(s.longBreakMinutes));
        p.setProperty("longBreakAfter", String.valueOf(s.longBreakAfter));
        p.setProperty("turnOffMonitor", String.valueOf(s.turnOffMonitor));
        p.setProperty("showPopup", String.valueOf(s.showPopup));
        p.setProperty("soundEnabled", String.valueOf(s.soundEnabled));
        if (Double.isFinite(s.x) && Double.isFinite(s.y)) {
            p.setProperty("windowX", String.valueOf(s.x));
            p.setProperty("windowY", String.valueOf(s.y));
        }
        p.setProperty("windowWidth", String.valueOf(s.width));
        p.setProperty("windowHeight", String.valueOf(s.height));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                p.store(out, "Cute Pomodoro settings");
            }
        } catch (IOException e) {
            System.err.println("Could not save settings: " + e.getMessage());
        }
    }

    private static int intOf(Properties p, String key, int def, int min, int max) {
        try {
            int v = Integer.parseInt(p.getProperty(key, String.valueOf(def)).trim());
            return Math.max(min, Math.min(max, v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static double dblOf(Properties p, String key, double def) {
        try {
            return Double.parseDouble(p.getProperty(key, String.valueOf(def)).trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static boolean boolOf(Properties p, String key, boolean def) {
        return Boolean.parseBoolean(p.getProperty(key, String.valueOf(def)).trim());
    }
}
