package com.marvin.bundle.templating.extention;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
public interface Filter<T, R> {
    
    R apply(T input, Map<String, Object> args);
    
    default Filter bindDefaults(Map<String, Object> defaults) {
        return Filter.map(this, args -> {
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
    
    static Filter fromFunction(Function function) {
        return (input, args) -> function.apply(input);
    }
    
    static Filter fromBiFunction(BiFunction function) {
        return (input, args) -> {
            if (args.size() > 1) {
                throw new IllegalArgumentException("Too much arguments for a consumer");
            }
            
            Optional<Object> value = args.values().stream().findFirst();
            
            return function.apply(input, value.get());
        };
    }
    
    static Filter from(BiFunction function) {
        return (input, args) -> function.apply(input, args);
    }
    
    static Filter map(Filter base, Function<Map<String, Object>, Map<String, Object>> function) {
        return (in, args) -> base.apply(in, function.apply(args));
    }
    
    static FilterBuilder builder() {
        return new FilterBuilder();
    }
    
    public class FilterBuilder {
        
        private Filter base;
        private Map<String, Object> defaults = new LinkedHashMap<>();
        
        public FilterBuilder fromBiFunction(BiFunction function) {
            return withBase(Filter.fromBiFunction(function));
        }
        
        public FilterBuilder fromFunction(Function function) {
            return withBase(Filter.fromFunction(function));
        }
        
        public FilterBuilder withBase(Filter base) {
            this.base = base;
            return this;
        }
        
        public FilterBuilder withArg(String name) {
            return withArg(name, null);
        }
        
        public FilterBuilder withArg(String name, Object defaultValue) {
            this.defaults.put(name, defaultValue);
            return this;
        }
        
        public Filter build() {
            return base.bindDefaults(defaults);
        }
    }
}
