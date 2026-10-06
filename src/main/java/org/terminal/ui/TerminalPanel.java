package org.terminal.ui;

import org.terminal.engine.CellAttributes;
import org.terminal.engine.Style;
import org.terminal.engine.TerminalBuffer;
import org.terminal.engine.TerminalColor;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TerminalPanel extends JPanel {

    private final TerminalBuffer buffer;
    private final Font baseFont;

    private int charWidth;
    private int charHeight;
    private int charAscent;

    public TerminalPanel(TerminalBuffer buffer) {
        this.buffer = buffer;
        this.baseFont = new Font(Font.MONOSPACED, Font.PLAIN, 16);

        setBackground(ColorPalette.DEFAULT_BACKGROUND);
        setFocusable(true);

        calculateFontMetrics();

        setPreferredSize(new Dimension(
                buffer.getWidth() * charWidth,
                buffer.getHeight() * charHeight
        ));

        setupKeyBindings();
        setupResizeListener();
    }

    private void calculateFontMetrics() {
        FontMetrics metrics = getFontMetrics(baseFont);
        this.charWidth = metrics.charWidth('W');
        this.charHeight = metrics.getHeight();
        this.charAscent = metrics.getAscent();
    }

    private void setupKeyBindings() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
                repaint();
            }

            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c >= 32 && c != 127) {
                    buffer.write(String.valueOf(c));
                    repaint();
                }
            }
        });
    }

    private void handleKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER -> buffer.write("\n");
            case KeyEvent.VK_LEFT -> buffer.moveCursorLeft(1);
            case KeyEvent.VK_RIGHT -> buffer.moveCursorRight(1);
            case KeyEvent.VK_UP -> buffer.moveCursorUp(1);
            case KeyEvent.VK_DOWN -> buffer.moveCursorDown(1);
            case KeyEvent.VK_BACK_SPACE -> {
                if (buffer.getCursorColumn() > 0) {
                    buffer.moveCursorLeft(1);
                    buffer.write(" ");
                    buffer.moveCursorLeft(1);
                }
            }
        }
    }

    private void setupResizeListener() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (charWidth == 0 || charHeight == 0) return;

                int newWidth = Math.max(1, getWidth() / charWidth);
                int newHeight = Math.max(1, getHeight() / charHeight);

                if (newWidth != buffer.getWidth() || newHeight != buffer.getHeight()) {
                    buffer.resizeScreen(newWidth, newHeight);
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        renderCells(g2d);
        renderCursor(g2d);
    }

    private void renderCells(Graphics2D g2d) {
        for (int y = 0; y < buffer.getHeight(); y++) {
            for (int x = 0; x < buffer.getWidth(); x++) {
                CellAttributes attributes = buffer.getCellAttributesAt(x, y);
                char character = buffer.getCharacterAtPosition(x, y);

                int cellX = x * charWidth;
                int cellY = y * charHeight;

                if (attributes.background() != TerminalColor.DEFAULT) {
                    g2d.setColor(ColorPalette.getBackground(attributes.background()));
                    g2d.fillRect(cellX, cellY, charWidth, charHeight);
                }

                if (character != '\0' && character != ' ') {
                    applyFontStyle(g2d, attributes);
                    g2d.setColor(ColorPalette.getForeground(attributes.foreground()));
                    g2d.drawString(String.valueOf(character), cellX, cellY + charAscent);

                    if (attributes.styles().contains(Style.UNDERLINE)) {
                        int underlineY = cellY + charAscent + 2;
                        g2d.drawLine(cellX, underlineY, cellX + charWidth, underlineY);
                    }
                }
            }
        }
    }

    private void applyFontStyle(Graphics2D g2d, CellAttributes attributes) {
        int fontStyle = Font.PLAIN;
        if (attributes.styles().contains(Style.BOLD)) fontStyle |= Font.BOLD;
        if (attributes.styles().contains(Style.ITALIC)) fontStyle |= Font.ITALIC;
        g2d.setFont(baseFont.deriveFont(fontStyle));
    }

    private void renderCursor(Graphics2D g2d) {
        int cursorX = buffer.getCursorColumn();
        int cursorY = buffer.getCursorRow();

        if (cursorX < buffer.getWidth() && cursorY < buffer.getHeight()) {
            int px = cursorX * charWidth;
            int py = cursorY * charHeight;

            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.fillRect(px, py, charWidth, charHeight);

            char c = buffer.getCharacterAtPosition(cursorX, cursorY);
            if (c != '\0' && c != ' ') {
                g2d.setColor(Color.BLACK);
                g2d.setFont(baseFont);
                g2d.drawString(String.valueOf(c), px, py + charAscent);
            }
        }
    }
}