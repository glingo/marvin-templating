package com.marvin.bundle.templating.extention.core;

import com.marvin.bundle.templating.extention.Directive;
import com.marvin.bundle.templating.extention.Extension;
import com.marvin.bundle.templating.extention.Filter;
import com.marvin.bundle.templating.extention.Test;
import com.marvin.bundle.templating.extention.core.node.ImportNode;
import com.marvin.bundle.templating.extention.core.node.SetNode;
import com.marvin.bundle.templating.node.NodeParser;
import com.marvin.bundle.templating.node.NodeVisitorFactory;
import com.marvin.bundle.templating.operator.Associativity;
import com.marvin.bundle.templating.operator.BinaryOperator;
import com.marvin.bundle.templating.operator.UnaryOperator;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CoreExtension implements Extension {

    @Override
    public List<Class> getSafeNodes() {
        return Arrays.asList(ImportNode.class, SetNode.class);
    }
    
    @Override
    public List<NodeVisitorFactory> getNodeVisitorFactories() {
        List<NodeVisitorFactory> factories = new ArrayList<>();
        factories.add(new CoreNodeVisitorFactory());
        return factories;
    }

    @Override
    public Map<String, Test> getTests() {
        Map<String, Test> tests = new HashMap<>();
        tests.put("empty", CoreTests.isEmpty());
        tests.put("even", CoreTests.isEven());
        tests.put("iterable", CoreTests.isIterable());
        tests.put("map", CoreTests.isMap());
        tests.put("null", CoreTests.isNull());
        tests.put("odd", CoreTests.isOdd());
        tests.put("defined", CoreTests.isDefined());
        return tests;
    }
    
    @Override
    public Map<String, Directive> getDirectives() {
        Map<String, Directive> directives = new HashMap<>();
        directives.put("max", CoreDirectives.max());
        directives.put("min", CoreDirectives.min());
        directives.put("range", CoreDirectives.range());
        return directives;
    }

    @Override
    public Map<String, NodeParser> getNodeParsers() {
        Map<String, NodeParser> parsers = new HashMap<>();
        parsers.put("if", CoreNodeParsers.ifNodeParser());
        parsers.put("for", CoreNodeParsers.forNodeParser());
        parsers.put("extends", CoreNodeParsers.extendsNodeParser());
        parsers.put("block", CoreNodeParsers.blockNodeParser());
        parsers.put("import", CoreNodeParsers.importNodeParser());
        parsers.put("set", CoreNodeParsers.setNodeParser());
        return parsers;
    }
    
    @Override
    public Map<String, Filter> getFilters() {
        Map<String, Filter> filters = new HashMap<>();
        filters.put("abbreviate", CoreFilters.abbreviate());
        filters.put("abs", CoreFilters.abs());
        filters.put("capitalize", CoreFilters.capitalize());
        filters.put("upper", CoreFilters.upper());
        filters.put("lower", CoreFilters.lower());
        filters.put("default", CoreFilters.defaults());
        filters.put("date", CoreFilters.date());
        filters.put("first", CoreFilters.first());
        filters.put("join", CoreFilters.join());
        filters.put("last", CoreFilters.last());
        filters.put("numberformat", CoreFilters.numberFormat());
        filters.put("slice", CoreFilters.slice());
        filters.put("sort", CoreFilters.sort());
        filters.put("rsort", CoreFilters.rsort());
        filters.put("title", CoreFilters.title());
        filters.put("trim", CoreFilters.trim());
        filters.put("urlencode", CoreFilters.urlEncode());
        filters.put("length", CoreFilters.length());
        // not tested
        filters.put("replace", CoreFilters.replace());
        filters.put("merge", CoreFilters.merge());
        return filters;
    }

    @Override
    public Map<String, BinaryOperator> getBinaryOperators() {
        Map<String, BinaryOperator> operators = new LinkedHashMap<>();
        operators.put("or", new BinaryOperator(10, ObjectUtils::or, Associativity.LEFT));
        operators.put("and", new BinaryOperator(15, ObjectUtils::and, Associativity.LEFT));
        
        operators.put("is not", new BinaryOperator(20, ObjectUtils::negative, Associativity.LEFT));
        operators.put("is", new BinaryOperator(20, ObjectUtils::test, Associativity.LEFT));
        
        operators.put("+", new BinaryOperator(40, ObjectUtils::add, Associativity.RIGHT));
        operators.put("-", new BinaryOperator(40, ObjectUtils::subtract, Associativity.RIGHT));
        
        operators.put("%", new BinaryOperator(60, ObjectUtils::mod, Associativity.RIGHT));
        operators.put("/", new BinaryOperator(60, ObjectUtils::divide, Associativity.RIGHT));
        operators.put("*", new BinaryOperator(60, ObjectUtils::multiply, Associativity.RIGHT));
        
        operators.put(">=", new BinaryOperator(30, ObjectUtils::gte, Associativity.RIGHT));
        operators.put(">", new BinaryOperator(30, ObjectUtils::gt, Associativity.RIGHT));
        operators.put("<=", new BinaryOperator(30, ObjectUtils::lte, Associativity.RIGHT));
        operators.put("<", new BinaryOperator(30, ObjectUtils::lt, Associativity.RIGHT));
        operators.put("==", new BinaryOperator(30, ObjectUtils::equals, Associativity.RIGHT));
        operators.put("equals", new BinaryOperator(30, ObjectUtils::equals, Associativity.RIGHT));
        
        operators.put("|", new BinaryOperator(100, ObjectUtils::second, Associativity.LEFT));
        operators.put("~", new BinaryOperator(110, ObjectUtils::concatenate, Associativity.LEFT));
        operators.put("..", new BinaryOperator(120, ObjectUtils::enumerate, Associativity.LEFT));

        return operators;
    }

    @Override
    public Map<String, UnaryOperator> getUnaryOperators() {
        Map<String, UnaryOperator> operators = new HashMap<>();
        operators.put("not", new UnaryOperator(5, ObjectUtils::not));
        operators.put("+", new UnaryOperator(500, ObjectUtils::unaryPlus));
        operators.put("-", new UnaryOperator(500, ObjectUtils::unaryMinus));
        return operators;
    }
}
