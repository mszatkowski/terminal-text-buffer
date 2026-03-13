package org.terminal;

import java.util.Set;

public record CellAttributes(
        TerminalColor foreground,
        TerminalColor background,
        Set<Style> styles
) {}
