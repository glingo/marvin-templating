package com.marvin.bundle.templating.node.support;

import com.marvin.bundle.templating.Context;
import com.marvin.bundle.templating.Renderer;
import com.marvin.bundle.templating.node.NodeVisitor;

public class RootNode implements Node {

    private final BodyNode body;

    public RootNode(BodyNode body) {
        this.body = body;
    }

    public BodyNode getBody() {
        return body;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        getBody().accept(visitor);
    }

    @Override
    public void render(Context context, Renderer renderer) {
        getBody().setOnlyRenderInheritanceSafeNodes(true);
        getBody().render(context, renderer);
    }
}
