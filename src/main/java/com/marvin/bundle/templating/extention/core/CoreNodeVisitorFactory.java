package com.marvin.bundle.templating.extention.core;

import com.marvin.bundle.templating.Template;
import com.marvin.bundle.templating.extention.core.node.BlockNode;
import com.marvin.bundle.templating.node.NodeVisitor;
import com.marvin.bundle.templating.node.NodeVisitorFactory;

public class CoreNodeVisitorFactory implements NodeVisitorFactory {
    
    @Override
    public NodeVisitor create(Template template) {
        return (node) -> {
            if (node instanceof BlockNode) {
                BlockNode block = (BlockNode) node;
                template.registerBlock(block.getName(), block.getBody());
            }
        };
    }
}
