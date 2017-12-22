package com.marvin.bundle.templating.extention.core.node;

import com.marvin.bundle.templating.Context;
import com.marvin.bundle.templating.Hierarchy;
import com.marvin.bundle.templating.Renderer;
import com.marvin.bundle.templating.Template;
import com.marvin.bundle.templating.node.support.BodyNode;
import com.marvin.bundle.templating.node.support.Node;

public class BlockNode implements Node {
    
    private final BodyNode body;

    private final String name;
    
    public BlockNode(String name, BodyNode body) {
        this.body = body;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public BodyNode getBody() {
        return body;
    }
    
    @Override
    public void render(Context context, Renderer renderer) {
        Hierarchy<Template> hierarchy = context.getTemplateHierarchy();
        hierarchy.get().renderBlock(name, context, renderer, false);
    }
}
