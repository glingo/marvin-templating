package com.marvin.bundle.templating.extention;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.marvin.bundle.templating.Renderer;
import com.marvin.bundle.templating.node.NodeParser;
import com.marvin.bundle.templating.node.NodeVisitorFactory;
import com.marvin.bundle.templating.operator.BinaryOperator;
import com.marvin.bundle.templating.operator.UnaryOperator;
import com.marvin.bundle.templating.token.TokenParser;
import java.util.Collections;

public interface Extension {
    
    default Map<String, Test> getTests() {
        return Collections.EMPTY_MAP;
    }
    
    default Map<String, Directive> getDirectives() {
        return Collections.EMPTY_MAP;
    }
    
    default Map<String, Filter> getFilters() {
        return Collections.EMPTY_MAP;
    }
    
    default List<TokenParser> getTokenParsers() {
        return new ArrayList<>();
    }
    
    default Map<String, NodeParser> getNodeParsers() {
        return Collections.EMPTY_MAP;
    }
    
    default Map<String, Renderer> getRenderers() {
        return Collections.EMPTY_MAP;
    }
    
    default Map<String, UnaryOperator> getUnaryOperators() {
        return Collections.EMPTY_MAP;
    }
    
    default Map<String, BinaryOperator> getBinaryOperators() {
        return Collections.EMPTY_MAP;
    }
    
    default List<NodeVisitorFactory> getNodeVisitorFactories() {
        return Collections.EMPTY_LIST;
    }
    
    default List<Class> getSafeNodes() {
        return Collections.EMPTY_LIST;
    }
}
