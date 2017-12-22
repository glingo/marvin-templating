package com.marvin.bundle.templating.extention.core.node;

import com.marvin.bundle.templating.Context;
import com.marvin.bundle.templating.Renderer;
import com.marvin.bundle.templating.expression.Expression;
import com.marvin.bundle.templating.node.support.BodyNode;
import com.marvin.bundle.templating.node.support.Node;
import java.util.Map;

public class IfNode implements Node {
    
    private final Expression expression;
    private final Map<Expression<Boolean>, BodyNode> bodies;
    private final BodyNode elseBody;
    
    public IfNode(Expression expression, Map<Expression<Boolean>, BodyNode> bodies) {
        this(expression, bodies, null);
    }
    
    public IfNode(Expression expression, Map<Expression<Boolean>, BodyNode> bodies, BodyNode elseBody) {
        this.expression = expression;
        this.bodies = bodies;
        this.elseBody = elseBody;
    }

    @Override
    public void render(Context context, Renderer renderer) {
        boolean satisfied = false;
        
        for (Map.Entry<Expression<Boolean>, BodyNode> entry : bodies.entrySet()) {
            Expression<Boolean> statement = entry.getKey();
            BodyNode body = entry.getValue();
            satisfied = statement.evaluate(context);

            if (satisfied) {
                body.render(context, renderer);
                break;
            }
        }

        if (!satisfied && elseBody != null) {
            elseBody.render(context,renderer);
        }
    }
}
