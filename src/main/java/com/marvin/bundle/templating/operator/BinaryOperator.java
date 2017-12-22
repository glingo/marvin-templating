package com.marvin.bundle.templating.operator;

import java.util.function.BiFunction;

public class BinaryOperator extends Operator {
    
    protected int precedence;
    protected Associativity associativity;
    protected BiFunction function;

    public BinaryOperator(int precedence, BiFunction function, Associativity associativity) {
        this.associativity = associativity;
        this.precedence = precedence;
        this.function = function;
    }
    
    public Associativity getAssociativity() {
        return associativity;
    }
    
    public int getPrecedence(){
        return this.precedence;
    }

    public BiFunction getFunction() {
        return function;
    }
}
