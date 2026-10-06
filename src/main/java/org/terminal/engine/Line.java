package org.terminal.engine;

import java.util.Arrays;

class Line {
    private final char[] characters;
    private final CellAttributes[] attributes;
    private boolean isWrapped;
    private boolean hasContent;

    Line(int width) {
        this.characters = new char[width];
        this.attributes = new CellAttributes[width];
        Arrays.fill(this.characters, '\0');
        Arrays.fill(this.attributes, CellAttributes.DEFAULT);
        this.isWrapped = false;
        this.hasContent = false;
    }

    char getCharacter(int index) {
        return characters[index];
    }

    void setCharacter(int index, char character) {
        characters[index] = character;
        if (character != '\0') {
            hasContent = true;
        }
    }

    CellAttributes getAttributes(int index) {
        return attributes[index];
    }

    void setAttributes(int index, CellAttributes attributes) {
        this.attributes[index] = attributes;
        if (!attributes.equals(CellAttributes.DEFAULT)) {
            hasContent = true;
        }
    }

    void setCell(int index, char character, CellAttributes attr) {
        characters[index] = character;
        attributes[index] = attr;
        if (character != '\0' || !attr.equals(CellAttributes.DEFAULT)) {
            hasContent = true;
        }
    }

    void copyCellFrom(int targetIndex, Line sourceLine, int sourceIndex) {
        char c = sourceLine.characters[sourceIndex];
        CellAttributes attr = sourceLine.attributes[sourceIndex];

        this.characters[targetIndex] = c;
        this.attributes[targetIndex] = attr;

        if (c != '\0' || !attr.equals(CellAttributes.DEFAULT)) {
            hasContent = true;
        }
    }

    void clear() {
        Arrays.fill(characters, '\0');
        Arrays.fill(attributes, CellAttributes.DEFAULT);
        isWrapped = false;
        hasContent = false;
    }

    void fill(char character, CellAttributes attr) {
        Arrays.fill(characters, character);
        Arrays.fill(attributes, attr);
        this.hasContent = (character != '\0' || !attr.equals(CellAttributes.DEFAULT));
    }

    boolean isEmpty() {
        return !hasContent;
    }

    boolean isDefaultAt(int index) {
        return characters[index] == '\0' && attributes[index].equals(CellAttributes.DEFAULT);
    }

    void setWrapped(boolean wrapped) {
        isWrapped = wrapped;
    }

    boolean isWrapped() {
        return isWrapped;
    }

    int getWidth() {
        return characters.length;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(characters.length);
        for (char character : characters) {
            stringBuilder.append(character == '\0' ? ' ' : character);
        }
        return stringBuilder.toString();
    }
}