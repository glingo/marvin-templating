package com.marvin.bundle.templating.node;

import com.marvin.bundle.templating.node.support.Node;

public interface NodeVisitor {
    
    void visit(Node node);
}
