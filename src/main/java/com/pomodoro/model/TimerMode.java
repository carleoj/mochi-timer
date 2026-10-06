package com.pomodoro.model;

public enum TimerMode {
    FOCUS("Focus", "focus"),
    SHORT_BREAK("Short Break", "short-break"),
    LONG_BREAK("Long Break", "long-break");

    private final String label;
    private final String styleClass;

    TimerMode(String label, String styleClass) {
        this.label = label;
        this.styleClass = styleClass;
    }

    public String label() { return label; }
    public String styleClass() { return styleClass; }
    public boolean isBreak() { return this != FOCUS; }
}
