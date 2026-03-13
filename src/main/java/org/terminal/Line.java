package org.terminal;

class Line {
    private final Cell[] cells;

    Line(int width) {
        cells = new Cell[width];
        for (int i = 0; i < width; i++) {
            cells[i] = new Cell('\0');
        }
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
    }

    void fill(char character, CellAttributes attributes) {
        for (Cell cell : cells) {
            cell.setCharacter(character);
            cell.setForegroundColor(attributes.foreground());
            cell.setBackgroundColor(attributes.background());
            cell.setStyles(attributes.styles());
        }
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
