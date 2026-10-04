package org.terminal;

import java.util.ArrayList;
import java.util.List;

class LogicalLine {

    private final StringBuilder characters = new StringBuilder();
    private final List<CellAttributes> attributes = new ArrayList<>();

    void append(char character, CellAttributes attribute) {
        characters.append(character);
        attributes.add(attribute);
    }

    char getCharacter(int index) {
        return characters.charAt(index);
    }

    CellAttributes getAttributes(int index) {
        return attributes.get(index);
    }

    int length() {
        return characters.length();
    }

    boolean isEmpty() {
        return characters.isEmpty();
    }
}