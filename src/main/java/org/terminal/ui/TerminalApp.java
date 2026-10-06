package org.terminal.ui;

import org.terminal.engine.Style;
import org.terminal.engine.TerminalBuffer;
import org.terminal.engine.TerminalColor;

import javax.swing.*;

public class TerminalApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TerminalApp::createAndShowGui);
    }

    private static void createAndShowGui() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);

        loadDemoContent(buffer);

        JFrame frame = new JFrame("Terminal Text Buffer - Live Demo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        TerminalPanel panel = new TerminalPanel(buffer);
        frame.setContentPane(panel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        panel.requestFocusInWindow();
    }

    private static void loadDemoContent(TerminalBuffer buffer) {
        buffer.setForegroundColor(TerminalColor.BRIGHT_CYAN);
        buffer.addStyle(Style.BOLD);
        buffer.write("=== Terminal Text Buffer Engine ===\n");

        buffer.clearStyles();
        buffer.setForegroundColor(TerminalColor.YELLOW);
        buffer.write("Tip: Resize this window with your mouse to watch dynamic text reflow!\n\n");

        buffer.setForegroundColor(TerminalColor.DEFAULT);
        buffer.write("Styles showcase: ");

        buffer.addStyle(Style.BOLD);
        buffer.write("[Bold] ");
        buffer.removeStyle(Style.BOLD);

        buffer.addStyle(Style.ITALIC);
        buffer.write("[Italic] ");
        buffer.removeStyle(Style.ITALIC);

        buffer.addStyle(Style.UNDERLINE);
        buffer.write("[Underline] ");
        buffer.clearStyles();
        buffer.write("\n\n");

        buffer.setForegroundColor(TerminalColor.BRIGHT_GREEN);
        buffer.write("Reflow text paragraph:\n");
        buffer.setForegroundColor(TerminalColor.DEFAULT);
        buffer.write("This long sentence is placed here specifically so you can grab the edge of this window " +
                "and shrink it horizontally. Notice how words unwrap and re-wrap cleanly without any " +
                "lost text, while maintaining accurate cursor coordinates under the hood.\n\n");

        buffer.setForegroundColor(TerminalColor.BRIGHT_MAGENTA);
        buffer.write("user@terminal");
        buffer.setForegroundColor(TerminalColor.DEFAULT);
        buffer.write(":");
        buffer.setForegroundColor(TerminalColor.BRIGHT_BLUE);
        buffer.write("~");
        buffer.setForegroundColor(TerminalColor.DEFAULT);
        buffer.write("$ ");
    }
}