package com.marvin.bundle.templating;

public interface Renderer {
    
    void renderValue(Object value);
    
    void finalyze();
}
