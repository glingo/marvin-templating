package com.marvin.bundle.templating.extention.core;

import com.marvin.bundle.templating.extention.Directive;
import java.util.ArrayList;
import java.util.List;

public interface CoreDirectives {
    
    static Directive max() {
        Directive directive = (args) -> {
            Object max = null;
            int i = 0;
            while (args.containsKey(String.valueOf(i))) {
                Object candidate = args.get(String.valueOf(i));
                i++;
                if (max == null) {
                    max = candidate;
                    continue;
                }
                if (ObjectUtils.gt(candidate, max)) {
                    max = candidate;
                }
            }
            
            return max;
        };
        
        return Directive.builder()
                .withBase(directive)
                .build();
    }
    
    static Directive min() {
        Directive directive = (args) -> {
            Object min = null;
            int i = 0;

            while (args.containsKey(String.valueOf(i))) {
                Object candidate = args.get(String.valueOf(i));
                i++;

                if (min == null) {
                    min = candidate;
                    continue;
                }
                if (ObjectUtils.lt(candidate, min)) {
                    min = candidate;
                }
            }
            return min;
        };
        return Directive.builder()
                .withBase(directive)
                .build();
    }
    
    static Directive range() {
        Directive directive = (args) -> {
            Object start = args.get("start");
            Object end = args.get("end");
            Object increment = (Object) args.get("increment");
            
            if (!(increment instanceof Number)) {
                throw new IllegalArgumentException("The increment of the range function must be a number " + increment);
            }

            Long incrementNum = ((Number) increment).longValue();
            List<Object> results = new ArrayList<>();
            // Iterating over Number
            if (start instanceof Number && end instanceof Number) {
                Long startNum = ((Number) start).longValue();
                Long endNum = ((Number) end).longValue();

                if (incrementNum > 0) {
                    for (Long i = startNum; i <= endNum; i += incrementNum) {
                        results.add(i);
                    }
                } else if (incrementNum < 0) {
                    for (Long i = startNum; i >= endNum; i += incrementNum) {
                        results.add(i);
                    }
                } else {
                    throw new IllegalArgumentException("The increment of the range function must be different than 0");
                }
            } else if (start instanceof String && end instanceof String) {
                String startStr = (String) start;
                String endStr = (String) end;
                if (startStr.length() != 1 || endStr.length() != 1) {
                    throw new IllegalArgumentException("Arguments of range function must be of type Number or String with "
                            + "a length of 1");
                }

                char startChar = startStr.charAt(0);
                char endChar = endStr.charAt(0);

                if (incrementNum > 0) {
                    for (int i = startChar; i <= endChar; i += incrementNum) {
                        results.add((char) i);
                    }
                } else if (incrementNum < 0) {
                    for (int i = startChar; i >= endChar; i += incrementNum) {
                        results.add((char) i);
                    }
                } else {
                    throw new IllegalArgumentException("The increment of the range function must be different than 0");
                }
            } else {
                throw new IllegalArgumentException("Arguments of range function must be of type Number or String with a "
                        + "length of 1");
            }

            return results;
        };
        return Directive.builder()
                .withBase(directive)
                .withArg("start")
                .withArg("end")
                .withArg("increment", 1L)
                .build();
    }
}
