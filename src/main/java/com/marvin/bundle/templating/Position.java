package com.marvin.bundle.templating;

public class Position {
    
    private final String name;
    private final int line;
    private final int column;

    public Position(String name, int line, int column) {
        this.name = name;
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return String.format("%s (Line: %d, Column: %d)", name, line, column);
    }
}
