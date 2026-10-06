package com.pomodoro.util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayOutputStream;

/** Tiny synthesized two-note chime. No audio files, no extra libraries. */
public final class Sound {
    private static final float RATE = 44100f;

    private Sound() {}

    public static void chime() {
        Thread t = new Thread(() -> {
            try {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                buf.writeBytes(tone(659.25, 0.16));
                buf.writeBytes(tone(880.00, 0.38));
                byte[] data = buf.toByteArray();
                AudioFormat fmt = new AudioFormat(RATE, 16, 1, true, false);
                try (SourceDataLine line = AudioSystem.getSourceDataLine(fmt)) {
                    line.open(fmt);
                    line.start();
                    line.write(data, 0, data.length);
                    line.drain();
                }
            } catch (Exception ignored) {
                // no audio device: stay silent
            }
        }, "chime");
        t.setDaemon(true);
        t.start();
    }

    private static byte[] tone(double hz, double seconds) {
        int n = (int) (RATE * seconds);
        byte[] b = new byte[n * 2];
        for (int i = 0; i < n; i++) {
            double env = Math.exp(-4.0 * i / n) * Math.min(1.0, i / 200.0);
            short v = (short) (Math.sin(2 * Math.PI * hz * i / RATE) * env * 0.3 * Short.MAX_VALUE);
            b[2 * i] = (byte) v;
            b[2 * i + 1] = (byte) (v >> 8);
        }
        return b;
    }
}
