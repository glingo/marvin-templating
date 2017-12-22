package com.marvin.bundle.templating.token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.marvin.bundle.templating.Source;

@FunctionalInterface
public interface TokenParser {
    
    List<Token> parse(Source source) throws Exception;
    
    default List<Token> parse(Source source, boolean skipWhiteSpaces) throws Exception{
        if (skipWhiteSpaces) {
            source.advanceThroughWhitespace();
        }
        return parse(source);
    }
    
    @SuppressWarnings("UnusedAssignment")
    default Optional<List<Token>> tryParse(Source source, boolean skipWhiteSpaces) {
        Source clone = source.save();
        try {
            return Optional.of(parse(source, skipWhiteSpaces));
        } catch (Exception exception) {
            // do nothing with exception
            source = clone;
        }
        return Optional.empty();
    };
    
    default TokenParser skip(TokenParser skip, boolean skipWhiteSpaces) {
        return (source) -> {
            List<Token> result = parse(source, skipWhiteSpaces);
            skip.parse(source, skipWhiteSpaces);
            return result;
        };
    }
    
    default TokenParser then(TokenParser then) {
        return (source) -> {
            List<Token> result = parse(source, true);
            result.addAll(then.parse(source, true));
            return result;
        };
    }
    
    default TokenParser or(TokenParser then) {
        return (source) -> {
            Optional<List<Token>> tryParse = tryParse(source, true);
            if (tryParse.isPresent()) {
                return tryParse.get();
            }
            return then.parse(source);
        };
    }
    
    default TokenParser until(TokenParser end) {
        return source -> {
            List<Token> result = new ArrayList<>();
            Optional<List<Token>> tokens = end.tryParse(source, true);
            while(!tokens.isPresent()) {
                result.addAll(parse(source, true));
                tokens = end.tryParse(source, true);
            }
            result.addAll(tokens.get());
            return result;
        };
    }
    
    default TokenParser optional() {
        return source -> {
            Optional<List<Token>> result = tryParse(source, true);
            if (result.isPresent()) {
                return result.get();
            }
            return Collections.emptyList();
        };
    }
    
    default TokenParser zeroOrMore() {
        return source -> {
            List<Token> result = new ArrayList<>();
            Optional<List<Token>> element;
            while((element = tryParse(source, true)).isPresent()) {
                result.addAll(element.get());
            }
            return result;
        };
    }
    
    default TokenParser zeroOrMore(TokenParser separator, boolean skipWhiteSpaces) {
        return source -> {
            List<Token> result = new ArrayList<>();
            Optional<List<Token>> element = tryParse(source, skipWhiteSpaces);
            if(element.isPresent()) {
                result.addAll(element.get());
                while(separator.tryParse(source, skipWhiteSpaces).isPresent()) {
                    result.addAll(parse(source, skipWhiteSpaces));
                }
            }
            return result;
        };
    }
    
    default TokenParser oneOrMore(boolean skipWhiteSpaces) {
        return source -> {
            List<Token> result = new ArrayList<>();
            result.addAll(parse(source, true));
            Optional<List<Token>> element;
            while((element = tryParse(source, skipWhiteSpaces)).isPresent()) {
                result.addAll(element.get());
            }
            return result;
        };
    }
    
    default TokenParser oneOrMore(TokenParser separator, boolean skipWhiteSpaces) {
        return source -> {
            List<Token> result = new ArrayList<>();
            result.addAll(parse(source, true));
            while(separator.tryParse(source, skipWhiteSpaces).isPresent()) {
                result.addAll(parse(source, skipWhiteSpaces));
            }
            return result;
        };
    }
    
    default TokenParser filter(Predicate predicate) {
        return source -> {
            List<Token> result = parse(source, true);
            
            if (!predicate.test(result)) {
                throw new Exception("Does not respect filter");
            }
            
            return result;
        };
    }
    
    default TokenParser map(Function<List<Token>, List<Token>> function) {
        return source -> {
            return function.apply(parse(source, true));
        };
    }
    
    static TokenParser from(TokenType type, Pattern pattern, boolean trim) {
        Function<String, String> function = Function.identity();
        if (trim) {
            function = String::trim;
        }
        
        return from(type, pattern, function);
    }
    
    static TokenParser from(TokenType type, Pattern pattern, Function<String, String> operation) {
        return source -> {
            Matcher matcher = pattern.matcher(source);
            if(!matcher.lookingAt()) {
                String msg = String.format("%s not found at %s", pattern.pattern(), source.getPosition());
                throw new Exception(msg);
            }
            String value = source.substring(matcher.end());
            value = operation.apply(value);
            Token token = new Token(type, value, source.getPosition());
            source.advance(matcher.end());
            return new ArrayList(Collections.singletonList(token));
        };
    }
    
    static TokenParser until(TokenType type, String... values) {
        return until(type, Arrays.asList(values));
    }
    
    static TokenParser until(TokenType type, List<String> values) {
        StringBuilder sb = new StringBuilder("^.*?(?=");
        sb.append(values.stream().map(Pattern::quote).collect(Collectors.joining("|")));
        sb.append(")");
        return from(type, Pattern.compile(sb.toString(), Pattern.DOTALL), true);
    }
    
    static TokenParser value(TokenType type, String value) {
        return from(type, Pattern.compile(Pattern.quote(value)), true);
    }
    
    static TokenParser in(TokenType type, String[] values) {
        StringBuilder regex = new StringBuilder("^");
        boolean isFirst = true;

        for (String operator : values) {
            if (isFirst) {
                isFirst = false;
            } else {
                regex.append("|");
            }
            regex.append(Pattern.quote(operator));

            char nextChar = operator.charAt(operator.length() - 1);
            if (Character.isLetter(nextChar) || Character.getType(nextChar) == Character.LETTER_NUMBER) {
                regex.append("(?![a-zA-Z])");
            }
        }
        return from(type, Pattern.compile(regex.toString()), true);
    }
    
    static TokenParser in(TokenType type, String value) {
        return in(type, new String[]{value});
    }
}
