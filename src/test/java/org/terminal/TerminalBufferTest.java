package org.terminal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TerminalBufferTest {

    @ParameterizedTest(name = "Move {0} by {1} steps -> expect cursor at column {2}, row {3}")
    @CsvSource({
            "UP,    2, 5, 3",
            "DOWN,  4, 5, 9",
            "LEFT,  3, 2, 5",
            "RIGHT, 1, 6, 5"
    })
    void moveCursor_whenWithinBounds_shouldMoveCursor(String direction, int steps, int expectedColumn, int expectedRow) {
        int width = 10;
        int height = 10;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);
        buffer.setCursorPosition(5, 5);

        switch (direction) {
            case "UP" -> buffer.moveCursorUp(steps);
            case "DOWN" -> buffer.moveCursorDown(steps);
            case "LEFT" -> buffer.moveCursorLeft(steps);
            case "RIGHT" -> buffer.moveCursorRight(steps);
        }

        assertThat(buffer.getCursorColumn()).isEqualTo(expectedColumn);
        assertThat(buffer.getCursorRow()).isEqualTo(expectedRow);
    }

    @ParameterizedTest(name = "Move {0} by {1} steps -> expect cursor at column {2}, row {3}")
    @CsvSource({
            "UP,    4, 1, 0",
            "DOWN,  4, 1, 2",
            "LEFT,  4, 0, 1",
            "RIGHT, 4, 2, 1"
    })
    void moveCursor_whenDestinationIsOutOfBounds_shouldSnapToEdge(String direction, int steps, int expectedColumn, int expectedRow) {
        int width = 3;
        int height = 3;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);
        buffer.setCursorPosition(1, 1);

        switch (direction) {
            case "UP" -> buffer.moveCursorUp(steps);
            case "DOWN" -> buffer.moveCursorDown(steps);
            case "LEFT" -> buffer.moveCursorLeft(steps);
            case "RIGHT" -> buffer.moveCursorRight(steps);
        }

        assertThat(buffer.getCursorColumn()).isEqualTo(expectedColumn);
        assertThat(buffer.getCursorRow()).isEqualTo(expectedRow);
    }

    @Test
    void write_shouldMoveCursorToEndOfText() {
        int width = 3;
        int height = 1;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.write("AB");

        assertThat(buffer.getCursorColumn()).isEqualTo(2);
        assertThat(buffer.getCursorRow()).isEqualTo(0);
    }

    @Test
    void write_whenAtEndOfLine_shouldMoveCursorToNextLine() {
        int width = 3;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.write("ABC");

        assertThat(buffer.getCursorColumn()).isEqualTo(3);
        assertThat(buffer.getCursorRow()).isEqualTo(0);
    }

    @Test
    void write_whenTextFitsWithinWidth_shouldWriteText() {
        int width = 3;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.write("AB");

        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "AB \n" +
                        "   "
        );
    }

    @Test
    void write_whenTextExceedsWidth_shouldWrapToNextLine() {
        int width = 3;
        int height = 3;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.write("ABCD");

        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "ABC\n" +
                        "D  \n" +
                        "   "
        );
    }

    @Test
    void write_whenTextExceedsHeight_shouldScrollUp() {
        int width = 2;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.write("ABC");
        buffer.write("DE");

        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "CD\n" +
                        "E "
        );
    }

    @Test
    void write_whenThereAlreadyIsText_shouldOverwriteExistingText() {
        int width = 2;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.write("AB");
        buffer.moveCursorUp(1);
        buffer.write("C");

        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "AC\n" +
                        "  "
        );
    }

    @Test
    void write_whenEncountersNewLine_shouldMoveToNewLine() {
        int width = 2;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.write("A\nB");

        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "A \n" +
                        "B ");
    }

    @Test
    void write_whenGivenEmptyString_shouldDoNothing() {
        int width = 2;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.write("");

        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "  \n" +
                        "  ");
    }

    @Test
    void write_whenUsingAttributes_shouldApplyAttributes() {
        int width = 2;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.addStyle(Style.BOLD);
        buffer.setForegroundColor(TerminalColor.RED);
        buffer.setBackgroundColor(TerminalColor.GREEN);

        CellAttributes attributes = new CellAttributes(
                TerminalColor.RED,
                TerminalColor.GREEN,
                Set.of(Style.BOLD)
        );
        CellAttributes defaultAttributes = new CellAttributes(
                TerminalColor.DEFAULT,
                TerminalColor.DEFAULT,
                Set.of()
        );

        buffer.write("AB");

        assertThat(buffer.getCellAttributesAt(0, 0)).isEqualTo(attributes);
        assertThat(buffer.getCellAttributesAt(1, 0)).isEqualTo(attributes);
        assertThat(buffer.getCellAttributesAt(0, 1)).isEqualTo(defaultAttributes);
        assertThat(buffer.getCellAttributesAt(1, 1)).isEqualTo(defaultAttributes);
    }

    @Test
    void insert_shouldMoveCursorToEndOfText() {
        int width = 3;
        int height = 1;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.insert("AB");

        assertThat(buffer.getCursorColumn()).isEqualTo(2);
        assertThat(buffer.getCursorRow()).isEqualTo(0);
    }

    @Test
    void insert_whenAtEndOfLine_shouldMoveCursorToNextLine() {
        int width = 3;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.insert("ABC");

        assertThat(buffer.getCursorColumn()).isEqualTo(3);
        assertThat(buffer.getCursorRow()).isEqualTo(0);
    }

    @Test
    void insert_whenTextFitsWithinWidth_shouldWriteText() {
        int width = 3;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.insert("AB");

        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "AB \n" +
                        "   "
        );
    }

    @Test
    void insert_whenTextFitsWithinWidth_shouldInsertText() {
        int width = 3;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.insert("AB");
        buffer.moveCursorLeft(2);
        buffer.insert("C");

        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "CAB\n" +
                        "   "
        );
    }

    @Test
    void insert_whenTextExceedsWidth_shouldWrapToNextLine() {
        int width = 3;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.insert("AB");
        buffer.moveCursorLeft(2);
        buffer.insert("CD");

        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "CDA\n" +
                        "B  "
        );
    }

    @Test
    void insert_whenAtBottomRight_shouldVoidMovedCell() {
        int width = 2;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.insert("ABC");
        buffer.moveCursorLeft(1);
        buffer.insert("DE");

        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "AB\n" +
                        "DE"
        );
    }

    @Test
    void insert_whenEncountersNewLine_shouldMoveToNewLine() {
        int width = 2;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.insert("A\nB");

        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "A \n" +
                        "B ");
    }

    @Test
    void insert_whenGivenEmptyString_shouldDoNothing() {
        int width = 2;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.insert("");

        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "  \n" +
                        "  ");
    }

    @Test
    void insert_whenUsingAttributes_shouldApplyAttributes() {
        int width = 2;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.addStyle(Style.BOLD);
        buffer.setForegroundColor(TerminalColor.RED);
        buffer.setBackgroundColor(TerminalColor.GREEN);

        CellAttributes attributes = new CellAttributes(
                TerminalColor.RED,
                TerminalColor.GREEN,
                Set.of(Style.BOLD)
        );

        CellAttributes defaultAttributes = new CellAttributes(
                TerminalColor.DEFAULT,
                TerminalColor.DEFAULT,
                Set.of()
        );

        buffer.insert("AB");

        assertThat(buffer.getCellAttributesAt(0, 0)).isEqualTo(attributes);
        assertThat(buffer.getCellAttributesAt(1, 0)).isEqualTo(attributes);
        assertThat(buffer.getCellAttributesAt(0, 1)).isEqualTo(defaultAttributes);
        assertThat(buffer.getCellAttributesAt(1, 1)).isEqualTo(defaultAttributes);
    }

    @Test
    void clearScreen_shouldClearEntireBuffer() {
        int width = 2;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        CellAttributes defaultAttributes = new CellAttributes(
                TerminalColor.DEFAULT,
                TerminalColor.DEFAULT,
                Set.of()
        );

        buffer.setForegroundColor(TerminalColor.RED);
        buffer.setBackgroundColor(TerminalColor.GREEN);
        buffer.addStyle(Style.BOLD);

        buffer.write("AB");

        buffer.clearScreen();

        assertThat(buffer.getCellAttributesAt(0, 0)).isEqualTo(defaultAttributes);
        assertThat(buffer.getCellAttributesAt(1, 0)).isEqualTo(defaultAttributes);
        assertThat(buffer.getCellAttributesAt(0, 1)).isEqualTo(defaultAttributes);
        assertThat(buffer.getCellAttributesAt(1, 1)).isEqualTo(defaultAttributes);

        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "  \n" +
                        "  ");
    }

    @Test
    void fillLine_shouldFillLineWithCharacterUsingAttributes() {
        int width = 2;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        CellAttributes attributes = new CellAttributes(
                TerminalColor.RED,
                TerminalColor.GREEN,
                Set.of(Style.BOLD)
        );
        CellAttributes defaultAttributes = new CellAttributes(
                TerminalColor.DEFAULT,
                TerminalColor.DEFAULT,
                Set.of()
        );

        buffer.setForegroundColor(TerminalColor.RED);
        buffer.setBackgroundColor(TerminalColor.GREEN);
        buffer.addStyle(Style.BOLD);

        buffer.fillLine('A');

        assertThat(buffer.getCellAttributesAt(0, 0)).isEqualTo(attributes);
        assertThat(buffer.getCellAttributesAt(1, 0)).isEqualTo(attributes);
        assertThat(buffer.getCellAttributesAt(0, 1)).isEqualTo(defaultAttributes);
        assertThat(buffer.getCellAttributesAt(1, 1)).isEqualTo(defaultAttributes);

        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "AA\n" +
                        "  ");
    }

    @Test
    void insertEmptyLineAtBottom_shouldInsertEmptyLineAtBottomAndPushTopLineToScrollback() {
        int width = 2;
        int height = 2;
        int maxScrollback = 1;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.fillLine('A');
        buffer.moveCursorDown(1);
        buffer.fillLine('B');

        buffer.insertEmptyLineAtBottom();


        assertThat(buffer.getScreenAsString()).isEqualTo(
                "BB\n" +
                        "  "
        );
        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "AA\n" +
                        "BB\n" +
                        "  "
        );
    }

    @Test
    void insertEmptyLineAtBottom_whenScrollbackIsFull_shouldVoidTheOldestLine() {
        int width = 2;
        int height = 1;
        int maxScrollback = 1;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.fillLine('A');
        buffer.insertEmptyLineAtBottom();
        buffer.fillLine('B');
        buffer.insertEmptyLineAtBottom();

        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "BB\n" +
                        "  "
        );
    }

    @Test
    void getCellAt_whenWithinBounds_shouldReturnCell() {
        int width = 2;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.write("AB");

        String line = buffer.getLineAsString(0);

        assertThat(line).isEqualTo("AB");
    }

    @Test
    void getLineAsString_whenOutsideBounds_shouldThrowOutOfBoundsException() {
        int width = 2;
        int height = 2;
        int maxScrollback = 0;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        assertThatThrownBy(() -> buffer.getLineAsString(10))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessage("Invalid row: 10");
    }

    @Test
    void resize_whenDecreasingHeight_shouldSpillToScrollback() {
        int width = 2;
        int height = 3;
        int maxScrollback = 1;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.write("AA\nBB\nC");

        buffer.resizeScreen(2, 2);

        assertThat(buffer.getEntireContentAsString()).isEqualTo(
                "AA\n" +
                        "BB\n" +
                        "C "
        );
        assertThat(buffer.getScreenAsString()).isEqualTo(
                "BB\n" +
                        "C "
        );
    }

    @Test
    void resize_whenIncreasingHeight_shouldKeepContentAndAddEmptyLinesAtBottom() {
        TerminalBuffer buffer = new TerminalBuffer(2, 2, 2);
        buffer.write("A");

        buffer.resizeScreen(2, 4);

        assertThat(buffer.getScreenAsString()).isEqualTo(
                "A \n" +
                        "  \n" +
                        "  \n" +
                        "  "
        );
        assertThat(buffer.getScrollbackAsString()).isEmpty();
    }

    @Test
    void resize_whenDecreasingWidth_shouldWrapLinesAndSpillToScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 5);
        buffer.write("12345678");

        buffer.resizeScreen(2, 2);

        assertThat(buffer.getScrollbackAsString()).isEqualTo(
                "12\n" +
                        "34"
        );
        assertThat(buffer.getScreenAsString()).isEqualTo(
                "56\n" +
                        "78"
        );
    }

    @Test
    void resize_whenIncreasingWidth_shouldUnwrapLines() {
        TerminalBuffer buffer = new TerminalBuffer(2, 2, 2);
        buffer.write("1234");

        buffer.resizeScreen(4, 2);

        assertThat(buffer.getScreenAsString()).isEqualTo(
                "1234\n" +
                        "    "
        );
    }

    @Test
    void resize_whenDecreasingHeightExceedsMaxScrollback_shouldDropOldestLines() {
        int width = 2;
        int height = 4;
        int maxScrollback = 1;
        TerminalBuffer buffer = new TerminalBuffer(width, height, maxScrollback);

        buffer.write("AA\nBB\nCC\nDD");

        buffer.resizeScreen(2, 1);

        assertThat(buffer.getScrollbackAsString()).isEqualTo("CC");
        assertThat(buffer.getScreenAsString()).isEqualTo("DD");
    }

    @Test
    void resize_shouldKeepCursorOnCorrectCharacterWhenWrapping() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 2);
        buffer.write("ABC");

        assertThat(buffer.getCursorColumn()).isEqualTo(3);
        assertThat(buffer.getCursorRow()).isEqualTo(0);

        buffer.resizeScreen(2, 2);

        assertThat(buffer.getCursorColumn()).isEqualTo(1);
        assertThat(buffer.getCursorRow()).isEqualTo(0);
    }

    @Test
    void resize_shouldKeepCursorOnCorrectCharacterWhenUnwrapping() {
        TerminalBuffer buffer = new TerminalBuffer(2, 2, 2);
        buffer.write("ABC");

        assertThat(buffer.getCursorColumn()).isEqualTo(1);
        assertThat(buffer.getCursorRow()).isEqualTo(1);

        buffer.resizeScreen(4, 2);

        assertThat(buffer.getCursorColumn()).isEqualTo(3);
        assertThat(buffer.getCursorRow()).isEqualTo(0);
    }
}
