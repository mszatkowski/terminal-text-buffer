# Terminal Text Buffer

A core Java data structure for terminal emulators. This project implements the underlying grid system used to store, manipulate, and render text in a command-line interface, complete with cursor management, text styling, and dynamic resizing.

## Features

* **Screen & Scrollback Separation:** Accurately models a terminal by splitting the active, fixed-size grid (Screen) from the rolling history log (Scrollback).
* **Accurate Cursor Management:** Implements standard VT100 behavior, including the "pending wrap" state at the right edge of the screen to prevent accidental blank lines.
* **Text Styling & Colors:** Supports the standard 16 terminal colors (foreground and background) and text styles like `BOLD`, `ITALIC`, and `UNDERLINE`.
* **Dynamic Reflow Resizing:** Intelligently unwraps and re-wraps text when the terminal dimensions change, ensuring text isn't lost and the cursor remains on the correct character.
* **Editing Modes:** Supports both `write` (overwriting existing characters) and `insert` (shifting existing characters to the right).

## Example Usage

```java
// Initialize an 80x24 terminal with 1000 lines of scrollback
TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);

// Apply styles and write text
buffer.setForegroundColor(TerminalColor.BRIGHT_GREEN);
buffer.addStyle(Style.BOLD);
buffer.write("System initialized.\n");

// Insert text and reflow
buffer.insert("Awaiting input...");

// Resize the terminal (text will automatically reflow to fit)
buffer.resizeScreen(40, 24);

// Get the current screen output
System.out.println(buffer.getScreenAsString());