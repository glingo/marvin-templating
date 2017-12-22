package com.marvin.bundle.templating.extention.core.node;

import com.marvin.bundle.templating.Context;
import com.marvin.bundle.templating.Renderer;
import com.marvin.bundle.templating.expression.Expression;
import com.marvin.bundle.templating.node.support.Node;

public class SetNode implements Node {
    
    private final String name;

    private final Expression value;
    
    public SetNode(String name, Expression value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public void render(Context context, Renderer renderer) {
        context.set(name, value.evaluate(context));
    }
}
