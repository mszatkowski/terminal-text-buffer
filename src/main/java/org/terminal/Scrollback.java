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

    Line getLine(int index) {
        return lines.get(getSize() + index);
    }

    int getSize() {
        return lines.size();
    }

    int getWidth() {
        return width;
    }
}
