# Terminal Text Buffer

A Java implementation of a core text buffer for a terminal emulator. It manages the 2D character grid, scrollback history, cursor movement, text styling, and dynamic screen resizing (reflow).

The project focuses on data structure efficiency and clean architecture, featuring zero-allocation cell storage, constant-time scrolling, and a lightweight Swing-based GUI for visual demonstration.

## Features

* **Screen & Scrollback:** Splits the terminal state into an active, fixed-size grid (screen) and a rolling history log (scrollback).
* **Dynamic Reflow:** Unwraps and re-wraps text when the terminal is resized, preserving text layout and adjusting the cursor to stay on the correct character.
* **Editing Modes:** Supports both standard overwriting (`write`) and character shifting (`insert`).
* **Text Styling & Colors:** Supports 16 standard terminal colors (foreground/background) and text styles (`BOLD`, `ITALIC`, `UNDERLINE`).
* **Cursor Navigation:** Standard cursor movement (up, down, left, right) with proper boundary handling.
* **Interactive Swing GUI:** A standalone graphical interface to demonstrate the buffer, type text, and test live reflow by resizing the window.

## Under the Hood

A few design choices made to keep the buffer lightweight and fast:

* **Parallel arrays instead of `Cell` objects:** To reduce Garbage Collector pressure, each line stores characters and styles in primitive parallel arrays (`char[]` and `CellAttributes[]`).
* **Flyweight pattern for attributes:** Most characters share default or identical styles. `CellAttributes` instances are cached and reused via a factory method (`CellAttributes.of(...)`), keeping memory allocations very low during heavy text streaming.
* **Ring buffers for scrolling:** Both the active screen and the scrollback history are backed by circular arrays with modulo indexing. This makes scrolling an $O(1)$ pointer increment instead of shifting elements across the array.
* **Decoupled Architecture:** The core engine (`org.terminal`) has no UI dependencies and encapsulates internal line structures, exposing a clean public API consumed by the presentation layer (`org.terminal.ui`).

## Getting started

### Run the Interactive Demo
You can launch the visual terminal window to test typing and live window resizing:

```bash
mvn clean compile exec:java -Dexec.mainClass="org.terminal.ui.TerminalApp"
```

### Run Tests
```bash
mvn clean test
```

## Example Usage (Core API)

```java
// Create an 80x24 buffer with 1000 lines of scrollback history
TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);

// Style and write text
buffer.setForegroundColor(TerminalColor.BRIGHT_GREEN);
buffer.addStyle(Style.BOLD);
buffer.write("System initialized.\n");

// Insert text
buffer.insert("Awaiting command...");

// Resize the terminal (text reflows to fit new dimensions)
buffer.resizeScreen(40, 24);

// Retrieve current screen output
System.out.println(buffer.getScreenAsString());
```
