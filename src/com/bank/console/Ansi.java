package com.bank.console;

import java.util.regex.Pattern;

/**
 * Colours and styles for terminal text, using ANSI escape codes.
 * Set the {@code NO_COLOR} environment variable to turn them off.
 *
 * <p>Styles do not nest: each call resets everything at its end, so combine
 * codes with {@link #style(String, String)} (e.g. {@code "1;36"} = bold cyan).
 */
public final class Ansi {

    private static final boolean ENABLED = System.getenv("NO_COLOR") == null;
    private static final Pattern ESCAPE_CODE = Pattern.compile("\u001B\\[[0-9;?]*[A-Za-z]");

    private Ansi() {
    }

    public static String bold(String text)    { return style("1", text); }
    public static String dim(String text)     { return style("2", text); }
    public static String red(String text)     { return style("31", text); }
    public static String green(String text)   { return style("32", text); }
    public static String yellow(String text)  { return style("33", text); }
    public static String magenta(String text) { return style("35", text); }
    public static String cyan(String text)    { return style("36", text); }

    public static String style(String codes, String text) {
        return ENABLED ? "\u001B[" + codes + "m" + text + "\u001B[0m" : text;
    }

    /** The length of the text as it appears on screen, ignoring escape codes. */
    public static int visibleLength(String text) {
        return ESCAPE_CODE.matcher(text).replaceAll("").length();
    }

    public static String padRight(String text, int width) {
        return text + " ".repeat(Math.max(0, width - visibleLength(text)));
    }

    public static String padLeft(String text, int width) {
        return " ".repeat(Math.max(0, width - visibleLength(text))) + text;
    }

    /** Cuts plain (uncoloured) text down to {@code width} characters, ending with an ellipsis. */
    public static String fit(String text, int width) {
        return text.length() <= width ? text : text.substring(0, Math.max(0, width - 1)) + "…";
    }
}
