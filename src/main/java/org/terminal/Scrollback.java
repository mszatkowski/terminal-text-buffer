package org.terminal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Scrollback {

    private final int maxLines;
    private final int width;
    private final Line[] buffer;
    private int head;
    private int size;

    public Scrollback(int maxLines, int width) {
        this.maxLines = maxLines;
        this.width = width;
        this.buffer = new Line[maxLines];
        this.head = 0;
        this.size = 0;
    }

    void push(Line line) {
        if (maxLines == 0) return;

        if (size == maxLines) {
            buffer[head] = line;
            head = (head + 1) % maxLines;
        } else {
            int writeIndex = (head + size) % maxLines;
            buffer[writeIndex] = line;
            size++;
        }
    }

    char getCharacterAt(int x, int y) {
        return getLine(y).getCharacter(x);
    }

    CellAttributes getAttributesAt(int x, int y) {
        return getLine(y).getAttributes(x);
    }

    Line getLine(int index) {
        int logicalIndex = size + index;
        if (logicalIndex < 0 || logicalIndex >= size) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
        return buffer[(head + logicalIndex) % maxLines];
    }

    int getSize() {
        return size;
    }

    int getWidth() {
        return width;
    }

    void clear() {
        Arrays.fill(buffer, null);
        head = 0;
        size = 0;
    }

    int getMaxLines() {
        return maxLines;
    }

    List<Line> getAllLines() {
        List<Line> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(buffer[(head + i) % maxLines]);
        }
        return result;
    }

    @Override
    public String toString() {
        int capacity = (width + 1) * size;
        StringBuilder stringBuilder = new StringBuilder(capacity);
        for (int i = 0; i < size; i++) {
            stringBuilder.append(buffer[(head + i) % maxLines].toString());
            if (i < size - 1) {
                stringBuilder.append('\n');
            }
        }
        return stringBuilder.toString();
    }
}