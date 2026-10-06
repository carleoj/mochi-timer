# MochiTimer Pomodoro for Windows

A small, lightweight Pomodoro timer for Windows built with **Java 21+, JavaFX 21, and Maven**.

MochiTimer provides customizable focus and break timers with an optional cute character popup and automatic monitor shutdown during breaks.

## Download

**[Download MochiTimer for Windows](https://www.dropbox.com/scl/fi/v24znl50jq3c1yi9m949y/MochiTimer.zip?rlkey=8fy5na947bsu9h77uv1ms19tx&st=anc849e7&dl=0)**

Download the ZIP, extract it, and run `MochiTimer.exe`.

## Features

- Customizable focus duration
- Customizable short break duration
- Customizable long break duration
- Configurable sessions before a long break
- Cute character popup when a focus session ends
- Optional automatic monitor shutdown during breaks
- PC remains awake while the monitor is off
- Lightweight JavaFX desktop GUI
- Local settings storage
- No account or internet connection required

## Default Settings

| Setting | Default |
|---|---:|
| Focus | 25 minutes |
| Short Break | 5 minutes |
| Long Break | 20 minutes |
| Sessions Before Long Break | 4 |

All durations can be changed from **Settings**.

## Tech Stack

- **Java 21+**
- **JavaFX 21**
- **Maven**
- **Windows API** for monitor power control

## Run from Source

```bash
mvn clean javafx:run
```

## Build

```bash
mvn clean package
```

## Package for Windows

Make sure `JAVA_HOME` points to your JDK installation and Maven is available on your `PATH`.

```powershell
jpackage `
  --type app-image `
  --name MochiTimer `
  --input target\libs `
  --main-jar cute-pomodoro-1.0.0.jar `
  --main-class com.pomodoro.Main `
  --dest target\dist `
  --icon src\main\packaging\app-icon.ico `
  --add-modules java.desktop,java.logging,java.prefs,java.xml,jdk.unsupported `
  --java-options "--enable-native-access=ALL-UNNAMED" `
  --java-options "--sun-misc-unsafe-memory-access=allow"
```

The resulting application will be located at:

```text
target\dist\MochiTimer\MochiTimer.exe
```

## Character

Replace the character by placing an image at:

```text
src/main/resources/images/character.png
```

Supported formats:

- PNG
- JPG/JPEG
- GIF

> JavaFX does not natively support WebP images.

## Settings

The following settings can be customized:

- Focus duration
- Short break duration
- Long break duration
- Sessions before a long break
- Turn off monitor during breaks
- Show character popup

Settings are stored locally at:

```text
%USERPROFILE%\.cute-pomodoro\settings.properties
```

## Project Structure

```text
src/
└── main/
    ├── java/
    │   └── com/
    │       └── pomodoro/
    ├── packaging/
    │   └── app-icon.ico
    └── resources/
        ├── images/
        └── styles/
```
