package com.marvin.bundle.templating.extention;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface Directive<R extends Object> {
    
    <R> R apply(Map<String, Object> args);
    
    static Directive bindDefaults(Directive base, Map<String, Object> defaults) {
        return Directive.map(base, args -> {
                int argIndex = 0;
                
                for (Map.Entry<String, Object> entry : defaults.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    String index = String.valueOf(argIndex);
                    
                    if(!args.containsKey(key)) {
                        
                        if (!args.containsKey(index)) {
                            args.put(index, value);
                        }

                        if (args.containsKey(index)) {
                            args.put(key, args.get(index));
                            args.remove(index);
                        }
                    }
                    
                    if (args.containsKey(key)) {
                        args.put(key, args.getOrDefault(key, value));
                    }

                    argIndex++;
                }
                return args;
            });
    }
    
    static Directive map(Directive base, Function<Map<String, Object>, Map<String, Object>> function) {
        return args -> base.apply(function.apply(args));
    }
    
    static DirectiveBuilder builder() {
        return new DirectiveBuilder();
    }
    
    public class DirectiveBuilder {
        
        private Directive base;
        private Map<String, Object> defaults = new LinkedHashMap<>();
        
        public DirectiveBuilder withBase(Directive<Object> base) {
            this.base = base;
            return this;
        }
        
        public DirectiveBuilder withArg(String name) {
            return withArg(name, null);
        }
        
        public DirectiveBuilder withArg(String name, Object defaultValue) {
            this.defaults.put(name, defaultValue);
            return this;
        }
        
        public Directive build() {
            return Directive.bindDefaults(base, defaults);
        }
    }
    
    static Directive fromConsumer(Consumer consumer) {
        return args -> {
            if (args.size() != 1) {
                throw new IllegalArgumentException("Too much arguments for a consumer");
            }
            
            List<Object> values = new LinkedList<>();
            Optional first = args.values().stream().findFirst();
            
            if (first.isPresent()) {
                consumer.accept(first.get());
            }
            
            return null;
        };
    }
    
    static Directive fromBiConsumer(BiConsumer consumer) {
        return args -> {
            if (args.size() > 2) {
                throw new IllegalArgumentException("Too much arguments for a consumer");
            }
            
            List<Object> values = new LinkedList<>();
            args.values().stream().limit(2).forEach(values::add);
            
            consumer.accept(values.get(0), values.get(1));
            return null;
        };
    }
    
    static Directive fromFunction(Function function) {
        return args -> {
            if (args.size() != 1) {
                throw new IllegalArgumentException("Wrong number of arguments");
            }
            
            List<Object> values = new LinkedList<>();
            Optional first = args.values().stream().findFirst();
            
            if (first.isPresent()) {
                return function.apply(first.get());
            }
            
            return null;
        };
    }
    
    static Directive fromBiFunction(BiFunction function) {
        return args -> {
            if (args.size() != 2) {
                throw new IllegalArgumentException("Wrong number of arguments");
            }
            
            List<Object> values = new LinkedList<>();
            args.values().stream().limit(2).forEach(values::add);
            
            return function.apply(values.get(0), values.get(1));
        };
    }
}