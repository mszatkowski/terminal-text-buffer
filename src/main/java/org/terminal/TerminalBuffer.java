package org.terminal;

import java.util.EnumSet;

public class TerminalBuffer {

    private final Screen screen;
    private final Scrollback scrollback;

    private int cursorX;
    private int cursorY;

    private TerminalColor currentForegroundColor;
    private TerminalColor currentBackgroundColor;
    private final EnumSet<Style> currentStyles;

    public TerminalBuffer(int screenWidth, int screenHeight, int maxScrollback) {
        this.screen = new Screen(screenWidth, screenHeight);
        this.scrollback = new Scrollback(maxScrollback, screenWidth);
        this.cursorX = 0;
        this.cursorY = 0;
        this.currentForegroundColor = TerminalColor.DEFAULT;
        this.currentBackgroundColor = TerminalColor.DEFAULT;
        this.currentStyles = EnumSet.noneOf(Style.class);
    }

    public String getEntireContentAsString() {
        if (scrollback.getSize() == 0) {
            return screen.toString();
        }
        return scrollback.toString() + "\n" + screen.toString();
    }

    public String getScrollbackAsString() {
        return scrollback.toString();
    }

    public String getScreenAsString() {
        return screen.toString();
    }

    public char getCharacterAtPosition(int column, int row) {
        return getCellAt(column, row).getCharacter();
    }

    public CellAttributes getCellAttributesAt(int column, int row) {
        Cell cell = getCellAt(column, row);
        return new CellAttributes(
                cell.getForegroundColor(),
                cell.getBackgroundColor(),
                cell.getStyles()
        );
    }

    public String getLineAsString(int y) {
        if (y >= 0 && y < screen.getHeight()) {
            return screen.getLine(y).toString();
        } else if (y < 0 && -y <= scrollback.getSize()){
            return scrollback.getLine(y).toString();
        } else {
            throw new IndexOutOfBoundsException("Invalid row: " + y);
        }
    }

    private Cell getCellAt(int x, int y) {
        if (y >= 0 && y < screen.getHeight()) {
            return screen.getCell(x, y);
        } else if (y < 0 && -y <= scrollback.getSize()){
            return scrollback.getCellAt(x, y);
        } else {
            throw new IndexOutOfBoundsException("Invalid coordinates: " + x + ", " + y);
        }
    }

    public void setCursorPosition(int column, int row) {
        this.cursorX = Math.max(0, Math.min(column, screen.getWidth() - 1));
        this.cursorY = Math.max(0, Math.min(row, screen.getHeight() - 1));
    }

    public void moveCursorUp(int n) {
        setCursorPosition(cursorX, cursorY - n);
    }

    public void moveCursorDown(int n) {
        setCursorPosition(cursorX, cursorY + n);
    }

    public void moveCursorLeft(int n) {
        setCursorPosition(cursorX - n, cursorY);
    }

    public void moveCursorRight(int n) {
        setCursorPosition(cursorX + n, cursorY);
    }

    public int getCursorColumn() {
        return cursorX;
    }

    public int getCursorRow() {
        return cursorY;
    }

    public void setForegroundColor(TerminalColor color) {
        this.currentForegroundColor = color;
    }

    public void setBackgroundColor(TerminalColor color) {
        this.currentBackgroundColor = color;
    }

    public void addStyle(Style style) {
        this.currentStyles.add(style);
    }

    public void removeStyle(Style style) {
        this.currentStyles.remove(style);
    }

    public void clearStyles() {
        this.currentStyles.clear();
    }
}
