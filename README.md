# Cute Pomodoro

Java 21 + JavaFX 21 + Maven. Focus 25 / short break 5 / long break 20 after 4 sessions.
On every break the Windows monitor is switched off (PC stays awake) - configurable in Settings.

## Run
    mvn clean javafx:run

## Package (Windows)
    mvn clean package
    jpackage --type app-image --name CutePomodoro --input target\libs --main-jar cute-pomodoro-1.0.0.jar ^
      --main-class com.pomodoro.Main --dest target\dist ^
      --add-modules java.desktop,java.logging,java.prefs,java.xml,jdk.unsupported
Result: `target\dist\CutePomodoro\CutePomodoro.exe` (no installer tools needed).

For a real installer use `--type exe --win-menu --win-shortcut` instead (requires WiX Toolset 3.x on PATH).

## Replace the character
Put `character.png` in `src/main/resources/images/` (PNG/JPG/GIF; JavaFX cannot read WebP).

Settings are stored in `%USERPROFILE%\.cute-pomodoro\settings.properties`.
