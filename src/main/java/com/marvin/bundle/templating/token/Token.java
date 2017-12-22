package com.marvin.bundle.templating.token;

import java.util.Objects;
import com.marvin.bundle.templating.Position;
import java.util.Arrays;

public class Token {
    
    private TokenType type;
    
    private String value;
    
    private Position position;

    public Token(TokenType type) {
        this.type = type;
    }

    public Token(TokenType type, String value, Position position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }
    
    public boolean isA(TokenType type) {
        return this.type.equals(type);
    }
    
    public boolean isA(TokenType type, String value) {
        return isA(type) && Objects.equals(this.value, value);
    }
    
    public boolean isA(TokenType type, String... values) {
        boolean test = true;
        
        if (values.length > 0) {
            test = Arrays.asList(values).contains(this.value);
        }
        
        return test && isA(type);
    }


    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }
    
    public static boolean isEOF(Token token) {
        return token.isA(TokenType.EOF);
    }
    
    public static Token EOF() {
        return new Token(TokenType.EOF);
    }
    
    public static Token text(String text, Position position) {
        return new Token(TokenType.TEXT, text, position);
    }

    @Override
    public String toString() {
        return "Token{" + "type=" + type + ", value=" + value + ", position=" + position + '}';
    }
    
}
