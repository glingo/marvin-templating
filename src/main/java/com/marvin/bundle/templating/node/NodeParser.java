package com.marvin.bundle.templating.node;

import com.marvin.bundle.templating.node.support.Node;
import com.marvin.bundle.templating.node.support.BodyNode;
import com.marvin.bundle.templating.Context;
import com.marvin.bundle.templating.Engine;
import com.marvin.bundle.templating.expression.ExpressionParser;
import com.marvin.bundle.templating.token.Token;
import com.marvin.bundle.templating.token.TokenStream;
import com.marvin.bundle.templating.token.TokenType;
import java.util.function.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface NodeParser<N extends Node> {
    
    N parse(TokenStream stream, ExpressionParser parser, Engine engine, Predicate<Token> endCondition) throws Exception;
    
    default N parse(TokenStream stream, ExpressionParser parser, Engine engine) throws Exception {
        return parse(stream, parser, engine, Token::isEOF);
    }
    
    static NodeParser delegate(Map<TokenType, NodeParser> parsers) {
        return (stream, parser, engine, endCondition) -> {
            Token token = stream.current();
            
            if (endCondition.test(token)) {
                return null;
            }
            
            NodeParser nodeParser = parsers.get(token.getType());
            if (null != nodeParser) {
                return nodeParser.parse(stream, parser, engine, endCondition);
            }
            
            throw new Exception("Unknown Token type " + token.getType());
        };
    }
    
    static NodeParser body(Map<TokenType, NodeParser> parsers) {
        NodeParser subParser = delegate(parsers);
  
        return (stream, parser, engine, endCondition) -> {
            List<Node> children = new ArrayList<>();
            
            while (endCondition != null && !endCondition.test(stream.current())) {                
                Node node = subParser.parse(stream, parser, engine, endCondition);
                if (null == node) {
                    break;
                }
                
                children.add(node);
            }
            
            return new BodyNode(children);
        };
    }
    
}
