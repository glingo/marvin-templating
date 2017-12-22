package com.marvin.bundle.templating;

import java.util.ArrayList;

public class Hierarchy<T> {
    
    private final ArrayList<T> hierarchy = new ArrayList<>();

    private int current = 0;

    public Hierarchy(T current) {
        this.hierarchy.add(current);
    }

    public void pushAncestor(T ancestor) {
        this.hierarchy.add(ancestor);
    }

    public void ascend() {
        this.current++;
    }

    public void descend() {
        this.current--;
    }

    public T get() {
        return this.hierarchy.get(this.current);
    }
    
    public T getChild() {
        if (this.current == 0) {
            return null;
        }
        return this.hierarchy.get(this.current - 1);
    }

    public T getParent() {
        if (this.current == this.hierarchy.size() - 1) {
            return null;
        }
        return this.hierarchy.get(this.current + 1);
    }
    
    public void reset() {
        this.current = 0;
    }
}
