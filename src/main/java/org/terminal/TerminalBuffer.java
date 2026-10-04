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
        Scrollback newScrollback = reflowScrollback(newWidth);

        Cell cursorTarget = getCursorTargetCell();
        List<List<Cell>> unwrappedScreen = unwrapLines(screen.getAllLines(), cursorTarget);
        List<Line> newScreenLines = wrapLogicalLines(unwrappedScreen, newWidth);

        int spillCount = Math.max(0, newScreenLines.size() - newHeight);
        spillLinesToScrollback(newScreenLines, spillCount, newScrollback);

        Screen newScreen = buildNewScreen(newScreenLines, spillCount, newWidth, newHeight);
        adjustCursorPosition(unwrappedScreen, cursorTarget, newWidth, spillCount, newScreenLines.size(), newHeight);

        this.screen = newScreen;
        this.scrollback = newScrollback;
    }

    private Scrollback reflowScrollback(int newWidth) {
        Scrollback newScrollback = new Scrollback(scrollback.getMaxLines(), newWidth);
        List<List<Cell>> unwrapped = unwrapLines(scrollback.getAllLines(), null);
        List<Line> reflowedLines = wrapLogicalLines(unwrapped, newWidth);

        for (Line line : reflowedLines) {
            newScrollback.push(line);
        }
        return newScrollback;
    }

    private Cell getCursorTargetCell() {
        int safeCursorX = Math.min(cursorX, screen.getWidth() - 1);
        int safeCursorY = Math.min(cursorY, screen.getHeight() - 1);
        return getCellAt(safeCursorX, safeCursorY);
    }

    private void spillLinesToScrollback(List<Line> lines, int spillCount, Scrollback targetScrollback) {
        for (int i = 0; i < spillCount; i++) {
            targetScrollback.push(lines.get(i));
        }
    }

    private Screen buildNewScreen(List<Line> lines, int spillCount, int newWidth, int newHeight) {
        Screen newScreen = new Screen(newWidth, newHeight);
        int targetRow = 0;

        for (int i = spillCount; i < lines.size(); i++) {
            Line sourceLine = lines.get(i);
            Line destinationLine = newScreen.getLine(targetRow);
            for (int x = 0; x < sourceLine.getWidth(); x++) {
                destinationLine.getCell(x).copyFrom(sourceLine.getCell(x));
            }
            destinationLine.setWrapped(sourceLine.isWrapped());
            targetRow++;
        }
        return newScreen;
    }

    private void adjustCursorPosition(List<List<Cell>> logicalLines, Cell cursorTarget, int newWidth, int spillCount, int totalLines, int newHeight) {
        int linesBefore = 0;

        for (List<Cell> logicalLine : logicalLines) {
            if (logicalLine.isEmpty()) {
                linesBefore++;
                continue;
            }

            int targetIndex = logicalLine.indexOf(cursorTarget);
            if (targetIndex != -1) {
                this.cursorX = targetIndex % newWidth;
                this.cursorY = linesBefore + (targetIndex / newWidth);
                break;
            }

            linesBefore += (logicalLine.size() + newWidth - 1) / newWidth;
        }

        this.cursorX = Math.clamp(this.cursorX, 0, newWidth - 1);
        this.cursorY -= spillCount;

        if (this.cursorY < 0) {
            this.cursorY = 0;
        } else if (this.cursorY >= totalLines) {
            this.cursorY = newHeight - 1;
        }
    }

    private List<List<Cell>> unwrapLines(List<Line> lines, Cell cellTargetedByCursor) {
        List<List<Cell>> unwrapped = new ArrayList<>();
        List<Cell> currentLogicalLine = new ArrayList<>();

        for (Line line : lines) {
            int effectiveWidth = calculateEffectiveLineWidth(line, cellTargetedByCursor);

            for (int x = 0; x < effectiveWidth; x++) {
                currentLogicalLine.add(line.getCell(x));
            }

            if (!line.isWrapped()) {
                unwrapped.add(currentLogicalLine);
                currentLogicalLine = new ArrayList<>();
            }
        }

        if (!currentLogicalLine.isEmpty()) {
            unwrapped.add(currentLogicalLine);
        }

        return unwrapped;
    }

    private int calculateEffectiveLineWidth(Line line, Cell cellTargetedByCursor) {
        if (line.isWrapped()) {
            return line.getWidth();
        }

        int lastContentIndex = findLastNonDefaultCellIndex(line);
        int cursorColumn = findCellColumn(line, cellTargetedByCursor);

        int lastValidIndex = Math.max(lastContentIndex, cursorColumn);
        return lastValidIndex + 1;
    }

    private int findLastNonDefaultCellIndex(Line line) {
        int index = line.getWidth() - 1;
        while (index >= 0 && line.getCell(index).isDefault()) {
            index--;
        }
        return index;
    }

    private int findCellColumn(Line line, Cell target) {
        if (target == null) {
            return -1;
        }
        for (int x = 0; x < line.getWidth(); x++) {
            if (line.getCell(x) == target) {
                return x;
            }
        }
        return -1;
    }

    private List<Line> wrapLogicalLines(List<List<Cell>> logicalLines, int newWidth) {
        List<Line> reflowedLines = new ArrayList<>();

        for (List<Cell> logicalLine : logicalLines) {
            if (logicalLine.isEmpty()) {
                reflowedLines.add(new Line(newWidth));
                continue;
            }

            for (int offset = 0; offset < logicalLine.size(); offset += newWidth) {
                Line line = new Line(newWidth);
                int chunkSize = Math.min(newWidth, logicalLine.size() - offset);

                for (int x = 0; x < chunkSize; x++) {
                    line.getCell(x).copyFrom(logicalLine.get(offset + x));
                }

                if (offset + chunkSize < logicalLine.size()) {
                    line.setWrapped(true);
                }
                reflowedLines.add(line);
            }
        }

        return reflowedLines;
    }

    public void write(String text) {
        CellAttributes attributes = currentAttributes();

        for (char character : text.toCharArray()) {
            if (character == '\n') {
                handleNewLine();
                continue;
            }

            wrapLineIfAtEdge();
            screen.setCell(cursorX, cursorY, character, attributes);
            cursorX++;
        }
    }

    public void insert(String text) {
        CellAttributes attributes = currentAttributes();

        for (char character : text.toCharArray()) {
            if (character == '\n') {
                handleNewLine();
                continue;
            }

            wrapLineIfAtEdge();
            screen.insertCharAt(cursorX, cursorY, character, attributes);
            cursorX++;
        }
    }

    private void handleNewLine() {
        if (cursorX >= screen.getWidth()) {
            cursorX = screen.getWidth() - 1;
        }
        screen.getLine(cursorY).setWrapped(false);
        cursorX = 0;
        cursorY++;
        handleScroll();
    }

    private void wrapLineIfAtEdge() {
        if (cursorX >= screen.getWidth()) {
            screen.getLine(cursorY).setWrapped(true);
            cursorX = 0;
            cursorY++;
            handleScroll();
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
        screen.fillLine(cursorY, character, currentAttributes());
    }

    private CellAttributes currentAttributes() {
        return new CellAttributes(currentForegroundColor, currentBackgroundColor, currentStyles);
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
        } else if (y < 0 && -y <= scrollback.getSize()) {
            return scrollback.getLine(y).toString();
        } else {
            throw new IndexOutOfBoundsException("Invalid row: " + y);
        }
    }

    private Cell getCellAt(int x, int y) {
        if (y >= 0 && y < screen.getHeight()) {
            return screen.getCell(x, y);
        } else if (y < 0 && -y <= scrollback.getSize()) {
            return scrollback.getCellAt(x, y);
        } else {
            throw new IndexOutOfBoundsException("Invalid coordinates: " + x + ", " + y);
        }
    }

    public void setCursorPosition(int column, int row) {
        this.cursorX = Math.clamp(column, 0, screen.getWidth() - 1);
        this.cursorY = Math.clamp(row, 0, screen.getHeight() - 1);
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