package org.terminal;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

class Cell {
    private char character;
    private TerminalColor foregroundColor;
    private TerminalColor backgroundColor;
    private final EnumSet<Style> styles = EnumSet.noneOf(Style.class);

    Cell(char character) {
        this(character, TerminalColor.DEFAULT, TerminalColor.DEFAULT, Collections.emptySet());
    }

    Cell(char character, TerminalColor foregroundColor, TerminalColor backgroundColor, Set<Style> styles) {
        this.character = character;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.styles.addAll(styles);
    }

    public char getCharacter() {
        return character;
    }

    public void setCharacter(char character) {
        this.character = character;
    }

    public TerminalColor getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(TerminalColor foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public TerminalColor getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(TerminalColor backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Set<Style> getStyles() {
        return Collections.unmodifiableSet(styles);
    }

    public void addStyle(Style style) {
        styles.add(style);
    }

    public void removeStyle(Style style) {
        styles.remove(style);
    }
}
