package com.marvin.bundle.templating.node.support;

import com.marvin.bundle.templating.Context;
import com.marvin.bundle.templating.Renderer;
import com.marvin.bundle.templating.node.NodeVisitor;
import java.util.List;

public class BodyNode implements Node {
    
    private final List<Node> children;
    
    /**
     * When a template extends a parent template there are very few nodes in the
     * child that should actually get rendered such as set and import. All
     * others should be ignored.
     */
    private boolean onlyRenderInheritanceSafeNodes = false;
    
    
    public BodyNode(List<Node> children) {
        this.children = children;
    }

    public List<Node> getChildren() {
        return children;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        getChildren().stream().forEach((child) -> {
            child.accept(visitor);
        });
    }
    
    @Override
    public void render(Context context, Renderer renderer) {
        getChildren().stream().forEach((child) -> {
            if (isOnlyRenderInheritanceSafeNodes() && context.getTemplateHierarchy().getParent() != null) {
                if (!context.isSafeToRenderInChild(child.getClass())) {
                    return;
                }
            }
            child.render(context, renderer);
        });
    }

    public boolean isOnlyRenderInheritanceSafeNodes() {
        return onlyRenderInheritanceSafeNodes;
    }

    public void setOnlyRenderInheritanceSafeNodes(boolean onlyRenderInheritanceSafeNodes) {
        this.onlyRenderInheritanceSafeNodes = onlyRenderInheritanceSafeNodes;
    }
}
