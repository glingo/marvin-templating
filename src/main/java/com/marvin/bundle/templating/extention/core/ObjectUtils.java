package com.marvin.bundle.templating.extention.core;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * This class acts as a sort of wrapper around Java's built in operators. This
 * is necessary because Template engine treats all user provided variables as Objects
 * even if they were originally primitives.
 * <p>
 * It's important that this class mimics the natural type conversion that Java
 * will apply when performing operators. This can be found in section 5.6.2 of
 * the Java 7 spec, under Binary Numeric Promotion.
 *
 */
public class ObjectUtils {

    private enum Operation {
        ADD, SUBTRACT, MULTIPLICATION, DIVISION, MODULUS
    }

    private enum Comparison {
        GREATER_THAN, GREATER_THAN_EQUALS, LESS_THAN, LESS_THAN_EQUALS, EQUALS
    }
    
    public static Object mergeAsMap(Map<?, ?> arg1, Object arg2) {
        Map<?, ?> collection1 = arg1;
        Map<Object, Object> output = null;
        if (arg2 instanceof Map) {
            Map<?, ?> collection2 = (Map<?, ?>) arg2;
            output = new HashMap<>(collection1.size() + collection2.size() + 16);
            output.putAll(collection1);
            output.putAll(collection2);
        } else if (arg2 instanceof List) {
            List<?> collection2 = (List<?>) arg2;
            output = new HashMap<>(collection1.size() + collection2.size() + 16);
            output.putAll(collection1);
            for (Object o : collection2) {
                output.put(o, o);
            }
        } else {
            throw new UnsupportedOperationException(
                    "Currently, only Maps and Lists can be merged with a Map. Arg2: " + arg2.getClass().getName());
        }
        return output;
    }

    public static Object mergeAsList(List<?> arg1, Object arg2) {
        List<?> collection1 = arg1;
        List<Object> output = null;
        if (arg2 instanceof Map) {
            Map<?, ?> collection2 = (Map<?, ?>) arg2;
            output = new ArrayList<>(collection1.size() + collection2.size() + 16);
            output.addAll(collection1);
            output.addAll(collection2.entrySet());
        } else if (arg2 instanceof List) {
            List<?> collection2 = (List<?>) arg2;
            output = new ArrayList<>(collection1.size() + collection2.size() + 16);
            output.addAll(collection1);
            output.addAll(collection2);
        } else {
            throw new UnsupportedOperationException(
                    "Currently, only Maps and Lists can be merged with a List. Arg2: " + arg2.getClass().getName());
        }
        return output;
    }

    public static Object mergeAsArray(Object arg1, Object arg2) {
        Class<?> arg1Class = arg1.getClass().getComponentType();
        Class<?> arg2Class = arg2.getClass().getComponentType();
        if (!arg1Class.equals(arg2Class)) {
            throw new UnsupportedOperationException(
                    "Currently, only Arrays of the same component class can be merged. Arg1: " + arg1Class.getName()
                            + ", Arg2: " + arg2Class.getName());
        }
        Object output = Array.newInstance(arg1Class, Array.getLength(arg1) + Array.getLength(arg2));
        System.arraycopy(arg1, 0, output, 0, Array.getLength(arg1));
        System.arraycopy(arg2, 0, output, Array.getLength(arg1), Array.getLength(arg2));
        return output;
    }
    
    public static Object slice(Object input, Object fromIndex, Object toIndex) {
        if (input == null) {
            return null;
        }
        if (!(fromIndex instanceof Number)) {
            String msg = String.format("Argument fromIndex must be a number. Actual type: %s", (fromIndex == null ? "null" : fromIndex.getClass().getName()));
            throw new IllegalArgumentException(msg);
        }
        
        int from = ((Number) fromIndex).intValue();
        if (from < 0) {
            throw new IllegalArgumentException("fromIndex must be greater than 0");
        }
        
        if (!(toIndex instanceof Number)) {
            String msg = String.format("Argument toIndex must be a number. Actual type: %s", (toIndex == null ? "null" : toIndex.getClass().getName()));
            throw new IllegalArgumentException(msg);
        }

        int length;
        if (input instanceof List) {
            length = ((List<?>) input).size();
        } else if (input.getClass().isArray()) {
            length = Array.getLength(input);
        } else if (input instanceof String) {
            length = ((String) input).length();
        } else {
            throw new IllegalArgumentException("Slice filter can only be applied to String, List and array inputs. Actual type was: "
                            + input.getClass().getName());
        }
        
        int to = ((Number) toIndex).intValue();
        
        if (to > length) {
            throw new IllegalArgumentException("toIndex must be smaller than input size: " + length);
        }
        
        if (from >= to) {
            throw new IllegalArgumentException("toIndex must be greater than fromIndex");
        }

        // slice input
        if (input instanceof List) {
            List<?> value = (List<?>) input;
            return new ArrayList<>(value.subList(from, to));
        }
        
        if (input.getClass().isArray()) {
            return sliceArray(input, from, to);
        }
        
        String value = (String) input;
        return value.substring(from, to);
    }
    
