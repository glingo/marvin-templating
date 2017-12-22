package com.marvin.bundle.templating.token;

public enum TokenType {
    
    EOF,
    OPERATOR,
    STRING,
    PUNCTUATION,
    NUMBER,
    NAME,
    TEXT,
    
    PRINT_OPEN,
    PRINT_CLOSE,
    
    EXECUTE_OPEN,
    EXECUTE_CLOSE,
    
    COMMENT_OPEN,
    COMMENT_CLOSE
}
