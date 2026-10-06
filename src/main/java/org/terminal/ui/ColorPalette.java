package org.terminal.ui;

import org.terminal.engine.TerminalColor;

import java.awt.Color;

final class ColorPalette {

    public static final Color DEFAULT_BACKGROUND = new Color(0x1E, 0x1E, 0x1E);
    public static final Color DEFAULT_FOREGROUND = new Color(0xD4, 0xD4, 0xD4);

    private ColorPalette() {}

    public static Color getForeground(TerminalColor color) {
        if (color == TerminalColor.DEFAULT) {
            return DEFAULT_FOREGROUND;
        }
        return mapColor(color);
    }

    public static Color getBackground(TerminalColor color) {
        if (color == TerminalColor.DEFAULT) {
            return DEFAULT_BACKGROUND;
        }
        return mapColor(color);
    }

    private static Color mapColor(TerminalColor color) {
        return switch (color) {
            case DEFAULT -> DEFAULT_FOREGROUND;
            case BLACK -> new Color(0x00, 0x00, 0x00);
            case RED -> new Color(0xCD, 0x31, 0x31);
            case GREEN -> new Color(0x0D, 0xBC, 0x79);
            case YELLOW -> new Color(0xE5, 0xE5, 0x10);
            case BLUE -> new Color(0x24, 0x72, 0xC8);
            case MAGENTA -> new Color(0xBC, 0x3F, 0xBC);
            case CYAN -> new Color(0x11, 0xA8, 0xCD);
            case WHITE -> new Color(0xE5, 0xE5, 0xE5);
            case BRIGHT_BLACK -> new Color(0x66, 0x66, 0x66);
            case BRIGHT_RED -> new Color(0xF1, 0x4C, 0x4C);
            case BRIGHT_GREEN -> new Color(0x23, 0xD1, 0x8B);
            case BRIGHT_YELLOW -> new Color(0xF5, 0xF5, 0x43);
            case BRIGHT_BLUE -> new Color(0x3B, 0x8E, 0xEA);
            case BRIGHT_MAGENTA -> new Color(0xD6, 0x70, 0xD6);
            case BRIGHT_CYAN -> new Color(0x29, 0xB8, 0xDB);
            case BRIGHT_WHITE -> new Color(0xFF, 0xFF, 0xFF);
        };
    }
}