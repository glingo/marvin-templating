package com.marvin.bundle.templating.expression;

import com.marvin.bundle.templating.operator.Associativity;
import com.marvin.bundle.templating.operator.BinaryOperator;
import com.marvin.bundle.templating.operator.UnaryOperator;
import com.marvin.bundle.templating.token.Token;
import com.marvin.bundle.templating.token.TokenStream;
import com.marvin.bundle.templating.token.TokenType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExpressionParser {
    
    private static final Set<String> RESERVED_KEYWORDS = new HashSet<>(Arrays.asList("true", "false", "null", "none"));
    
    private final Map<String, UnaryOperator> unaryOperators;
    private final Map<String, BinaryOperator> binaryOperators;

    public ExpressionParser(Map<String, UnaryOperator> unaryOperators, Map<String, BinaryOperator> binaryOperators) {
        this.unaryOperators = unaryOperators;
        this.binaryOperators = binaryOperators;
    }
    
    public Expression parse(TokenStream stream) throws Exception {
        return parse(stream, 0);
    }

    public Expression parse(TokenStream stream, int minPrecedence) throws Exception {
        Expression expression;
        
        Token token = stream.current();
        
        // check for unary tokens
        if (isUnary(token)) {
            UnaryOperator operator = this.unaryOperators.get(token.getValue());
            stream.next();
            expression = parse(stream, operator.getPrecedence());
            expression = Expression.unary(expression, operator.getFunction());
        } else if (token.isA(TokenType.PUNCTUATION, "(")) {
            stream.next();
            expression = parse(stream);
            stream.expect(TokenType.PUNCTUATION, ")");
            return parsePostfixExpression(stream, expression);
        } else if (token.isA(TokenType.PUNCTUATION, "[")) {
            expression = array(stream);
        } else {
            expression = subParse(stream);
        }
        
        token = stream.current();
        while (isBinary(token)) {
            // find out which operator we are dealing with 
            BinaryOperator operator = this.binaryOperators.get(token.getValue());
            if (operator == null) {
                throw new Exception(String.format("Unknown Operator %s at %s", token.getValue(), token.getPosition()));
            }
            
            if (operator.getPrecedence() < minPrecedence) {
                break;
            }
            
            // skip over it
            stream.next();
            Expression expressionRight;
            Expression expressionLeft = expression;
            
            if (stream.current().isA(TokenType.NAME)) {
                expressionRight = parseFilterOrTestExpression(stream, expression);
            } else {
                int precedence = Associativity.LEFT.equals(operator.getAssociativity()) ? operator.getPrecedence() + 1 : operator.getPrecedence();
                expressionRight = parse(stream, precedence);
            }

            Expression finalExpression = Expression.binary(expressionLeft, expressionRight, operator.getFunction());
            expression = finalExpression;
            token = stream.current();
        }
        
        return expression;
    }
    
    private Expression subParse(TokenStream stream) throws Exception {
        Expression expression = null;
        Token token = stream.current();
        switch(token.getType()) {
            case NAME:
                switch(token.getValue()) {
                    case "true":
                    case "TRUE":
                        expression = Expression.literalBoolean(Boolean.TRUE);
                        break;
                    case "false":
                    case "FALSE":
                        expression = Expression.literalBoolean(Boolean.FALSE);
                        break;
                    case "none":
                    case "NONE":
                    case "null":
                    case "NULL":
                        expression = Expression.literalNull();
                        break;
                    default:
                        if (stream.peek().isA(TokenType.PUNCTUATION, "(")) {
                            // function call
                            expression = Expression.functionName(token.getValue());
                            break;
                        }
                        expression = Expression.var(token.getValue());
                        break;
                }
                break;
            case NUMBER:
                final String numberValue = token.getValue();
                if (numberValue.contains(".")) {
                    expression = Expression.literalDouble(numberValue);
                    break;
                }
                expression = Expression.literalLong(numberValue);
                break;
            case STRING:
                expression = Expression.literalString(token.getValue());
                break;
            default:
                String msg = String.format("Unexpected token \"%s\" of value \"%s\" at %s.", token.getType(), token.getValue(), token.getPosition());
                throw new Exception(msg);
        }
        
        stream.next();
        return parsePostfixExpression(stream, expression);
    }
    /**
     * Checks if a token is a unary operator.
     *
     * @param token The token that we are checking
     * @return boolean Whether the token is a unary operator or not
     */
    private boolean isUnary(Token token) {
        return token.isA(TokenType.OPERATOR) && this.unaryOperators.containsKey(token.getValue());
    }
    
    /**
     * Checks if a token is a binary operator.
     *
     * @param token The token that we are checking
     * @return boolean Whether the token is a unary operator or not
     */
    private boolean isBinary(Token token) {
        return token.isA(TokenType.OPERATOR) && this.binaryOperators.containsKey(token.getValue());
    }
    
    /**
     * Determines if there is more to the provided expression than we originally
     * thought. We will look for the filter operator or perhaps we are getting
     * an attribute from a variable (ex. var.attribute or var['attribute'] or
     * var.attribute(bar)).
     *
     * @param node The expression that we have already discovered
     * @return Either the original expression that was passed in or a slightly
     * modified version of it, depending on what was discovered.
     * @throws Exception Thrown if a parsing error occurs.
     */
    private Expression parsePostfixExpression(TokenStream stream, Expression expression) throws Exception {
        Token current;
        while (true) {
            current = stream.current();

            if (current.isA(TokenType.PUNCTUATION, ".") || current.isA(TokenType.PUNCTUATION, "[")) {

                // a period represents getting an attribute from a variable or
                // calling a method
                expression = parseBeanAttributeExpression(stream, expression);

            } else if (current.isA(TokenType.PUNCTUATION, "(")) {
                // directive call
                expression = parseDirectiveOrMacroInvocation(stream, expression);
            } else {
                break;
            }
        }
        return expression;
    }
    
    public Expression parseFilterOrTestExpression(TokenStream stream, Expression valueExpression) throws Exception {
        Token token = stream.expect(TokenType.NAME);

        Expression argExpression = Expression.emptyArguments();
        if (stream.current().isA(TokenType.PUNCTUATION, "(")) {
            argExpression = parseArguments(stream);
        }

        return Expression.binaryOperator(token.getValue(), valueExpression, argExpression);
    }
    
    public Expression parseFilterInvocationExpression(TokenStream stream, Expression valueExpression) throws Exception {
        Token token = stream.expect(TokenType.NAME);

        Expression argExpression = Expression.emptyArguments();
        if (stream.current().isA(TokenType.PUNCTUATION, "(")) {
            argExpression = parseArguments(stream);
        }

        return Expression.filter(token.getValue(), valueExpression, argExpression);
    }
    
    /**
     * A bean attribute expression can either be an expression getting an
     * attribute from a variable in the context, or calling a method from a
     * variable.
     *
     * Ex. foo.bar or foo['bar'] or foo.bar('baz')
     *
     * @param node The expression parsed so far
     * @return NodeExpression The parsed subscript expression
     * @throws Exception Thrown if a parsing error occurs.
     */
    private Expression parseBeanAttributeExpression(TokenStream stream, Expression expression) throws Exception {
        if (stream.current().isA(TokenType.PUNCTUATION, ".")) {

            // skip over the '.' token
            stream.next();

            Token token = stream.expect(TokenType.NAME);

            Expression<Map<String, Object>> args = null;
            if (stream.current().isA(TokenType.PUNCTUATION, "(")) {
                args = parseArguments(stream);
            }
            expression = Expression.attribute(expression, Expression.literalString(token.getValue()), args);
        } else if (stream.current().isA(TokenType.PUNCTUATION, "[")) {
            // skip over opening '[' bracket
            stream.next();

            expression = Expression.attribute(expression, parse(stream), null);
            // move past the closing ']' bracket
            stream.expect(TokenType.PUNCTUATION, "]");
        }

        return expression;
    }
    
    private Expression parseDirectiveOrMacroInvocation(TokenStream stream, Expression<String> expression) throws Exception {
        String functionName = expression.evaluate();
        Expression<Map<String, Object>> args = parseArguments(stream);
        return Expression.directive(functionName, args);
    }
    
     public Expression<Map<String, Object>> parseArguments(TokenStream stream) throws Exception {
        return parseArguments(stream, false);
    }

    public Expression<Map<String, Object>> parseArguments(TokenStream stream, boolean isMacroDefinition) throws Exception {
        List<Expression> positionalArgs = new ArrayList<>();
        Map<String, Expression> namedArgs = new HashMap<>();

        stream.expect(TokenType.PUNCTUATION, "(");

        while (!stream.current().isA(TokenType.PUNCTUATION, ")")) {
            String argumentName = null;
            Expression argumentValue = null;

            if (!namedArgs.isEmpty() || !positionalArgs.isEmpty()) {
                stream.expect(TokenType.PUNCTUATION, ",");
            }

            /*
             * Most arguments consist of VALUES with optional NAMES but in the
             * case of a macro definition the user is specifying NAMES with
             * optional VALUES. Therefore the logic changes slightly.
             */
            if (isMacroDefinition) {
                argumentName = varName(stream);
                if (stream.current().isA(TokenType.PUNCTUATION, "=")) {
                    stream.expect(TokenType.PUNCTUATION, "=");
                    argumentValue = parse(stream);
                }
            } else {
                if (stream.peek().isA(TokenType.PUNCTUATION, "=")) {
                    argumentName = varName(stream);
                    stream.expect(TokenType.PUNCTUATION, "=");
                }
                argumentValue = parse(stream);
            }

            if (argumentName == null) {
                if (!namedArgs.isEmpty()) {
                    String msg = String.format(
                            "Positional arguments must be declared before any named arguments at %s.", 
                            stream.current().getPosition());
                    throw new Exception(msg);
                }
                positionalArgs.add(argumentValue);
            } else {
                namedArgs.put(argumentName, argumentValue);
            }
        }

        stream.expect(TokenType.PUNCTUATION, ")");
        return Expression.arguments(positionalArgs, namedArgs);
    }
    
    public String varName(TokenStream stream) throws Exception {

        Token token = stream.current();
        token.isA(TokenType.NAME);

        if (RESERVED_KEYWORDS.contains(token.getValue())) {
            String msg = String.format("Can not assign a value to %s at %s.", token.getValue(), token.getPosition());
            throw new Exception(msg);
        }

        stream.next();
        return token.getValue();
    }
    
    private Expression<List> array(TokenStream stream) throws Exception {
        stream.expect(TokenType.PUNCTUATION, "[");
        if (stream.current().isA(TokenType.PUNCTUATION, "]")) {
            stream.next();
            return Expression.emptyList();
        }
        
        List<Expression> expressions = new ArrayList();
        while (true) {
            expressions.add(subParse(stream));
            if (stream.current().isA(TokenType.PUNCTUATION, "]")) {
                break;
            }
            stream.expect(TokenType.PUNCTUATION, ",");
        }
        
        stream.expect(TokenType.PUNCTUATION, "]");
        return Expression.array(expressions);
    }
}
