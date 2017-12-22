package com.marvin.bundle.templating;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Locale;

public class Context {
    
    private final Map<String, Object> model;
    
    private final Hierarchy<Template> templateHierarchy;
    
    private final Locale locale;
    
    private final Engine engine;
    
    private final String name;
        
    private boolean ignoreOverriden = false;
    
    private List<Class> nodesToRenderInChild = new ArrayList<>();
    
    public Context(Template template, Engine engine, Locale locale, Map<String, Object> model, List<Class> nodesToRenderInChild) {
        this.name = template.getName();
        this.templateHierarchy = new Hierarchy<>(template);
        this.engine = engine;
        this.locale = locale;
        this.model = model;
        this.nodesToRenderInChild = nodesToRenderInChild;
        //.QSDQSDQDSQDS/
    }
    
    public Context subContext(Template sub) {
        return new Context(sub, engine, locale, model, nodesToRenderInChild);
    }
    
    public <T> T evaluate(String name) {
        if (Objects.isNull(this.model)) {
            return null;
        }
        return (T) this.model.get(name);
    }
    
    public <T> T evaluate(String expression, Class<T> c) {
        return evaluate(expression);
    }

    public String getName() {
        return name;
    }
    
    public Map<String, Object> getModel() {
        return model;
    }
    
    public void set(String name, Object value) {
        this.model.put(name, value);
    }
    
    public void remove(String name) {
        this.model.remove(name);
    }
    
    public Engine getEngine() {
        return engine;
    }

    public Locale getLocale() {
        return locale;
    }

    public Hierarchy<Template> getTemplateHierarchy() {
        return templateHierarchy;
    }
    
    public boolean isSafeToRenderInChild(Class nodeClass) {
        return nodesToRenderInChild.contains(nodeClass);
    }

    public boolean isIgnoreOverriden() {
        return ignoreOverriden;
    }

    public void setIgnoreOverriden(boolean ignoreOverriden) {
        this.ignoreOverriden = ignoreOverriden;
    }
}
