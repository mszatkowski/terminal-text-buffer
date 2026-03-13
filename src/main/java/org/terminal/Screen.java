package org.terminal;

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

    public void fillLine(int row, char character, CellAttributes attributes) {
        getLine(row).fill(character, attributes);
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
