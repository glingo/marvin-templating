package com.marvin.bundle.templating.node.support;

import com.marvin.bundle.templating.Context;
import com.marvin.bundle.templating.Renderer;
import com.marvin.bundle.templating.expression.Expression;

public class PrintNode implements Node {
    
    private final Expression expression;

    public PrintNode(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }
    
    @Override
    public void render(Context context, Renderer renderer) {
        Object value = getExpression().evaluate(context);
        renderer.renderValue(value);
    }
}
