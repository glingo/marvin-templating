package com.marvin.bundle.templating.token;

import java.util.Arrays;
import java.util.regex.Pattern;

public interface TokenParsers {
    
    public static final Pattern REGEX_STRING = Pattern.compile("((\").*?(?<!\\\\)(\"))|((').*?(?<!\\\\)('))", Pattern.DOTALL);
    
    public static final Pattern REGEX_NUMBER = Pattern.compile("^[0-9]+(\\.[0-9]+)?");
    
    public static final Pattern REGEX_NAME = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*");
        
    public static final String PUNCTUATIONS = "()[]{}?:.,|=";

    static TokenParser text() {
        return TokenParser.from(TokenType.TEXT, Pattern.compile(".*"), false);
    }
    
    static TokenParser string() {
        return TokenParser.from(TokenType.STRING, REGEX_STRING, value -> {
            char quotationType = value.charAt(0);
            // remove backslashes used to escape inner quotation marks
            value = value.substring(1, value.length() - 1);
            if (quotationType == '\'') {
                value = value.replaceAll("\\\\(')", "$1");
            } else if (quotationType == '"') {
                value = value.replaceAll("\\\\(\")", "$1");
            }
            
            return value;
        });
    }
    
    static TokenParser name() {
        return TokenParser.from(TokenType.NAME, REGEX_NAME, true);
    }
    
    static TokenParser number() {
        return TokenParser.from(TokenType.NUMBER, REGEX_NUMBER, true);
    }
    
    static TokenParser punctuation() {
        return TokenParser.in(TokenType.PUNCTUATION, PUNCTUATIONS.split(""));
    }
    
    static TokenParser EOF() {
        return text().then(source -> {
            if (source.length() == 0) {
                return Arrays.asList(Token.EOF());
            }
            
            String msg = String.format("Expected EOF Token but there is still data to tokenize (%s)", source);
            throw new Exception(msg);
        });
    }
    
}
