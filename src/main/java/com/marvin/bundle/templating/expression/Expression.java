package com.marvin.bundle.templating.expression;

import java.util.Collections;
import java.util.List;
import com.marvin.bundle.templating.Context;
import com.marvin.bundle.templating.extention.Directive;
import com.marvin.bundle.templating.extention.Filter;
import com.marvin.bundle.templating.extention.Test;
import com.marvin.component.util.ClassUtils;
import com.marvin.component.util.ReflectionUtils;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@FunctionalInterface
public interface Expression<T> {

    T evaluate(Context context);
    
    default T evaluate(){
        return evaluate(null);
    }
    
    static Expression<String> functionName(String value) {
        return literalString(value);
    }
    
    static Expression var(String value) {
        return context -> context.evaluate(value);
    }
    
    static Expression<String> varName(String value) {
        return literalString(value);
    }
    
    static Expression attribute(Expression expression, Expression<String> attributeNameExpression, Expression<Map<String, Object>> argsExpression) {
        return context -> {
            Object object = expression.evaluate(context);
            String attributeName = attributeNameExpression.evaluate(context);
            Map<String, Object> args = new HashMap<>();
            Object result = null;
            Object[] argumentValues = new Object[0];
            if (null != argsExpression) {
                args = argsExpression.evaluate(context);
                argumentValues = args.values().toArray();
            }

            Member member = null;
            if (object != null) {
                if (null != argsExpression) {
                     // first we check maps
                    if (object instanceof Map && ((Map<?, ?>) object).containsKey(attributeName)) {
                        return ((Map<?, ?>) object).get(attributeName);
                    }
                    try {

                        // then we check arrays
                        if (object.getClass().isArray()) {
                            int index = Integer.parseInt(attributeName);
                            int length = Array.getLength(object);
                            if (index < 0 || index >= length) {
                                return null;
                            }
                            return Array.get(object, index);
                        }

                        // then lists
                        if (object instanceof List) {
                            List<Object> list = (List<Object>) object;

                            int index = Integer.parseInt(attributeName);
                            int length = list.size();

                            if (index < 0 || index >= length) {
                                return null;
                            }

                            return list.get(index);
                        }
                    } catch (NumberFormatException ex) {
                        // do nothing
                    }
                }
                
                Class<?>[] argumentTypes = ReflectionUtils.resolveArgumentsTypes(argumentValues);
                Class<?> clazz = object.getClass();
                
                member = ClassUtils.findMember(clazz, attributeName, argumentTypes);
            }
            
            if (object != null && member != null) {
                try {
                    if (member instanceof Method) {
                        result = ((Method) member).invoke(object, argumentValues);
                    } else if (member instanceof Field) {
                        result = ((Field) member).get(object);
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            return result;
        };
    }
    
    static Expression test(String name, Expression left, Expression<Map<String, Object>> right) {
        return context -> {
            Test test = context.getEngine().getTest(name);
            if (null != test) {
                return test.apply(left.evaluate(context), right.evaluate(context));
            }
            return null;
        }; 
    }
    
    static Expression filter(String name, Expression valueExpression, Expression<Map<String, Object>> argsExpression) {
       return context -> {
            Filter filter = context.getEngine().getFilter(name);
            if (null != filter) {
                return filter.apply(valueExpression.evaluate(context), argsExpression.evaluate(context));
            }
            return null;
        }; 
    }
    
    static Expression directive(String name, Expression<Map<String, Object>> argsExpression) {
        return context -> {
            Directive directive = context.getEngine().getDirective(name);
            if (null != directive) {
                return directive.apply(argsExpression.evaluate(context));
            }
            return null;
        };
    }
    
    static Expression binaryOperator(String name, Expression left, Expression<Map<String, Object>> right) {
        return context -> {
            Filter filter = context.getEngine().getFilter(name);
            if (null != filter) {
                return filter.apply(left.evaluate(context), right.evaluate(context));
            }
            
            Test test = context.getEngine().getTest(name);
            if (null != test) {
                return test.apply(left.evaluate(context), right.evaluate(context));
            }
            
            return null;
        };
    }
    
    static Expression<Map<String, Object>> emptyArguments() throws Exception {
        return arguments(Collections.EMPTY_LIST, Collections.EMPTY_MAP);
    }
    
    static Expression<Map<String, Object>> arguments(List<Expression> positionalArgs, Map<String, Expression> namedArgs) throws Exception {
        Map<String, Expression> args = new HashMap<>();
        if (namedArgs == null || namedArgs.isEmpty()) {
            if (positionalArgs != null && !positionalArgs.isEmpty()) {
                for (int i = 0; i < positionalArgs.size(); i++) {
                    args.put(String.valueOf(i), positionalArgs.get(i));
                }
            }
        } else {
            if (positionalArgs != null) {
                int nameIndex = 0;
                for (Expression arg : positionalArgs) {
                    if (namedArgs.size() <= nameIndex) {
                        String msg = String.format("The argument at position %s is not allowed. Only %s argument(s) are allowed.", nameIndex + 1, namedArgs.size());
                        throw new Exception(msg);
                    }

                    args.put(String.valueOf(nameIndex), arg);
                    nameIndex++;
                }
            }

            if (!namedArgs.isEmpty()) {
                for (Map.Entry<String, Expression> entry : namedArgs.entrySet()) {
                    String name = entry.getKey();
                    Expression expression = entry.getValue();
                    if (!namedArgs.keySet().contains(name)) {
                        String msg = String.format("The following named argument does not exist: %s", name);
                        throw new Exception(msg);
                    }
                    args.put(name, expression);
                }
            }
        }
        
        return context -> {
            Map<String, Object> result = new HashMap<>();
            args.forEach((name, expression) -> {
                result.put(name, expression.evaluate(context));
            });
            return result;
        };
    }
    
    static Expression binary(Expression left, Expression right, BiFunction consumer) {
        return context -> consumer.apply(left.evaluate(context), right.evaluate(context));
    }
    
    static Expression unary(Expression right, Function consumer) {
        return context -> consumer.apply(right.evaluate(context));
    }
    
    static Expression<Boolean> literalNull() {
        return context -> null;
    }
    
    static Expression<Boolean> literalBoolean(Boolean value) {
        return context -> value;
    }
    
    static Expression<String> literalString(String value) {
        return context -> value;
    }
    
    static Expression<Double> literalDouble(String value) {
        return context -> Double.parseDouble(value);
    }
    
    static Expression<Long> literalLong(String value) {
        return context -> Long.parseLong(value);
    }
    
    static Expression<List> emptyList() {
        return context -> Collections.emptyList();
    }
    
    static Expression<List> array(List<Expression> expressions) {
        return (context) -> expressions.stream()
                    .map((expr) -> expr == null ? null : expr.evaluate(context))
                    .collect(Collectors.toList());
    }
}