    public static Object sliceArray(Object input, int from, int to) {
        if (input instanceof Object[]) {
            return Arrays.copyOfRange((Object[]) input, from, to);
        } else if (input instanceof boolean[]) {
            return Arrays.copyOfRange((boolean[]) input, from, to);
        } else if (input instanceof byte[]) {
            return Arrays.copyOfRange((byte[]) input, from, to);
        } else if (input instanceof char[]) {
            return Arrays.copyOfRange((char[]) input, from, to);
        } else if (input instanceof double[]) {
            return Arrays.copyOfRange((double[]) input, from, to);
        } else if (input instanceof float[]) {
            return Arrays.copyOfRange((float[]) input, from, to);
        } else if (input instanceof int[]) {
            return Arrays.copyOfRange((int[]) input, from, to);
        } else if (input instanceof long[]) {
            return Arrays.copyOfRange((long[]) input, from, to);
        } else {
            return Arrays.copyOfRange((short[]) input, from, to);
        }
    }
    
    public static Object join(Object input, Object separator) {
        Collection<Object> inputCollection = (Collection<Object>) input;
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (Object entry : inputCollection) {
            if (!isFirst && separator != null) {
                builder.append(separator);
            }
            builder.append(entry);
            isFirst = false;
        }
        return builder.toString();
    }
    
    public static Object last(Object input) {
        if (input == null) {
            return null;
        }

        if(input instanceof String){
            String inputString = (String)input;
            return inputString.charAt(inputString.length() - 1);
        }

        if(input.getClass().isArray()) {
            int length = Array.getLength(input);
            return length > 0 ? Array.get(input, length - 1) : null;
        }
        
        Collection<Object> inputCollection = (Collection<Object>) input;
        Object result = null;
        Iterator<Object> iterator = inputCollection.iterator();
        while(iterator.hasNext()){
            result = iterator.next();
        }
        return result;
    }
    
    public static Object first(Object input) {
        if (input == null) {
            return null;
        }
        
        if(input instanceof String){
            String inputString = (String)input;
            return inputString.charAt(0);
        }

        if(input.getClass().isArray()) {
            int length = Array.getLength(input);
            return length > 0 ? Array.get(input, 0) : null;
        }
        
        Collection<?> inputCollection = (Collection<?>) input;
        return inputCollection.iterator().next();
    }
    
    public static Object formatNumber(Object input, String toFormat, Locale locale) {
        if (input == null) {
            return null;
        }
        Number number = (Number) input;

        if (toFormat != null) {
            Format format = new DecimalFormat(toFormat, new DecimalFormatSymbols(locale));
            return format.format(number);
        }
        
        NumberFormat numberFormat = NumberFormat.getInstance(locale);
        return numberFormat.format(number);
    }
    
    public static Object formatDate(Object input, String toFormat, String fromFormat, Locale locale) {
        if (input == null) {
            return null;
        }

        Date date = null;
        DateFormat existingFormat;
        DateFormat intendedFormat;

        intendedFormat = new SimpleDateFormat(toFormat, locale);

        if (fromFormat != null) {
            existingFormat = new SimpleDateFormat(fromFormat, locale);
            try {
                date = existingFormat.parse((String) input);
            } catch (ParseException e) {
                throw new RuntimeException("Could not parse date", e);
            }
        } else {
            date = (Date) input;
        }

        return intendedFormat.format(date);
    }
    
