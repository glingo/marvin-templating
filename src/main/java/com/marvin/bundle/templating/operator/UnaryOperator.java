package com.marvin.bundle.templating.operator;

import java.util.function.Function;

public class UnaryOperator extends Operator {
    
    protected int precedence;
    
    protected Function function;

    public UnaryOperator(int precedence, Function function) {
        super();
        this.function = function;
        this.precedence = precedence;
    }
    
    public int getPrecedence(){
        return this.precedence;
    }

    public Function getFunction() {
        return function;
    }
}
