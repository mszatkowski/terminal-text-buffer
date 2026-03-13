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

    void copyFrom(Cell other) {
        this.character = other.character;
        this.foregroundColor = other.foregroundColor;
        this.backgroundColor = other.backgroundColor;
        this.styles.clear();
        this.styles.addAll(other.styles);
    }

    char getCharacter() {
        return character;
    }

    void setCharacter(char character) {
        this.character = character;
    }

    TerminalColor getForegroundColor() {
        return foregroundColor;
    }

    void setForegroundColor(TerminalColor foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    TerminalColor getBackgroundColor() {
        return backgroundColor;
    }

    void setBackgroundColor(TerminalColor backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    Set<Style> getStyles() {
        return Collections.unmodifiableSet(styles);
    }

    void addStyle(Style style) {
        styles.add(style);
    }

    void removeStyle(Style style) {
        styles.remove(style);
    }

    void setStyles(Set<Style> styles) {
        this.styles.clear();
        this.styles.addAll(styles);
    }

    char getPrintableCharacter() {
        return character == '\0' ? ' ' : character;
    }

    void clear() {
        this.character = '\0';
        this.foregroundColor = TerminalColor.DEFAULT;
        this.backgroundColor = TerminalColor.DEFAULT;
        this.styles.clear();
    }

    boolean isDefault() {
        return character == '\0'
                && styles.isEmpty()
                && foregroundColor == TerminalColor.DEFAULT
                && backgroundColor == TerminalColor.DEFAULT;
    }
}