    public static Object abs(Object input) {
        if (input == null) {
            return null;
        }
        if (input instanceof Long) {
            return Math.abs((Long) input);
        } else {
            return Math.abs((Double) input);
        }
    }
    
    public static Object abbreviate(Object input, Object l) {
        if (input == null) {
            return null;
        }
        
        String value = (String) input;
        int maxWidth = ((Number) l).intValue();

        if(maxWidth < 0){
            throw new RuntimeException("Invalid argument to abbreviate filter; must be greater than zero");
        }

        String ellipsis = "...";
        int length = value.length();

        if (length < maxWidth) {
            return value;
        }
        
        if (length <= 3) {
            return value;
        }
        
        if(maxWidth <= 3){
            return value.substring(0, maxWidth);
        }
        
        return value.substring(0, Math.max(0, maxWidth - 3)) + ellipsis;
    }
       
    public static boolean isEmpty(Object input) {
        boolean isEmpty = input == null;
            
        if (!isEmpty && input instanceof String) {
            String value = (String) input;
            isEmpty = value.trim().isEmpty();
        }

        if (!isEmpty && input instanceof Collection) {
            isEmpty = ((Collection) input).isEmpty();
        }

        if (!isEmpty && input instanceof Map) {
            isEmpty = ((Map) input).isEmpty();
        }

        return isEmpty;
    }
    
    public static Object orDefault(Object input, Object defaultValue) {
        if (isEmpty(input)) {
            return defaultValue;
        }
        
        return input;
    }
    
    public static Object toLowerCase(Object input) {
        if (input == null) {
            return null;
        }
        return ((String) input).toLowerCase();
    }
    
    public static Object toUpperCase(Object input) {
        if (input == null) {
            return null;
        }
        return ((String) input).toUpperCase();
    }
    
    public static Object capitalize(Object input) {
        if (input == null) {
            return null;
        }
        String value = (String) input;

        if (value.length() == 0) {
            return value;
        }

        StringBuilder result = new StringBuilder();

        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (Character.isWhitespace(c)) {
                result.append(c);
            } else {
                result.append(Character.toTitleCase(c));
                result.append(Arrays.copyOfRange(chars, i + 1, chars.length));
                break;
            }
        }

