package com.marvin.bundle.templating;

import com.marvin.bundle.templating.node.support.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Template {
    
    private String name;
    
    private Node root;
    
    private List<Template> imports;
    
    private final Map<String, Node> blocks = new HashMap<>();
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Template> getImports() {
        return imports;
    }

    public void setImports(List<Template> imports) {
        this.imports = imports;
    }

    public void registerBlock(String name, Node block) {
        blocks.put(name, block);
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }
    
    public Map<String, Node> getBlocks() {
        return blocks;
    }
    
    public Node getBlock(String name) {
        return blocks.get(name);
    }
    
    public boolean hasBlock(String name) {
        return this.blocks.containsKey(name);
    }
    
    public void renderBlock(String name, Context context, Renderer renderer, boolean ignoreOverriden) {
        Hierarchy<Template> hierarchy = context.getTemplateHierarchy();
        Template childTemplate = hierarchy.getChild();
        
        if (!ignoreOverriden && childTemplate != null) {
            hierarchy.descend();
            childTemplate.renderBlock(name, context, renderer, false);
            hierarchy.ascend();
        } else if (hasBlock(name)) {
            Node block = getBlock(name);
            block.render(context, renderer);
            // delegate to parent
        } else {
            Template parent = hierarchy.getParent();
            if (parent != null) {
                hierarchy.ascend();
                parent.renderBlock(name, context, renderer, true);
                hierarchy.descend();
            }
        }
    }

    public static TemplateBuilder builder() {
        return new TemplateBuilder();
    }
    
    public static class TemplateBuilder {
    
        private String name;
        
        private final List<Template> imports = new ArrayList<>();
        
        private Node root;
        
        public TemplateBuilder root(Node root) {
            this.root = root;
            return this;
        }
        
        public TemplateBuilder named(String name) {
            this.name = name;
            return this;
        }
        
        public TemplateBuilder importTemplate(Template template) {
            this.imports.add(template);
            return this;
        }
        
        public Template build() {
            Template template = new Template();
            
            template.setName(this.name);
            template.setRoot(this.root);
            template.setImports(this.imports);
            return template;
        }
    }
}
