package com.marvin.bundle.templating.node;

import com.marvin.bundle.templating.node.support.Node;
import com.marvin.bundle.templating.node.support.TextNode;
import com.marvin.bundle.templating.node.support.PrintNode;
import com.marvin.bundle.templating.node.support.RootNode;
import com.marvin.bundle.templating.node.support.BodyNode;
import com.marvin.bundle.templating.node.support.CommentNode;
import com.marvin.bundle.templating.token.Token;
import com.marvin.bundle.templating.token.TokenType;
import java.util.HashMap;
import java.util.Map;

public interface NodeParsers {
    
    static NodeParser root(Map<TokenType, NodeParser> parsers) {
        NodeParser<BodyNode> bodyParser = NodeParser.body(parsers);
        
        return (stream, parser, engine, endCondition) -> {
            BodyNode body = bodyParser.parse(stream, parser, engine, endCondition);
            return new RootNode(body);
        };
    }
    
    static NodeParser root() {
        NodeParser<BodyNode> bodyParser = body();
        
        return (stream, parser, engine, endCondition) -> {
            BodyNode body = bodyParser.parse(stream, parser, engine, endCondition);
            return new RootNode(body);
        };
    }
    
    static NodeParser body() {
        Map<TokenType, NodeParser> parsers = new HashMap<>();
        parsers.put(TokenType.TEXT, text());
        parsers.put(TokenType.COMMENT_OPEN, comment());
        parsers.put(TokenType.PRINT_OPEN, print());
        parsers.put(TokenType.EXECUTE_OPEN, execute());
        
        return NodeParser.body(parsers);
    }
    
    static NodeParser comment() {
        return (stream, parser, engine, endCondition) -> {
            stream.next();
            Token current = stream.current();
            if (null == current.getValue() || current.getValue().isEmpty()) {
                return null;
            }
            Node node = new CommentNode(current.getValue());
            stream.next();
            stream.expect(TokenType.COMMENT_CLOSE);
            return node;
        };
    }
    
    static NodeParser text() {
        return (stream, parser, engine, endCondition) -> {
            Token current = stream.current();
            if (null == current.getValue() || current.getValue().isEmpty()) {
                return null;
            }
            Node node = new TextNode(current.getValue());
            stream.next();
            return node;
        };
    }
    
    static NodeParser print() {
        return (stream, parser, engine, endCondition) -> {
            stream.next();
            Node node = new PrintNode(parser.parse(stream));
            stream.expect(TokenType.PRINT_CLOSE);
            return node;
        };
    }
    
    static NodeParser execute() {
        return (stream, parser, engine, endCondition) -> {
            Token token = stream.next();
            
            if (!token.isA(TokenType.NAME)) {
                String msg = String.format("A block must start with a tag name at %s.", token.getPosition());
                throw new Exception(msg);
            }
            
            if (null != endCondition && endCondition.test(token)) {
                return null;
            }
            
            // find an appropriate parser for this name
            NodeParser nodeParser = engine.getNodeParser(token.getValue());
            
            if (nodeParser == null) {
                String msg = String.format("Unexpected tag name \"%s\" at %s", token.getValue(), token.getPosition());
                throw new Exception(msg);
            }
            
            return nodeParser.parse(stream, parser, engine, endCondition);
        };
    }
}
