package com.marvin.bundle.templating.token;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class TokenStream {

    private final ArrayList<Token> tokens = new ArrayList<>();
    private int current;

    /**
     * Constructor for a Token Stream
     *
     * @param tokens A collection of tokens
     */
    public TokenStream(Collection<Token> tokens) {
        this.tokens.addAll(tokens);
        this.current = 0;
    }

    /**
     * Consumes and returns the next token in the stream.
     *
     * @return The next token
     */
    public Token next() {
        return tokens.get(++current);
    }

    /**
     * Checks the current token to see if it matches the provided type. If it
     * doesn't match this will throw a SyntaxException. This will consume a
     * token.
     *
     * @param type The type of token that we expect
     * @return Token The current token
     */
    public Token expect(TokenType type) {
        return expect(type, null);
    }

    /**
     * Checks the current token to see if it matches the provided type. If it
     * doesn't match this will throw a SyntaxException. This will consume a
     * token.
     *
     * @param type The type of token that we expect
     * @param value The expected value of the token
     * @return Token The current token
     */
    public Token expect(TokenType type, String value) {
        Token token = tokens.get(current);

        boolean success = Objects.isNull(value) 
                ? token.isA(type) : token.isA(type, value);

        if (!success) {
            String message = String.format("Unexpected token of value \"%s\" and type %s, expected token of type %s and value %s",
                    token.getValue(), token.getType(), type, value);
            throw new IllegalStateException(message);
        }
        this.next();
        return token;
    }

    /**
     * Returns the next token in the stream without consuming it.
     *
     * @return The next token
     */
    public Token peek() {
        return peek(1);
    }

    /**
     * Returns a future token in the stream without consuming any.
     *
     * @param number How many tokens to lookahead
     * @return The token we are peeking at
     */
    public Token peek(int number) {
        return this.tokens.get(this.current + number);
    }

    @Override
    public String toString() {
        return tokens.toString();
    }

    /**
     * Looks at the current token. Does not consume the token.
     *
     * @return Token The current token
     */
    public Token current() {
        return this.tokens.get(current);
    }

    /**
     * used for testing purposes
     *
     * @return List of tokens
     */
    public ArrayList<Token> getTokens() {
        return tokens;
    }
}
