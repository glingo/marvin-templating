package com.marvin.bundle.templating.token;

@FunctionalInterface
public interface TokenVisitor {
    
    void visit(Token token);
}
