package com.marvin.bundle.templating.node.support;

import com.marvin.bundle.templating.Context;
import com.marvin.bundle.templating.Renderer;

public class TextNode implements Node {

    private final String value;

    public TextNode(String value) {
        this.value = value;
    }
    
    @Override
    public void render(Context context, Renderer renderer) {
        renderer.renderValue(value);
    }

    public String getValue() {
        return value;
    }
}
