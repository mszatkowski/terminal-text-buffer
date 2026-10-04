package org.terminal;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class CellAttributes {

    private final TerminalColor foreground;
    private final TerminalColor background;
    private final Set<Style> styles;

    private static final Map<CellAttributes, CellAttributes> POOL = new HashMap<>();

    public static final CellAttributes DEFAULT = of(
            TerminalColor.DEFAULT,
            TerminalColor.DEFAULT,
            Collections.emptySet()
    );

    private CellAttributes(TerminalColor foreground, TerminalColor background, Set<Style> styles) {
        this.foreground = Objects.requireNonNull(foreground);
        this.background = Objects.requireNonNull(background);
        this.styles = styles.isEmpty()
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(styles));
    }

    public static CellAttributes of(TerminalColor foreground, TerminalColor background, Set<Style> styles) {
        CellAttributes candidate = new CellAttributes(foreground, background, styles);
        return POOL.computeIfAbsent(candidate, key -> key);
    }

    public TerminalColor foreground() {
        return foreground;
    }

    public TerminalColor background() {
        return background;
    }

    public Set<Style> styles() {
        return styles;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CellAttributes that = (CellAttributes) o;
        return foreground == that.foreground && background == that.background && Objects.equals(styles, that.styles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreground, background, styles);
    }
}