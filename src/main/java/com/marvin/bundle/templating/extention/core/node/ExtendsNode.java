package com.marvin.bundle.templating.extention.core.node;

import com.marvin.bundle.templating.Context;
import com.marvin.bundle.templating.Engine;
import com.marvin.bundle.templating.Renderer;
import com.marvin.bundle.templating.Template;
import com.marvin.bundle.templating.expression.Expression;
import com.marvin.bundle.templating.node.support.Node;

public class ExtendsNode implements Node {
    
    private Expression<String> parentExpression;
    
    public ExtendsNode(Expression<String> parentExpression) {
        this.parentExpression = parentExpression;
    }
    
    @Override
    public void render(Context context, Renderer renderer) {
        try {
            Engine engine = context.getEngine();
            String path = this.parentExpression.evaluate(context);
            Template extend = engine.load(path);
            context.getTemplateHierarchy().pushAncestor(extend);
        } catch(Exception exception) {
            exception.printStackTrace();
            throw new RuntimeException();
        }
    }
}
