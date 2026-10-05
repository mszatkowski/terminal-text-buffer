package org.terminal;

import java.util.ArrayList;
import java.util.List;

class Screen {

    private final int width;
    private final int height;
    private final Line[] lines;
    private int topIndex;

    Screen(int width, int height) {
        this.width = width;
        this.height = height;
        this.lines = new Line[height];
        for (int i = 0; i < height; i++) {
            lines[i] = new Line(width);
        }
        this.topIndex = 0;
    }

    List<Line> getAllLines() {
        List<Line> result = new ArrayList<>(height);
        for (int y = 0; y < height; y++) {
            result.add(getLine(y));
        }
        return result;
    }

    Line getLine(int y) {
        return lines[(topIndex + y) % height];
    }

    char getCharacter(int x, int y) {
        return getLine(y).getCharacter(x);
    }

    CellAttributes getAttributes(int x, int y) {
        return getLine(y).getAttributes(x);
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    void clear() {
        for (Line line : lines) {
            line.clear();
        }
        topIndex = 0;
    }

    void fillLine(int row, char character, CellAttributes attributes) {
        getLine(row).fill(character, attributes);
    }

    Line scrollUp() {
        Line scrolledOutLine = lines[topIndex];
        lines[topIndex] = new Line(width);
        topIndex = (topIndex + 1) % height;
        return scrolledOutLine;
    }

    void setCell(int column, int row, char character, CellAttributes attributes) {
        getLine(row).setCell(column, character, attributes);
    }

    void insertCharAt(int column, int row, char character, CellAttributes attributes) {
        for (int y = height - 1; y >= row; y--) {
            int stopColumn = (y == row) ? column + 1 : 0;
            Line currentLine = getLine(y);

            for (int x = width - 1; x >= stopColumn; x--) {
                if (x == 0) {
                    Line previousLine = getLine(y - 1);
                    currentLine.copyCellFrom(0, previousLine, width - 1);
                } else {
                    currentLine.copyCellFrom(x, currentLine, x - 1);
                }
            }
        }
        setCell(column, row, character, attributes);
    }

    @Override
    public String toString() {
        int capacity = (width + 1) * height;
        StringBuilder stringBuilder = new StringBuilder(capacity);
        for (int i = 0; i < height; i++) {
            stringBuilder.append(getLine(i).toString());
            if (i < height - 1) {
                stringBuilder.append('\n');
            }
        }
        return stringBuilder.toString();
    }
}