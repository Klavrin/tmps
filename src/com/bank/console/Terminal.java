package com.bank.console;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Talks to the terminal: reads single key presses, reads lines and redraws
 * the screen.
 *
 * <p>Arrow keys need "raw" input: keys arrive one by one, without waiting
 * for Enter and without being echoed. That is switched on with the
 * {@code stty} command, which exists on macOS and Linux. Where it does not
 * work (Windows, most IDE consoles, piped input) the terminal stays in line
 * mode and {@link Ui} falls back to numbered menus.
 */
public final class Terminal implements AutoCloseable {

    public enum Key { UP, DOWN, ENTER, ESCAPE, BACKSPACE, CHARACTER, END_OF_INPUT }

    /** One key press. {@code character} is only set for {@link Key#CHARACTER}. */
    public record KeyPress(Key key, char character) {
        private static KeyPress of(Key key) {
            return new KeyPress(key, '\0');
        }
    }

    private static final String CSI = "\u001B[";

    private final InputStream in = System.in;
    private final PrintStream out =
            new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);
    private final String originalSettings;   // null when raw mode is not available
    private final Thread restoreOnExit = new Thread(this::restore);
    private boolean restored;

    public Terminal() {
        originalSettings = enterRawMode();
        if (isInteractive()) {
            // Put the terminal back even if the user presses Ctrl+C.
            Runtime.getRuntime().addShutdownHook(restoreOnExit);
            enterFullScreen();
        }
    }

    /** True when single key presses (and so arrow keys) can be read. */
    public boolean isInteractive() {
        return originalSettings != null;
    }

    /** Replaces the whole screen with {@code frame}. In line mode it is simply printed. */
    public void draw(String frame) {
        if (isInteractive()) {
            // Draw over the old frame instead of clearing first, so nothing flickers.
            print(CSI + "H" + frame.replace("\n", CSI + "K\n") + CSI + "K" + CSI + "J");
        } else {
            print(frame);
        }
    }

    public void print(String text) {
        out.print(text);
        out.flush();
    }

    public void showCursor(boolean visible) {
        if (isInteractive()) {
            print(CSI + (visible ? "?25h" : "?25l"));
        }
    }

    /** Switches to a separate screen buffer, so the app leaves the scrollback untouched. */
    public void enterFullScreen() {
        if (isInteractive()) {
            print(CSI + "?1049h" + CSI + "?25l");
        }
    }

    /** Back to the normal screen, e.g. to print long output the user can scroll through. */
    public void leaveFullScreen() {
        if (isInteractive()) {
            print(CSI + "?25h" + CSI + "?1049l");
        }
    }

    public KeyPress readKey() {
        while (true) {
            int b = read();
            switch (b) {
                case -1, 4 -> {                 // end of input, Ctrl+D
                    return KeyPress.of(Key.END_OF_INPUT);
                }
                case '\r', '\n' -> {
                    return KeyPress.of(Key.ENTER);
                }
                case 127, 8 -> {
                    return KeyPress.of(Key.BACKSPACE);
                }
                case 27 -> {
                    KeyPress key = readEscapeSequence();
                    if (key != null) {
                        return key;
                    }
                }
                default -> {
                    if (b >= 32 && b < 127) {
                        return new KeyPress(Key.CHARACTER, (char) b);
                    }
                }
            }
        }
    }

    /** Reads one line in line mode. Returns null at the end of the input. */
    public String readLine() {
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int b;
        while ((b = read()) != -1 && b != '\n') {
            if (b != '\r') {
                line.write(b);
            }
        }
        if (b == -1 && line.size() == 0) {
            return null;
        }
        return line.toString(StandardCharsets.UTF_8);
    }

    @Override
    public void close() {
        restore();
        try {
            Runtime.getRuntime().removeShutdownHook(restoreOnExit);
        } catch (IllegalStateException | IllegalArgumentException alreadyShuttingDownOrNotRegistered) {
            // nothing to undo
        }
    }

    /**
     * Arrow keys arrive as ESC [ A (up) and ESC [ B (down). A lone ESC,
     * with nothing following it, is the Escape key itself.
     * Returns null for sequences we do not use, such as left/right.
     */
    private KeyPress readEscapeSequence() {
        if (!moreInputArrivesSoon()) {
            return KeyPress.of(Key.ESCAPE);
        }
        int next = read();
        if (next != '[' && next != 'O') {
            return KeyPress.of(Key.ESCAPE);
        }
        int code = read();
        while (code == ';' || (code >= '0' && code <= '9')) {   // skip modifiers such as "1;5"
            code = read();
        }
        return switch (code) {
            case 'A' -> KeyPress.of(Key.UP);
            case 'B' -> KeyPress.of(Key.DOWN);
            default -> null;
        };
    }

    private boolean moreInputArrivesSoon() {
        try {
            for (int attempt = 0; attempt < 3; attempt++) {
                if (in.available() > 0) {
                    return true;
                }
                Thread.sleep(15);
            }
            return in.available() > 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private int read() {
        try {
            return in.read();
        } catch (IOException e) {
            return -1;
        }
    }

    private synchronized void restore() {
        if (!isInteractive() || restored) {
            return;
        }
        restored = true;
        leaveFullScreen();
        stty(originalSettings);
    }

    /** Returns the settings to restore later, or null if raw mode is not possible here. */
    private static String enterRawMode() {
        if (System.getProperty("os.name", "").startsWith("Windows")) {
            return null;
        }
        String original = stty("-g");
        if (original == null || stty("-icanon", "-echo", "min", "1", "time", "0") == null) {
            return null;
        }
        return original;
    }

    /** Runs stty on our own input. Fails (returns null) when that input is not a terminal. */
    private static String stty(String... arguments) {
        List<String> command = new ArrayList<>(List.of("stty"));
        command.addAll(List.of(arguments));
        try {
            Process process = new ProcessBuilder(command)
                    .redirectInput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            return process.waitFor() == 0 ? output : null;
        } catch (IOException e) {
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
