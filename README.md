# MochiTimer Pomodoro

A small, lightweight Pomodoro timer for Windows built with **Java 21+, JavaFX 21, and Maven**.

A cute pomodoro provides customizable focus and break timers with an optional cute character popup and automatic monitor shutdown during breaks.

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

## Run

Run the application with Maven:

```bash
mvn clean javafx:run
```

## Build

Create the Maven package:

```bash
mvn clean package
```

## Package for Windows

The application is packaged as a standalone Windows application using `jpackage`.

The build process uses a custom application icon and includes the required Java modules and native-access options.

Example PowerShell build script:

```powershell
$env:JAVA_HOME = "C:\Users\CARL JIMROE PANO\.jdks\openjdk-27"

$ico = "C:\Users\CARL JIMROE PANO\Documents\cute-pomodoro\src\main\packaging\app-icon.ico"

$mvn = Get-ChildItem "C:\Program Files\JetBrains","$env:LOCALAPPDATA\Programs","$env:LOCALAPPDATA\JetBrains","$env:USERPROFILE\.m2" `
    -Recurse -Filter mvn.cmd -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName

"Using Maven: $mvn"

& $mvn clean package

if (Test-Path target\dist) {
    Remove-Item -Recurse -Force target\dist
}

& "$env:JAVA_HOME\bin\jpackage.exe" `
    --type app-image `
    --name CutePomodoro `
    --input target\libs `
    --main-jar cute-pomodoro-1.0.0.jar `
    --main-class com.pomodoro.Main `
    --dest target\dist `
    --icon $ico `
    --add-modules java.desktop,java.logging,java.prefs,java.xml,jdk.unsupported `
    --java-options "--enable-native-access=ALL-UNNAMED" `
    --java-options "--sun-misc-unsafe-memory-access=allow"

dir target\dist\CutePomodoro\CutePomodoro.exe
```

The resulting executable is:

```text
target\dist\CutePomodoro\CutePomodoro.exe
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

No account or external service is required.

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
