package org.terminal;

import java.util.Arrays;
import java.util.List;

class Screen {

    private final int width;
    private final int height;
    private final Line[] lines;

    Screen(int width, int height) {
        this.width = width;
        this.height = height;
        lines = new Line[height];
        for (int i = 0; i < height; i++) {
            lines[i] = new Line(width);
        }
    }

    List<Line> getAllLines() {
        return Arrays.asList(lines);
    }

    Line getLine(int y) {
        return lines[y];
    }

    char getCharacter(int x, int y) {
        return lines[y].getCharacter(x);
    }

    CellAttributes getAttributes(int x, int y) {
        return lines[y].getAttributes(x);
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
    }

    void fillLine(int row, char character, CellAttributes attributes) {
        getLine(row).fill(character, attributes);
    }

    Line scrollUp() {
        Line line = lines[0];

        for (int i = 1; i < height; i++) {
            lines[i - 1] = lines[i];
        }

        lines[height - 1] = new Line(width);

        return line;
    }

    void setCell(int column, int row, char character, CellAttributes attributes) {
        lines[row].setCell(column, character, attributes);
    }

    void insertCharAt(int column, int row, char character, CellAttributes attributes) {
        for (int y = height - 1; y >= row; y--) {
            int stopColumn = (y == row) ? column + 1 : 0;
            Line currentLine = lines[y];

            for (int x = width - 1; x >= stopColumn; x--) {
                if (x == 0) {
                    Line previousLine = lines[y - 1];
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
        for (int i = 0; i < lines.length; i++) {
            stringBuilder.append(lines[i].toString());
            if (i < lines.length - 1) {
                stringBuilder.append('\n');
            }
        }
        return stringBuilder.toString();
    }
}