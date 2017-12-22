package com.marvin.bundle.templating.token;

import java.util.List;
import com.marvin.bundle.templating.Source;

public class Tokenizer {
    
    private TokenParser tokenParser;
    
    public List<Token> tokenize(Source source) throws Exception {
        return this.tokenParser.parse(source, true);
    }
    
    public TokenParser getTokenParser() {
        return tokenParser;
    }

    public void setTokenParser(TokenParser tokenParser) {
        this.tokenParser = tokenParser;
    }
    
    public static TokenizerBuilder builder() {
        return new TokenizerBuilder();
    }
    
    public static class TokenizerBuilder {
        private TokenParser tokenParser;
        
        public TokenizerBuilder parser(TokenParser parser) {
            this.tokenParser = parser;
            return this;
        }
        
        public Tokenizer build() {
            Tokenizer tokenizer = new Tokenizer();
            tokenizer.setTokenParser(tokenParser);
            return tokenizer;
        }
    }
}
