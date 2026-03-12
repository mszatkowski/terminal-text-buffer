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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
