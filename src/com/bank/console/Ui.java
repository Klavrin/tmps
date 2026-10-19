package com.bank.console;

import com.bank.console.Terminal.KeyPress;

import java.util.List;

/**
 * Asks the user for something - a menu choice, a line of text or a key
 * press - below a screen of content.
 *
 * <p>With a real terminal, menus are driven with the arrow keys. In line mode
 * the same questions are asked by number, so the app still works in an IDE.
 */
public final class Ui {

    /** One menu entry: a label, plus a short dimmed detail shown next to it. */
    public record Option(String label, String detail) {
    }

    private final Terminal terminal;

    public Ui(Terminal terminal) {
        this.terminal = terminal;
    }

    /** Returns the index of the chosen option, or -1 if the user backed out. */
    public int choose(String screen, String title, List<Option> options) {
        return terminal.isInteractive()
                ? chooseWithArrows(screen, title, options)
                : chooseByNumber(screen, title, options);
    }

    /** Returns the trimmed answer, or null if the user cancelled (Esc or an empty answer). */
    public String ask(String screen, String question, String hint, String error) {
        StringBuilder frame = new StringBuilder(screen)
                .append("\n  ").append(Ansi.bold(question)).append('\n')
                .append("  ").append(Ansi.dim(hint)).append('\n');
        if (error != null) {
            frame.append("  ").append(Ansi.red("✖ " + error)).append('\n');
        }
        frame.append('\n').append("  ").append(Ansi.style("1;36", "❯ "));

        String answer = terminal.isInteractive() ? editLine(frame.toString()) : readLine(frame.toString());
        return answer == null || answer.isBlank() ? null : answer.trim();
    }

    /** Shows {@code screen} and waits for a key (or Enter, in line mode). */
    public void pause(String screen) {
        if (terminal.isInteractive()) {
            terminal.draw(screen + "\n  " + Ansi.dim("press any key to go back"));
            terminal.readKey();
        } else {
            terminal.draw(screen + "\n  " + Ansi.dim("press enter to go back "));
            terminal.readLine();
        }
    }

    /** Waits for a key after output that was printed normally, outside the full-screen view. */
    public void pauseAfterOutput(String message) {
        terminal.print("\n  " + Ansi.style("1;36", "❯ ") + Ansi.dim(message) + " ");
        if (terminal.isInteractive()) {
            terminal.readKey();
        } else {
            terminal.readLine();
        }
    }

    private int chooseWithArrows(String screen, String title, List<Option> options) {
        int labelWidth = labelWidth(options);
        int selected = 0;
        while (true) {
            StringBuilder frame = new StringBuilder(screen)
                    .append("\n  ").append(Ansi.bold(title)).append("\n\n");
            for (int i = 0; i < options.size(); i++) {
                Option option = options.get(i);
                String label = Ansi.padRight(option.label(), labelWidth);
                if (i == selected) {
                    frame.append("  ").append(Ansi.style("1;36", "❯ " + label));
                } else {
                    frame.append("    ").append(label);
                }
                frame.append("  ").append(Ansi.dim(option.detail())).append('\n');
            }
            frame.append("\n  ").append(Ansi.dim("↑/↓ move · enter select · esc back"));
            terminal.draw(frame.toString());

            KeyPress key = terminal.readKey();
            switch (key.key()) {
                case UP -> selected = (selected + options.size() - 1) % options.size();
                case DOWN -> selected = (selected + 1) % options.size();
                case ENTER -> {
                    return selected;
                }
                case ESCAPE, END_OF_INPUT -> {
                    return -1;
                }
                case CHARACTER -> {
                    char c = key.character();
                    if (c == 'k') {
                        selected = (selected + options.size() - 1) % options.size();
                    } else if (c == 'j') {
                        selected = (selected + 1) % options.size();
                    } else if (c == 'q') {
                        return -1;
                    } else if (c >= '1' && c <= '9' && c - '1' < options.size()) {
                        return c - '1';
                    }
                }
                default -> {
                }
            }
        }
    }

    private int chooseByNumber(String screen, String title, List<Option> options) {
        int labelWidth = labelWidth(options);
        StringBuilder frame = new StringBuilder(screen)
                .append("\n  ").append(Ansi.bold(title)).append("\n\n");
        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);
            frame.append("  ").append(Ansi.cyan(Ansi.padLeft(String.valueOf(i + 1), 2) + ")")).append(' ')
                    .append(Ansi.padRight(option.label(), labelWidth))
                    .append("  ").append(Ansi.dim(option.detail())).append('\n');
        }
        terminal.draw(frame.toString());

        while (true) {
            String answer = readLine("\n  Choose 1-" + options.size() + " (empty to go back): ");
            if (answer == null || answer.isBlank()) {
                return -1;
            }
            try {
                int choice = Integer.parseInt(answer.trim());
                if (choice >= 1 && choice <= options.size()) {
                    return choice - 1;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the error below
            }
            terminal.print("  " + Ansi.red("✖ Please type a number from 1 to " + options.size()) + "\n");
        }
    }

    /** Line editing in raw mode: we echo the typed characters ourselves. */
    private String editLine(String frame) {
        StringBuilder input = new StringBuilder();
        terminal.showCursor(true);
        try {
            while (true) {
                terminal.draw(frame + input);
                KeyPress key = terminal.readKey();
                switch (key.key()) {
                    case ENTER -> {
                        return input.toString();
                    }
                    case ESCAPE, END_OF_INPUT -> {
                        return null;
                    }
                    case BACKSPACE -> {
                        if (input.length() > 0) {
                            input.setLength(input.length() - 1);
                        }
                    }
                    case CHARACTER -> input.append(key.character());
                    default -> {
                    }
                }
            }
        } finally {
            terminal.showCursor(false);
        }
    }

    private String readLine(String prompt) {
        terminal.print(prompt);
        return terminal.readLine();
    }

    private static int labelWidth(List<Option> options) {
        return options.stream().mapToInt(o -> Ansi.visibleLength(o.label())).max().orElse(0);
    }
}
