package com.marvin.bundle.templating;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Source implements CharSequence {
    
    private final String name;

    /**
     * The characters found within the template.
     */
    private final char[] source;

    /**
     * Number of characters stored in source array remaining to be tokenized
     */
    private int size = 0;

    /**
     * Default capacity
     */
    private static final int DEFAULT_CAPACITY = 1024;

    /**
     * An index of the first character for the remaining un-tokenized source.
     */
    private int offset = 0;

    /**
     * Tracking the line number that we are currently tokenizing.
     */
    private int lineNumber = 1;
    private int columnNumber = 1;

    public Source(String name, char[] source, int size) {
        this.name = name;
        this.source = source;
        this.size = size;
    }
    
    public Source(String name, char[] source, int size, int lineNumber, int columnNumber) {
        this.name = name;
        this.source = source;
        this.size = size;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }
    
    public Source save() {
        return new Source(name, Arrays.copyOfRange(source, offset, offset + size), size, lineNumber, columnNumber);
    }

    /**
     * Moves the start index a certain amount. While traversing this amount we
     * will count how many newlines have been encountered.
     *
     * @param amount Amount of characters to advance by
     */
    public void advance(int amount) {
        int index = 0;
        while (index < amount) {
            int sizeOfNewline = advanceThroughNewline(index);

            if (sizeOfNewline > 0) {
                index += sizeOfNewline;
            } else {
                index++;
            }
            this.columnNumber++;
        }

        this.size -= amount;
        this.offset += amount;
    }

    public void advanceThroughWhitespace() {
        int index = 0;

        while (Character.isWhitespace(charAt(index))) {
            int sizeOfNewline = advanceThroughNewline(index);

            if (sizeOfNewline > 0) {
                index += sizeOfNewline;
            } else {
                index++;
            }
            this.columnNumber++;
        }

        this.size -= index;
        this.offset += index;
    }

    /**
     * Advances through possible newline character and returns how many
     * characters were used to represent the newline (windows uses two
     * characters to represent one newline).
     *
     * @param index The index of the potential newline character
     * @return
     */
    private int advanceThroughNewline(int index) {
        char character = this.charAt(index);
        int numOfCharacters = 0;

        // windows newline
        if ('\r' == character && '\n' == charAt(index + 1)) {

            this.lineNumber++;
            this.columnNumber = 0;
            numOfCharacters = 2;

            // various other newline characters
        } else if ('\n' == character || '\r' == character 
                || '\u0085' == character || '\u2028' == character
                || '\u2029' == character) {

            this.lineNumber++;
            this.columnNumber = 0;
            numOfCharacters = 1;
        }
        
        return numOfCharacters;
    }

    public String substring(int start, int end) {
        return new String(Arrays.copyOfRange(source, this.offset + start, this.offset + end));
    }

    public String substring(int end) {
        return new String(Arrays.copyOfRange(source, offset, offset + end));
    }

    @Override
    public int length() {
        return size;
    }

    @Override
    public char charAt(int index) {
        return source[offset + index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new String(Arrays.copyOfRange(source, this.offset + start, this.offset + end));
    }

    @Override
    public String toString() {
        return new String(Arrays.copyOfRange(source, offset, offset + size));
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    public Position getPosition() {
        return new Position(getName(), lineNumber, columnNumber);
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getName() {
        return name;
    }

    public static SourceBuilder builder(String name) {
        return new SourceBuilder(name);
    }

    public static class SourceBuilder {

        private final String name;
        private char[] source = new char[DEFAULT_CAPACITY];
        private int size = 0;

        public SourceBuilder(String name) {
            this.name = name;
        }
        
        public SourceBuilder read(String string) {
            return read(new StringReader(string));
        }

        public SourceBuilder read(Reader reader) {
            try {
                char[] buffer = new char[1024 * 4];
                int amountJustRead = 0;
                while ((amountJustRead = reader.read(buffer)) != -1) {
                    ensureCapacity(size + amountJustRead);
                    append(buffer, amountJustRead);
                }
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(Source.class.getName()).log(Level.SEVERE, null, ex);
            }
            return this;
        }

        public SourceBuilder append(char[] characters, int amount) {
            for (int i = 0; i < amount; ++i) {
                this.source[size + i] = characters[i];
            }
            size += amount;
            return this;
        }

        public Source build() {
            return new Source(this.name, this.source, this.size);
        }

        private void ensureCapacity(int minCapacity) {
            if (source.length - minCapacity < 0) {
                grow(minCapacity);
            }
        }

        private void grow(int minCapacity) {
            int oldCapacity = source.length;
            int newCapacity = Math.max(oldCapacity << 1, minCapacity);

            this.source = Arrays.copyOf(source, newCapacity);
        }
    }
}
