package com.marvin.bundle.templating.node.support;

import com.marvin.bundle.templating.Context;
import com.marvin.bundle.templating.Renderer;
import com.marvin.bundle.templating.node.NodeVisitor;

public interface Node {
    default void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
//    default void evaluate(Context context) {
//        
//    }

    default void render(Context context, Renderer render) {
        
    }
}
