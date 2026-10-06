package org.terminal.engine;

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

        int safeCursorX = Math.min(cursorX, screen.getWidth() - 1);
        int safeCursorY = Math.min(cursorY, screen.getHeight() - 1);

        List<LogicalLine> unwrappedScreen = unwrapLines(screen.getAllLines(), safeCursorX, safeCursorY);
        List<Line> newScreenLines = wrapLogicalLines(unwrappedScreen, newWidth);

        int spillCount = Math.max(0, newScreenLines.size() - newHeight);
        spillLinesToScrollback(newScreenLines, spillCount, newScrollback);

        Screen newScreen = buildNewScreen(newScreenLines, spillCount, newWidth, newHeight);
        updateCursorPosition(unwrappedScreen, newWidth, newHeight, spillCount, newScreenLines.size());

        this.screen = newScreen;
        this.scrollback = newScrollback;
    }

    private Scrollback reflowScrollback(int newWidth) {
        Scrollback newScrollback = new Scrollback(scrollback.getMaxLines(), newWidth);
        List<LogicalLine> unwrapped = unwrapLines(scrollback.getAllLines(), -1, -1);
        List<Line> reflowedLines = wrapLogicalLines(unwrapped, newWidth);

        for (Line line : reflowedLines) {
            newScrollback.push(line);
        }
        return newScrollback;
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
                destinationLine.copyCellFrom(x, sourceLine, x);
            }
            destinationLine.setWrapped(sourceLine.isWrapped());
            targetRow++;
        }
        return newScreen;
    }

    private void updateCursorPosition(List<LogicalLine> unwrappedScreen, int newWidth, int newHeight,
                                      int spillCount, int totalScreenLines) {
        int safeX = Math.min(cursorX, screen.getWidth() - 1);
        int safeY = Math.min(cursorY, screen.getHeight() - 1);

        int startRow = safeY;
        while (startRow > 0 && screen.getLine(startRow - 1).isWrapped()) {
            startRow--;
        }

        int logicalLineIndex = 0;
        for (int y = 0; y < startRow; y++) {
            if (!screen.getLine(y).isWrapped()) {
                logicalLineIndex++;
            }
        }

        int cursorOffset = (safeY - startRow) * screen.getWidth() + safeX;

        int linesBefore = 0;
        for (int i = 0; i < logicalLineIndex && i < unwrappedScreen.size(); i++) {
            LogicalLine line = unwrappedScreen.get(i);
            linesBefore += line.isEmpty() ? 1 : (line.length() + newWidth - 1) / newWidth;
        }

        this.cursorX = cursorOffset % newWidth;
        int adjustedY = linesBefore + (cursorOffset / newWidth) - spillCount;

        if (adjustedY < 0) {
            this.cursorY = 0;
        } else if (adjustedY >= totalScreenLines) {
            this.cursorY = newHeight - 1;
        } else {
            this.cursorY = adjustedY;
        }
    }

    private List<LogicalLine> unwrapLines(List<Line> lines, int cursorTargetX, int cursorTargetY) {
        List<LogicalLine> unwrapped = new ArrayList<>();
        LogicalLine currentLogicalLine = new LogicalLine();

        for (int y = 0; y < lines.size(); y++) {
            Line line = lines.get(y);
            int effectiveWidth = calculateEffectiveLineWidth(line, y, cursorTargetX, cursorTargetY);

            for (int x = 0; x < effectiveWidth; x++) {
                currentLogicalLine.append(line.getCharacter(x), line.getAttributes(x));
            }

            if (!line.isWrapped()) {
                unwrapped.add(currentLogicalLine);
                currentLogicalLine = new LogicalLine();
            }
        }

        if (!currentLogicalLine.isEmpty()) {
            unwrapped.add(currentLogicalLine);
        }

        return unwrapped;
    }

    private int calculateEffectiveLineWidth(Line line, int rowIndex, int cursorTargetX, int cursorTargetY) {
        if (line.isWrapped()) {
            return line.getWidth();
        }

        int lastContentIndex = findLastNonDefaultCellIndex(line);
        int cursorColumn = (rowIndex == cursorTargetY) ? cursorTargetX : -1;

        int lastValidIndex = Math.max(lastContentIndex, cursorColumn);
        return Math.min(line.getWidth(), lastValidIndex + 1);
    }

    private int findLastNonDefaultCellIndex(Line line) {
        int index = line.getWidth() - 1;
        while (index >= 0 && line.isDefaultAt(index)) {
            index--;
        }
        return index;
    }

    private List<Line> wrapLogicalLines(List<LogicalLine> logicalLines, int newWidth) {
        List<Line> reflowedLines = new ArrayList<>();

        for (LogicalLine logicalLine : logicalLines) {
            if (logicalLine.isEmpty()) {
                reflowedLines.add(new Line(newWidth));
                continue;
            }

            for (int offset = 0; offset < logicalLine.length(); offset += newWidth) {
                Line line = new Line(newWidth);
                int chunkSize = Math.min(newWidth, logicalLine.length() - offset);

                for (int x = 0; x < chunkSize; x++) {
                    line.setCell(x, logicalLine.getCharacter(offset + x), logicalLine.getAttributes(offset + x));
                }

                if (offset + chunkSize < logicalLine.length()) {
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
        return CellAttributes.of(currentForegroundColor, currentBackgroundColor, currentStyles);
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
        if (row >= 0 && row < screen.getHeight()) {
            return screen.getCharacter(column, row);
        } else if (row < 0 && -row <= scrollback.getSize()) {
            return scrollback.getCharacterAt(column, row);
        } else {
            throw new IndexOutOfBoundsException("Invalid coordinates: " + column + ", " + row);
        }
    }

    public CellAttributes getCellAttributesAt(int column, int row) {
        if (row >= 0 && row < screen.getHeight()) {
            return screen.getAttributes(column, row);
        } else if (row < 0 && -row <= scrollback.getSize()) {
            return scrollback.getAttributesAt(column, row);
        } else {
            throw new IndexOutOfBoundsException("Invalid coordinates: " + column + ", " + row);
        }
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

    public int getWidth() {
        return screen.getWidth();
    }

    public int getHeight() {
        return screen.getHeight();
    }
}