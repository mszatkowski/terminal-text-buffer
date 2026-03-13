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

    Cell getCell(int x, int y) {
        return lines[y].getCell(x);
    }

    Line getLine(int y) {
        return lines[y];
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
        Cell cell = getCell(column, row);
        cell.setCharacter(character);
        cell.setForegroundColor(attributes.foreground());
        cell.setBackgroundColor(attributes.background());
        cell.setStyles(attributes.styles());
    }

    void insertCharAt(int column, int row, char character, CellAttributes attributes) {
        for (int y = height - 1; y >= row; y--) {
            int stopColumn = y == row ? column + 1 : 0;

            for (int x = width - 1; x >= stopColumn; x--) {
                Cell currentCell = getCell(x, y);
                Cell previousCell;

                if (x == 0) {
                    previousCell = getCell(width - 1, y - 1);
                } else {
                    previousCell = getCell(x - 1, y);
                }

                currentCell.copyFrom(previousCell);
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
