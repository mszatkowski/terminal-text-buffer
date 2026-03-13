package org.terminal;

class Line {
    private final Cell[] cells;
    private boolean isWrapped;

    Line(int width) {
        cells = new Cell[width];
        for (int i = 0; i < width; i++) {
            cells[i] = new Cell('\0');
        }
        this.isWrapped = false;
    }

    Cell getCell(int index) {
        return cells[index];
    }

    void setCell(int index, Cell cell) {
        cells[index] = cell;
    }

    void clear() {
        for (Cell cell : cells) {
            cell.clear();
        }
        isWrapped = false;
    }

    void fill(char character, CellAttributes attributes) {
        for (Cell cell : cells) {
            cell.setCharacter(character);
            cell.setForegroundColor(attributes.foreground());
            cell.setBackgroundColor(attributes.background());
            cell.setStyles(attributes.styles());
        }
    }

    void setWrapped(boolean wrapped) {
        isWrapped = wrapped;
    }

    boolean isWrapped() {
        return isWrapped;
    }

    int getWidth() {
        return cells.length;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(cells.length);
        for (Cell cell : cells) {
            stringBuilder.append(cell.getPrintableCharacter());
        }
        return stringBuilder.toString();
    }
}
