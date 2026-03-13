package org.terminal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class TerminalBuffer {

    private Screen screen;
    private Scrollback scrollback;

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

    public void resizeScreen(int newWidth, int newHeight) {
        List<Line> oldScrollbackLines = scrollback.getAllLines();
        List<List<Cell>> unwrappedScrollbackLines = unwrapLines(oldScrollbackLines, null);

        Scrollback newScrollback = new Scrollback(scrollback.getMaxLines(), newWidth);

        for (List<Cell> unwrappedScrollbackLine : unwrappedScrollbackLines) {
            List<Line> wrappedBack = wrapBackLines(unwrappedScrollbackLine, newWidth);
            for (Line line : wrappedBack) {
                newScrollback.push(line);
            }
        }

        int safeCursorX = Math.min(cursorX, screen.getWidth() - 1);
        int safeCursorY = Math.min(cursorY, screen.getHeight() - 1);
        Cell cellTargetedByCursor = getCellAt(safeCursorX, safeCursorY);

        int newCursorX = cursorX;
        int newCursorY = cursorY;

        List<Line> oldScreenLines = screen.getAllLines();
        List<List<Cell>> unwrappedScreenLines = unwrapLines(oldScreenLines, cellTargetedByCursor);

        List<Line> newScreenLines = new ArrayList<>();
        for (List<Cell> unwrappedScreenLine : unwrappedScreenLines) {
            if (unwrappedScreenLine.isEmpty()) {
                newScreenLines.add(new Line(newWidth));
                continue;
            }

            int currentLineIndex = 0;
            while (currentLineIndex < unwrappedScreenLine.size()) {
                Line newLine = new Line(newWidth);
                int charsToCopy = Math.min(newWidth, unwrappedScreenLine.size() - currentLineIndex);

                for (int x = 0; x < charsToCopy; x++) {
                    Cell originalCell = unwrappedScreenLine.get(currentLineIndex + x);
                    newLine.getCell(x).copyFrom(originalCell);

                    if (originalCell == cellTargetedByCursor) {
                        newCursorX = x;
                        newCursorY = newScreenLines.size();
                    }
                }
                currentLineIndex += charsToCopy;
                if (currentLineIndex < unwrappedScreenLine.size()) {
                    newLine.setWrapped(true);
                }
                newScreenLines.add(newLine);
            }
        }

        Screen newScreen = new Screen(newWidth, newHeight);

        int spillCount = Math.max(0, newScreenLines.size() - newHeight);
        for (int i = 0; i < spillCount; i++) {
            newScrollback.push(newScreenLines.get(i));
        }

        newCursorX = Math.min(newWidth - 1, Math.max(0, newCursorX));
        newCursorY -= spillCount;

        if (newCursorY < 0) {
            newCursorY = 0;
        } else if (newCursorY >= newScreenLines.size()) {
            newCursorY = newHeight - 1;
        }

        int writeY = 0;
        for (int i = spillCount; i < newScreenLines.size(); i++) {
            Line sourceLine = newScreenLines.get(i);
            Line destinationLine = newScreen.getLine(writeY);
            for (int x = 0; x < sourceLine.getWidth(); x++) {
                destinationLine.getCell(x).copyFrom(sourceLine.getCell(x));
            }
            destinationLine.setWrapped(sourceLine.isWrapped());
            writeY++;
        }

        this.screen = newScreen;
        this.scrollback = newScrollback;
        this.cursorX = newCursorX;
        this.cursorY = newCursorY;
    }

    private List<List<Cell>> unwrapLines(List<Line> lines, Cell cellTargetedByCursor) {
        List<List<Cell>> unwrapped = new ArrayList<>();
        List<Cell> currentLine = new ArrayList<>();

        for (Line line : lines) {
            int lastValidIndex = line.getWidth() - 1;
            boolean hasCursor = false;
            int cursorColumn = -1;

            if (cellTargetedByCursor != null) {
                for (int x = 0; x < line.getWidth(); x++) {
                    if (line.getCell(x) == cellTargetedByCursor) {
                        hasCursor = true;
                        cursorColumn = x;
                        break;
                    }
                }
            }

            if (!line.isWrapped()) {
                while (lastValidIndex >= 0 && line.getCell(lastValidIndex).isDefault()) {
                    lastValidIndex--;
                }
            }

            if (hasCursor && cursorColumn > lastValidIndex) {
                lastValidIndex = cursorColumn;
            }

            for (int x = 0; x <= lastValidIndex; x++) {
                currentLine.add(line.getCell(x));
            }

            if (!line.isWrapped()) {
                unwrapped.add(currentLine);
                currentLine = new ArrayList<>();
            }
        }

        if (!currentLine.isEmpty()) {
            unwrapped.add(currentLine);
        }

        return unwrapped;
    }

    private List<Line> wrapBackLines(List<Cell> unwrappedLine, int width) {
        List<Line> wrappedLines = new ArrayList<>();

        if (unwrappedLine.isEmpty()) {
            wrappedLines.add(new Line(width));
            return wrappedLines;
        }

        int currentLineIndex = 0;
        while (currentLineIndex < unwrappedLine.size()) {
            Line newLine = new Line(width);
            int toBeCopied = Math.min(width, unwrappedLine.size() - currentLineIndex);

            for (int x = 0; x < toBeCopied; x++) {
                newLine.getCell(x).copyFrom(unwrappedLine.get(currentLineIndex + x));
            }

            currentLineIndex += toBeCopied;

            if (currentLineIndex < unwrappedLine.size()) {
                newLine.setWrapped(true);
            }
            wrappedLines.add(newLine);
        }
        return wrappedLines;
    }

    public void insert(String text) {
        CellAttributes attributes = new CellAttributes(currentForegroundColor, currentBackgroundColor, currentStyles);

        for (char character : text.toCharArray()) {
            if (character == '\n') {
                if (cursorX >= screen.getWidth()) {
                    cursorX = screen.getWidth() - 1;
                }
                screen.getLine(cursorY).setWrapped(false);
                cursorX = 0;
                cursorY++;
                handleScroll();
                continue;
            }

            insertChar(character, attributes);
        }
    }

    private void insertChar(char character, CellAttributes attributes) {
        if (cursorX >= screen.getWidth()) {
            screen.getLine(cursorY).setWrapped(true);
            cursorX = 0;
            cursorY++;
            handleScroll();
        }
        screen.insertCharAt(cursorX, cursorY, character, attributes);
        cursorX++;
    }

    public void write(String text) {
        CellAttributes attributes = new CellAttributes(currentForegroundColor, currentBackgroundColor, currentStyles);

        for (char character : text.toCharArray()) {
            if (character == '\n') {
                if (cursorX >= screen.getWidth()) {
                    cursorX = screen.getWidth() - 1;
                }
                screen.getLine(cursorY).setWrapped(false);
                cursorX = 0;
                cursorY++;
                handleScroll();
                continue;
            }

            if (cursorX >= screen.getWidth()) {
                screen.getLine(cursorY).setWrapped(true);
                cursorX = 0;
                cursorY++;
                handleScroll();
            }

            screen.setCell(cursorX, cursorY, character, attributes);
            cursorX++;

        }

    }

    private void handleScroll() {
        if (cursorY >= screen.getHeight()) {
            insertEmptyLineAtBottom();
            cursorY = screen.getHeight() - 1;
        }
    }

    public void insertEmptyLineAtBottom() {
        Line topLine = screen.scrollUp();
        scrollback.push(topLine);
    }

    public void fillLine(char character) {
        CellAttributes attributes = new CellAttributes(currentForegroundColor, currentBackgroundColor, currentStyles);
        screen.fillLine(cursorY, character, attributes);
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

    public void clearScreen() {
        this.screen.clear();
    }

    public void clearScreenAndScrollback() {
        this.screen.clear();
        this.scrollback.clear();
    }
}
