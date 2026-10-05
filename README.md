# Terminal Text Buffer

A Java implementation of a core text buffer for a terminal emulator. It handles the 2D character grid, scrollback history, cursor movement, text styling, and dynamic screen resizing (reflow).

## Features

* **Screen & Scrollback:** Splits the terminal state into an active, fixed-size grid (screen) and a rolling history log (scrollback).
* **Dynamic Reflow:** Unwraps and re-wraps text when the terminal is resized, preserving text layout and adjusting the cursor to stay on the correct character.
* **Editing Modes:** Supports both standard overwriting (`write`) and character shifting (`insert`).
* **Text Styling & Colors:** Supports 16 standard terminal colors (foreground/background) and text styles (`BOLD`, `ITALIC`, `UNDERLINE`).
* **Cursor Navigation:** Standard cursor movement (up, down, left, right) with proper boundary handling.

## Under the Hood

A few design choices made to keep the buffer lightweight and fast:

* **Parallel arrays instead of `Cell` objects:** To reduce Garbage Collector pressure, each line stores characters and styles in primitive parallel arrays (`char[]` and `CellAttributes[]`). 
* **Flyweight pattern for attributes:** Most characters share default or identical styles. `CellAttributes` instances are cached and reused, keeping memory allocations very low during heavy text streaming.
* **Ring buffers for scrolling:** Both the active screen and the scrollback history are backed by circular arrays with modulo indexing. This makes scrolling an $O(1)$ pointer increment instead of shifting elements across the array.

## Usage Example

```java
// Create an 80x24 buffer with 1000 lines of history
TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);

// Style and write text
buffer.setForegroundColor(TerminalColor.BRIGHT_GREEN);
buffer.addStyle(Style.BOLD);
buffer.write("System initialized.\n");

// Insert text
buffer.insert("Awaiting command...");

// Resize the terminal (text reflows to fit new width)
buffer.resizeScreen(40, 24);

// Get current screen content
System.out.println(buffer.getScreenAsString());
```

## Build & Run Tests

Requires **Java 23** and **Maven**.

```bash
mvn clean test
```
