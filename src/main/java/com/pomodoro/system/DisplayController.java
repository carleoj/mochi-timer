package com.pomodoro.system;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.WPARAM;

/**
 * All Windows-specific display logic lives here.
 *
 * turnOffMonitor() posts WM_SYSCOMMAND / SC_MONITORPOWER (off) to the system. This only switches the
 * display off: it does not sleep, hibernate or shut down the PC. While the monitor is off we also set
 * ES_SYSTEM_REQUIRED so Windows' idle timer does not put the PC itself to sleep; releaseWakeLock()
 * clears that again.
 *
 * Both calls are no-ops on non-Windows systems and never throw. Call them from the JavaFX thread
 * (SetThreadExecutionState is per-thread, so set and release must happen on the same thread).
 */
public final class DisplayController {
    private static final boolean WINDOWS =
            System.getProperty("os.name", "").toLowerCase().contains("win");

    private static final int WM_SYSCOMMAND = 0x0112;
    private static final int SC_MONITORPOWER = 0xF170;
    private static final int MONITOR_OFF = 2;          // 1 = low power, 2 = off
    private static final long HWND_BROADCAST = 0xFFFF;

    private static final int ES_CONTINUOUS = 0x80000000;
    private static final int ES_SYSTEM_REQUIRED = 0x00000001;

    private DisplayController() {}

    /** Requests that the display enter its off state. The PC stays awake. */
    public static void turnOffMonitor() {
        if (!WINDOWS) return;
        try {
            setExecutionState(ES_CONTINUOUS | ES_SYSTEM_REQUIRED);
            HWND all = new HWND(Pointer.createConstant(HWND_BROADCAST));
            User32.INSTANCE.PostMessage(all, WM_SYSCOMMAND,
                    new WPARAM(SC_MONITORPOWER), new LPARAM(MONITOR_OFF));
        } catch (Throwable t) {
            System.err.println("Could not turn off monitor: " + t);
        }
    }

    /** Lets Windows manage sleep normally again. */
    public static void releaseWakeLock() {
        if (!WINDOWS) return;
        try {
            setExecutionState(ES_CONTINUOUS);
        } catch (Throwable t) {
            System.err.println("Could not release wake lock: " + t);
        }
    }

    private static void setExecutionState(int flags) {
        Kernel.INSTANCE.SetThreadExecutionState(flags);
    }

    /** Lazy holder so kernel32 is only loaded on Windows when first needed. */
    private interface Kernel extends Library {
        Kernel INSTANCE = Native.load("kernel32", Kernel.class);

        int SetThreadExecutionState(int esFlags);
    }
}
