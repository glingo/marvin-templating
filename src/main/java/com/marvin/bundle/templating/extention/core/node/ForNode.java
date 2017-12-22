package com.marvin.bundle.templating.extention.core.node;

import com.marvin.bundle.templating.Context;
import com.marvin.bundle.templating.Renderer;
import com.marvin.bundle.templating.expression.Expression;
import com.marvin.bundle.templating.node.support.BodyNode;
import com.marvin.bundle.templating.node.support.Node;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ForNode implements Node {
    
    private final Expression<Iterable> iterableExpression;
    private final Expression<String> varExpression;
    private final BodyNode body;
    private final BodyNode elseBody;
    
    public ForNode(Expression varExpression, Expression<Iterable> iterableExpression, BodyNode body, BodyNode elseBody) {
        this.varExpression = varExpression;
        this.iterableExpression = iterableExpression;
        this.body = body;
        this.elseBody = elseBody;
    }

    @Override
    public void render(Context context, Renderer renderer) {
        Object iterableEvaluation = iterableExpression.evaluate(context);
        String varName = this.varExpression.evaluate(context);
        Iterable<?> iterable;

        if (iterableEvaluation == null) {
            return;
        }

        iterable = toIterable(iterableEvaluation);

        if (iterable == null) {
            String msg = String.format("Not an iterable object. Value = [%s].", iterableEvaluation);
            System.err.println(msg);
            return;
        }

        Iterator<?> iterator = iterable.iterator();

        if (iterator.hasNext()) {

            int length = getIteratorSize(iterableEvaluation);
            int index = 0;

            Map<String, Object> loop = new HashMap<>();
            while (iterator.hasNext()) {
                if (index == 0) {
                    loop.put("first", index == 0);
                    loop.put("last", index == length - 1);
                    loop.put("length", length);
                }else{
                    // second iteration
                    if(index == 1){
                        loop.put("first", false);
                    }
                    // last iteration
                    if(index == length - 1){
                        loop.put("last", true);
                    }
                }

                loop.put("revindex", length - index - 1);
                loop.put("index", index++);
                
                context.set("loop", loop);
                context.set(varName, iterator.next());

                body.render(context, renderer);
            }
            context.remove("loop");
            context.remove(varName);
        } else if (elseBody != null) {
            elseBody.render(context, renderer);
        }
    }
    
    private Iterable<Object> toIterable(final Object obj) {

        Iterable<Object> result = null;

        if (obj instanceof Iterable<?>) {

            result = (Iterable<Object>) obj;

        } else if (obj instanceof Map) {

            // raw type
            result = ((Map) obj).entrySet();

        } else if (obj.getClass().isArray()) {

            if (Array.getLength(obj) == 0) {
                return new ArrayList<>(0);
            }

            result = () -> new Iterator<Object>() {
                
                private int index = 0;
                
                private final int length = Array.getLength(obj);
                
                @Override
                public boolean hasNext() {
                    return index < length;
                }
                
                @Override
                public Object next() {
                    return Array.get(obj, index++);
                }
                
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        return result;
    }
    
    private int getIteratorSize(Object iterable) {
        if (iterable == null) {
            return 0;
        }
        if (iterable instanceof Collection) {
            return ((Collection<?>) iterable).size();
        } else if (iterable instanceof Map) {
            return ((Map<?, ?>) iterable).size();
        } else if (iterable.getClass().isArray()) {
            return Array.getLength(iterable);
        }

        // assumed to be of type Iterator
        Iterator<?> it = ((Iterable<?>) iterable).iterator();
        int size = 0;
        while (it.hasNext()) {
            size++;
            it.next();
        }
        return size;
    }
}
