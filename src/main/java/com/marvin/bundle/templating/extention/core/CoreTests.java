package com.marvin.bundle.templating.extention.core;

import com.marvin.bundle.templating.extention.Test;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public interface CoreTests {
    
    public static Test isEmpty() {
        return (Object input, Map<String, Object> args) -> {
            boolean isEmpty = input == null;
            
            if (!isEmpty && input instanceof String) {
                String value = (String) input;
                isEmpty = value.trim().isEmpty();
            }
            
            if (!isEmpty && input instanceof Collection) {
                isEmpty = ((Collection<?>) input).isEmpty();
            }
            
            if (!isEmpty && input instanceof Map) {
                isEmpty = ((Map<?, ?>) input).isEmpty();
            }
            
            return isEmpty;
        };
    }
    
    public static Test isEven() {
        return (Object input, Map<String, Object> args) -> {
            if (input == null) {
                throw new IllegalArgumentException("Can not pass null value to \"even\" test.");
            }

            if (input instanceof Integer) {
                return ((Integer) input) % 2 == 0;
            }
            
            return ((Long) input) % 2 == 0;
        };
    }
    
    public static Test isIterable() {
        return (input, args) -> input instanceof Iterable;
    }
    
    public static Test isMap() {
        return (input, args) -> input instanceof Map;
    }
    
    public static Test isNull() {
        return (input, args) ->  Objects.isNull(input);
    }
    
    public static Test isOdd() {
        return (input, args) ->  !isEven().apply(input, args);
    }
    
    public static Test isDefined() {
        return (input, args) ->  !isNull().apply(input, args);
    }
}
