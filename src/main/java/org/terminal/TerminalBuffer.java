package org.terminal;

public class TerminalBuffer {

    private final int SCREEN_WIDTH;
    private final int SCREEN_HEIGHT;
    private final int SCREEN_MAX_SCROLLBACK;

    public TerminalBuffer(int screenWidth, int screenHeight, int screenMaxScrollback) {
        SCREEN_WIDTH = screenWidth;
        SCREEN_HEIGHT = screenHeight;
        SCREEN_MAX_SCROLLBACK = screenMaxScrollback;
    }
}
