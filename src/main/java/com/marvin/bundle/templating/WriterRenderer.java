package com.marvin.bundle.templating;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

public class WriterRenderer implements Renderer {
    
    private final Writer writer;

    public WriterRenderer(Writer writer) {
        this.writer = writer;
    }
    
    @Override
    public void finalyze() {
        try {
            this.writer.flush();
        } catch(IOException exception) {
            // nothing to do ?
            exception.printStackTrace();
        }
    }

    @Override
    public void renderValue(Object value) {
        try {
            this.writer.write(Objects.toString(value, "null"));
            this.writer.write(System.lineSeparator());
        } catch(IOException exception) {
            // nothing to do ?
            exception.printStackTrace();
        }
    }
}
