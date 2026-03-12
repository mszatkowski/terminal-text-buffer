package org.terminal;

import java.util.ArrayList;
import java.util.List;

class Scrollback {

    private final int maxLines;
    private final int width;
    private final List<Line> lines;

    public Scrollback(int maxLines, int width) {
        this.maxLines = maxLines;
        this.width = width;
        this.lines = new ArrayList<>();
    }

    void push(Line line) {
        lines.add(line);

        if (lines.size() > maxLines) {
            lines.removeFirst();
        }
    }

    Cell getCellAt(int x, int y) {
        return getLine(y).getCell(x);
    }

    Line getLine(int index) {
        return lines.get(getSize() + index);
    }

    int getSize() {
        return lines.size();
    }

    int getWidth() {
        return width;
    }

    @Override
    public String toString() {
        int capacity = (width + 1) * getSize();
        StringBuilder stringBuilder = new StringBuilder(capacity);
        for (int i = 0; i < getSize(); i++) {
            stringBuilder.append(lines.get(i).toString());
            if (i < getSize() - 1) {
                stringBuilder.append('\n');
            }
        }
        return stringBuilder.toString();
    }
}
