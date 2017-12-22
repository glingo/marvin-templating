package com.marvin.bundle.templating.extention.core.node;

import com.marvin.bundle.templating.Context;
import com.marvin.bundle.templating.Engine;
import com.marvin.bundle.templating.Renderer;
import com.marvin.bundle.templating.expression.Expression;
import com.marvin.bundle.templating.node.support.Node;

public class ImportNode implements Node {
    
    private Expression<String> importExpression;
    
    public ImportNode(Expression<String> importExpression) {
        this.importExpression = importExpression;
    }

    @Override
    public void render(Context context, Renderer renderer) {
        try {
            Engine engine = context.getEngine();
            String path = this.importExpression.evaluate(context);
            context.getTemplateHierarchy().get().getImports().add(engine.load(path));
        } catch(Exception exception) {
            exception.printStackTrace();
            throw new RuntimeException();
        }
    }
}
