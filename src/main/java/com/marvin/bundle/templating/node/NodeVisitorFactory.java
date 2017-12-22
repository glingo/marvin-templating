package com.marvin.bundle.templating.node;

import com.marvin.bundle.templating.Template;

public interface NodeVisitorFactory {
    
    public NodeVisitor create(Template template);
}
