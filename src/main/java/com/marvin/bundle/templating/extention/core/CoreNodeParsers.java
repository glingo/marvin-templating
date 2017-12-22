package com.marvin.bundle.templating.extention.core;

import com.marvin.bundle.templating.expression.Expression;
import com.marvin.bundle.templating.extention.core.node.BlockNode;
import com.marvin.bundle.templating.extention.core.node.ExtendsNode;
import com.marvin.bundle.templating.extention.core.node.ForNode;
import com.marvin.bundle.templating.extention.core.node.IfNode;
import com.marvin.bundle.templating.extention.core.node.ImportNode;
import com.marvin.bundle.templating.extention.core.node.SetNode;
import com.marvin.bundle.templating.node.support.BodyNode;
import com.marvin.bundle.templating.node.NodeParser;
import com.marvin.bundle.templating.node.NodeParsers;
import com.marvin.bundle.templating.token.Token;
import com.marvin.bundle.templating.token.TokenType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public interface CoreNodeParsers {
    
    static NodeParser ifNodeParser() {
        return (stream, parser, engine, endCondition) -> {
            Predicate<Token> decideIfFork = (token) -> token.isA(TokenType.NAME, "elseif", "else", "endif");
            Predicate<Token> decideIfEnd = (token) -> token.isA(TokenType.NAME, "endif");
            
            Map<Expression<Boolean>, BodyNode> bodies = new LinkedHashMap<>();
            
            // skip the 'if' token
            stream.next();
            
            Expression<Boolean> expression = parser.parse(stream);
            
            stream.expect(TokenType.EXECUTE_CLOSE);
            
            NodeParser<BodyNode> bodyParser = NodeParsers.body();
            
            BodyNode body = bodyParser.parse(stream, parser, engine, decideIfFork);
            bodies.put(expression, body);
            BodyNode elseBody = null;
            boolean end = false;
            while (!end) {
                switch (stream.current().getValue()) {
                    case "else":
                        stream.next();
                        stream.expect(TokenType.EXECUTE_CLOSE);
                        elseBody = bodyParser.parse(stream, parser, engine, decideIfFork);
                        break;

                    case "elseif":
                        stream.next();
                        expression = parser.parse(stream);
                        stream.expect(TokenType.EXECUTE_CLOSE);
                        body = bodyParser.parse(stream, parser, engine, decideIfEnd);
                        bodies.put(expression, body);
                        break;

                    case "endif":
                        stream.next();
                        end = true;
                        break;
                    default:
                        String msg = String.format("Unexpected end of template. Pebble was looking for the following tags \"else\", \"elseif\", or \"endif\" at %s.", stream.current().getPosition());
                        throw new Exception(msg);
                }
            }
            
            stream.expect(TokenType.EXECUTE_CLOSE);
            return new IfNode(expression, bodies, elseBody);
        };
    }
    
    static NodeParser forNodeParser() {
        return (stream, parser, engine, endCondition) -> {
            Predicate<Token> decideElseFork = (token) -> token.isA(TokenType.NAME, "else", "endfor");
            Predicate<Token> decideEndFork = (token) -> token.isA(TokenType.NAME, "endfor");

            // skip the 'for' token
            stream.next();

            // get the iteration variable
            String iterationVariable = parser.varName(stream);

            stream.expect(TokenType.NAME, "in");

            // get the iterable variable
            Expression iterable = parser.parse(stream);

            stream.expect(TokenType.EXECUTE_CLOSE);

            NodeParser<BodyNode> bodyParser = NodeParsers.body();
            BodyNode body = bodyParser.parse(stream, parser, engine, decideElseFork);

            BodyNode elseBody = null;

            if (stream.current().isA(TokenType.NAME, "else")) {
                // skip the 'else' token
                stream.next();
                stream.expect(TokenType.EXECUTE_CLOSE);
                elseBody = bodyParser.parse(stream, parser, engine, decideEndFork);
            }

            // skip the 'endfor' token
            stream.next();

            stream.expect(TokenType.EXECUTE_CLOSE);
            return new ForNode(Expression.literalString(iterationVariable), iterable, body, elseBody);
        };
    }
    
    static NodeParser extendsNodeParser() {
        return (stream, parser, engine, endCondition) -> {
            // skip the 'extends' token
            stream.next();

            Expression parentTemplateExpression = parser.parse(stream);

            stream.expect(TokenType.EXECUTE_CLOSE);
            return new ExtendsNode(parentTemplateExpression);
        };
    }
    
    static NodeParser importNodeParser() {
        return (stream, parser, engine, endCondition) -> {
            // skip over the 'import' token
            stream.next();
            Expression importExpression = parser.parse(stream);

            stream.expect(TokenType.EXECUTE_CLOSE);

            return new ImportNode(importExpression);
        };
    }
    
    static NodeParser setNodeParser() {
        return (stream, parser, engine, endCondition) -> {
            // skip the 'extends' token
            stream.next();

            String name = parser.varName(stream);
            
            stream.expect(TokenType.PUNCTUATION, "=");

            Expression value = parser.parse(stream);

            stream.expect(TokenType.EXECUTE_CLOSE);
            return new SetNode(name, value);
        };
    }
    
    static NodeParser blockNodeParser() {
        return (stream, parser, engine, endCondition) -> {
            // skip over the 'block' token to the name token
            Token blockName = stream.next();

            // expect a name or string for the new block
            if (!blockName.isA(TokenType.NAME) && !blockName.isA(TokenType.STRING)) {

                // we already know an error has occurred but let's just call the
                // typical "expect" method so that we know a proper error
                // message is given to user
                stream.expect(TokenType.NAME);
            }

            // get the name of the new block
            String name = blockName.getValue();

            // skip over name
            stream.next();

            stream.expect(TokenType.EXECUTE_CLOSE);

            NodeParser<BodyNode> bodyParser = NodeParsers.body();
            // now we parse the block body
            BodyNode blockBody = bodyParser.parse(stream, parser, engine, (Token token1) -> token1.isA(TokenType.NAME, "endblock"));

            // skip the 'endblock' token
            stream.next();

            // check if user included block name in endblock
            Token current = stream.current();
            if (current.isA(TokenType.NAME, name) || current.isA(TokenType.STRING, name)) {
                stream.next();
            }

            stream.expect(TokenType.EXECUTE_CLOSE);
            return new BlockNode(name, blockBody);
        };
    }
}