        return result.toString();
    }
    
    public static Object concatenate(Object o1, Object o2) {
        StringBuilder result = new StringBuilder();
        if (o1 != null) {
            result.append(o1.toString());
        }
        
        if (o2 != null) {
            result.append(o2.toString());
        }
        return result.toString();
    }
    
    public static Object and(Object o1, Object o2) {
        return equals(o1, Boolean.TRUE) && equals(o2, Boolean.TRUE);
    }
    
    public static Object or(Object o1, Object o2) {
        return equals(o1, Boolean.TRUE) || equals(o2, Boolean.TRUE);
    }
    
    public static Object not(Object o1) {
        return equals(o1, Boolean.FALSE);
    }
    
    public static Object first(Object o1, Object o2) {
        return o1;
    }
    
    public static Object second(Object o1, Object o2) {
        return o2;
    }
    
    public static Object test(Object o1, Object o2) {
        return equals(o2, Boolean.TRUE);
    }
    
    public static Object negative(Object o1, Object o2) {
        return equals(o2, Boolean.FALSE);
    }
    
    public static Object enumerate(Object start, Object end) {
        Long increment = 1L;
        List<Object> results = new ArrayList<>();
        // Iterating over Number
        if (start instanceof Number && end instanceof Number) {
            Long startNum = ((Number) start).longValue();
            Long endNum = ((Number) end).longValue();

            if (increment > 0) {
                for (Long i = startNum; i <= endNum; i += increment) {
                    results.add(i);
                }
            } else if (increment < 0) {
                for (Long i = startNum; i >= endNum; i += increment) {
                    results.add(i);
                }
            } else {
                throw new IllegalArgumentException("The increment of the range function must be different than 0");
            }
        } else if (start instanceof String && end instanceof String) {
            // Iterating over character
            String startStr = (String) start;
            String endStr = (String) end;
            if (startStr.length() != 1 || endStr.length() != 1) {
                throw new IllegalArgumentException("Arguments of range function must be of type Number or String with "
                        + "a length of 1");
            }

            char startChar = startStr.charAt(0);
            char endChar = endStr.charAt(0);

            if (increment > 0) {
                for (int i = startChar; i <= endChar; i += increment) {
                    results.add((char) i);
                }
            } else if (increment < 0) {
                for (int i = startChar; i >= endChar; i += increment) {
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
    }

    public static Object add(Object op1, Object op2) {
        if (op1 instanceof String || op2 instanceof String) {
            return concatenateStrings(String.valueOf(op1), String.valueOf(op2));
        } else if (op1 instanceof List) {
            return addToList((List<?>) op1, op2);
        }
        return wideningConversionBinaryOperation(op1, op2, Operation.ADD);
    }

    public static Object subtract(Object op1, Object op2) {
        if (op1 instanceof List) {
            return subtractFromList((List<?>) op1, op2);
        }
        return wideningConversionBinaryOperation(op1, op2, Operation.SUBTRACT);
    }

    public static Object multiply(Object op1, Object op2) {
        return wideningConversionBinaryOperation(op1, op2, Operation.MULTIPLICATION);
    }

    public static Object divide(Object op1, Object op2) {
        return wideningConversionBinaryOperation(op1, op2, Operation.DIVISION);
    }

    public static Object mod(Object op1, Object op2) {
        return wideningConversionBinaryOperation(op1, op2, Operation.MODULUS);
    }

    public static boolean equals(Object op1, Object op2) {
        if (op1 != null && op1 instanceof Number && op2 != null && op2 instanceof Number) {
            return wideningConversionBinaryComparison(op1, op2, Comparison.EQUALS);
        } else if (op1 != null && op1 instanceof Enum<?> && op2 != null && op2 instanceof String) {
            return compareEnum((Enum<?>) op1, (String) op2);
        } else if (op2 != null && op2 instanceof Enum<?> && op1 != null && op1 instanceof String) {
            return compareEnum((Enum<?>) op2, (String) op1);
        } else {
            return ((op1 == op2) || ((op1 != null) && op1.equals(op2)));
        }
    }

    private static <T extends Enum<T>> boolean compareEnum(Enum<T> enumVariable, String compareToString) {
        return enumVariable.name().equals(compareToString);
    }

    public static boolean gt(Object op1, Object op2) {
        return wideningConversionBinaryComparison(op1, op2, Comparison.GREATER_THAN);
    }

    public static boolean gte(Object op1, Object op2) {
        return wideningConversionBinaryComparison(op1, op2, Comparison.GREATER_THAN_EQUALS);
    }

    public static boolean lt(Object op1, Object op2) {
        return wideningConversionBinaryComparison(op1, op2, Comparison.LESS_THAN);
    }

    public static boolean lte(Object op1, Object op2) {
        return wideningConversionBinaryComparison(op1, op2, Comparison.LESS_THAN_EQUALS);
    }

    public static Object unaryPlus(Object op1) {
        return multiply(1, op1);
    }

    public static Object unaryMinus(Object op1) {
        return multiply(-1, op1);
    }

    private static Object concatenateStrings(String op1, String op2) {
        return op1 + op2;
    }

    /**
     * This is not a documented feature but we are leaving this in for now. I'm
     * unsure if there is demand for this feature.
     *
     * @param op1
     * @param op2
     * @return
     */
    @Deprecated
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object addToList(List<?> op1, Object op2) {
        if (op2 instanceof Collection) {
            op1.addAll((Collection) op2);
        } else {
            ((List<Object>) op1).add(op2);
        }
        return op1;
    }

    /**
     * This is not a documented feature but we are leaving this in for now. I'm
     * unsure if there is demand for this feature.
     *
     * @param op1
     * @param op2
     * @return
     */
    @Deprecated
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object subtractFromList(List<?> op1, Object op2) {
        if (op2 instanceof Collection) {
            op1.removeAll((Collection) op2);
        } else {
            op1.remove(op2);
        }
        return op1;
    }

    private static Object wideningConversionBinaryOperation(Object op1, Object op2, Operation operation) {

        if (!(op1 instanceof Number) || !(op2 instanceof Number)) {
            throw new RuntimeException(
                    String.format("invalid operands for mathematical operation [%s]", operation.toString()));
        }

        Number num1 = (Number) op1;
        Number num2 = (Number) op2;

        if (num1 instanceof BigDecimal || num2 instanceof BigDecimal) {
            return bigDecimalOperation(BigDecimal.valueOf(num1.doubleValue()), BigDecimal.valueOf(num2.doubleValue()),
                    operation);
        }

        if (num1 instanceof Double || num2 instanceof Double) {
            return doubleOperation(num1.doubleValue(), num2.doubleValue(), operation);
        }

        if (num1 instanceof Float || num2 instanceof Float) {
            return floatOperation(num1.floatValue(), num2.floatValue(), operation);
        }

        if (num1 instanceof Long || num2 instanceof Long) {
            return longOperation(num1.longValue(), num2.longValue(), operation);
        }

        return integerOperation(num1.intValue(), num2.intValue(), operation);
    }

    private static boolean wideningConversionBinaryComparison(Object op1, Object op2, Comparison comparison) {
        if (op1 == null || op2 == null) {
            return false;
        }

        Number num1;
        Number num2;
        try {
            num1 = (Number) op1;
            num2 = (Number) op2;
        } catch (ClassCastException ex) {
            throw new RuntimeException(
                    String.format("invalid operands for mathematical comparison [%s]", comparison.toString()));
        }

        return doubleComparison(num1.doubleValue(), num2.doubleValue(), comparison);
    }

    private static double doubleOperation(double op1, double op2, Operation operation) {
        switch (operation) {
        case ADD:
            return op1 + op2;
        case SUBTRACT:
            return op1 - op2;
        case MULTIPLICATION:
            return op1 * op2;
        case DIVISION:
            return op1 / op2;
        case MODULUS:
            return op1 % op2;
        default:
            throw new RuntimeException("Bug in OperatorUtils in pebble library");
        }
    }

    private static boolean doubleComparison(double op1, double op2, Comparison comparison) {
        switch (comparison) {
        case GREATER_THAN:
            return op1 > op2;
        case GREATER_THAN_EQUALS:
            return op1 >= op2;
        case LESS_THAN:
            return op1 < op2;
        case LESS_THAN_EQUALS:
            return op1 <= op2;
        case EQUALS:
            return op1 == op2;
        default:
            throw new RuntimeException("Bug in OperatorUtils in pebble library");
        }
    }

    private static BigDecimal bigDecimalOperation(BigDecimal op1, BigDecimal op2, Operation operation) {
        switch (operation) {
        case ADD:
            return op1.add(op2);
        case SUBTRACT:
            return op1.subtract(op2);
        case MULTIPLICATION:
            return op1.multiply(op2, MathContext.DECIMAL128);
        case DIVISION:
            return op1.divide(op2, MathContext.DECIMAL128);
        case MODULUS:
            return op1.remainder(op2, MathContext.DECIMAL128);
        default:
            throw new RuntimeException("Bug in OperatorUtils in pebble library");
        }
    }

    private static Float floatOperation(Float op1, Float op2, Operation operation) {
        switch (operation) {
        case ADD:
            return op1 + op2;
        case SUBTRACT:
            return op1 - op2;
        case MULTIPLICATION:
            return op1 * op2;
        case DIVISION:
            return op1 / op2;
        case MODULUS:
            return op1 % op2;
        default:
            throw new RuntimeException("Bug in OperatorUtils in pebble library");
        }
    }

    private static long longOperation(long op1, long op2, Operation operation) {
        switch (operation) {
        case ADD:
            return op1 + op2;
        case SUBTRACT:
            return op1 - op2;
        case MULTIPLICATION:
            return op1 * op2;
        case DIVISION:
            return op1 / op2;
        case MODULUS:
            return op1 % op2;
        default:
            throw new RuntimeException("Bug in OperatorUtils in pebble library");
        }
    }

    private static long integerOperation(int op1, int op2, Operation operation) {
        switch (operation) {
        case ADD:
            return op1 + op2;
        case SUBTRACT:
            return op1 - op2;
        case MULTIPLICATION:
            return op1 * op2;
        case DIVISION:
            return op1 / op2;
        case MODULUS:
            return op1 % op2;
        default:
            throw new RuntimeException("Bug in OperatorUtils in pebble library");
        }
    }
} 
