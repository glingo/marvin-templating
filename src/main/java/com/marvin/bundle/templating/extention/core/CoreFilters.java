package com.marvin.bundle.templating.extention.core;

import com.marvin.bundle.templating.extention.Filter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface CoreFilters {
    
    static Filter capitalize() {
        return Filter.builder()
                .fromFunction(ObjectUtils::capitalize)
                .build();
    }
    
    static Filter upper() {
        return Filter.builder()
                .fromFunction(ObjectUtils::toUpperCase)
                .build();
    }
    
    static Filter lower() {
        return Filter.builder()
                .fromFunction(ObjectUtils::toLowerCase)
                .build();
    }
    
    static Filter defaults() {
        return Filter.builder()
                .fromBiFunction(ObjectUtils::orDefault)
                .withArg("default", null)
                .build();
    }
    
    static Filter abbreviate() {
        return Filter.builder()
                .fromBiFunction(ObjectUtils::abbreviate)
                .withArg("l", 10)
                .build();
    }
    
    static Filter abs() {
        return Filter.builder()
                .fromFunction(ObjectUtils::abs)
                .withArg("a", 0d)
                .build();
    }
    
    static Filter date() {
        return Filter.builder()
                .withArg("format")
                .withArg("existingFormat")
                .withArg("locale", Locale.getDefault())
                .withBase((input, args) -> {
                    String toFormat = (String) args.get("format");
                    String fromFormat = (String) args.get("existingFormat");
                    Locale locale = (Locale) args.get("locale");
                    
                    return ObjectUtils.formatDate(input, toFormat, fromFormat, locale);
                }).build();
    }
    
    static Filter numberFormat() {
        return Filter.builder()
                .withArg("format")
                .withArg("locale", Locale.getDefault())
                .withBase((input, args) -> {
                    String toFormat = (String) args.get("format");
                    Locale locale = (Locale) args.get("locale");
                    
                    return ObjectUtils.formatNumber(input, toFormat, locale);
                }).build();
    }
    
    static Filter first() {
        return Filter.builder()
                .fromFunction(ObjectUtils::first)
                .build();
    }
    
    static Filter last() {
        return Filter.builder()
                .fromFunction(ObjectUtils::last)
                .build();
    }
    
    static Filter join() {
        return Filter.builder()
                .fromBiFunction(ObjectUtils::join)
                .build();
    }
    
    static Filter slice() {
        return Filter.builder()
                .withArg("fromIndex", 0L)
                .withArg("toIndex")
                .withBase((input, args) -> {
                    Object fromIndex = args.get("fromIndex");
                    Object toIndex = args.get("toIndex");
                    
                    return ObjectUtils.slice(input, fromIndex, toIndex);
                }).build();
    }
    
    static Filter sort() {
        return Filter.builder()
                .fromFunction((input) -> {
                    if (input == null) {
                        return null;
                    }
                    List<Comparable> collection = (List<Comparable>) input;
                    Collections.sort(collection);
                    return collection;
                }).build();
    }
    
    static Filter rsort() {
        return Filter.builder()
                .fromFunction((input) -> {
                    if (input == null) {
                        return null;
                    }
                    List<Comparable> collection = (List<Comparable>) input;
                    Collections.sort(collection, Collections.reverseOrder());
                    return collection;
                }).build();
    }
    
    static Filter title() {
        return Filter.builder()
                .fromFunction((input) -> {
                    if (input == null) {
                        return null;
                    }
                    String value = (String) input;
                    if (value.length() == 0) {
                        return value;
                    }

                    StringBuilder result = new StringBuilder();

                    boolean capitalizeNextCharacter = true;

                    for (char c : value.toCharArray()) {
                        if (Character.isWhitespace(c)) {
                            capitalizeNextCharacter = true;
                        } else if (capitalizeNextCharacter) {
                            c = Character.toTitleCase(c);
                            capitalizeNextCharacter = false;
                        }
                        result.append(c);
                    }

                    return result.toString();
                }).build();
    }
    
    static Filter trim() {
        return Filter.builder()
                .fromFunction((input) -> {
                    if (input == null) {
                        return null;
                    }
                    String str = (String) input;
                    return str.trim();
                }).build();
    }
    
    static Filter urlEncode() {
        return Filter.builder()
                .fromFunction((input) -> {
                    if (input == null) {
                        return null;
                    }
                    String arg = (String) input;
                    try {
                        arg = URLEncoder.encode(arg, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                    }
                    return arg;
                }).build();
    }
    
    static Filter length() {
        return Filter.builder()
                .fromFunction((input) -> {
                    if (input == null) {
                        return 0;
                    }
                    if (input instanceof String) {
                        return ((String) input).length();
                    }
                    if (input instanceof Collection) {
                        return ((Collection<?>) input).size();
                    }
                    if (input.getClass().isArray()) {
                        return Array.getLength(input);
                    }
                    if (input instanceof Map) {
                        return ((Map<?, ?>) input).size();
                    }
                    if (input instanceof Iterable) {
                        Iterator<?> it = ((Iterable<?>) input).iterator();
                        int size = 0;
                        while (it.hasNext()) {
                            it.next();
                            size++;
                        }
                        return size;
                    }
                    if (input instanceof Iterator) {
                        Iterator<?> it = (Iterator<?>) input;
                        int size = 0;
                        while (it.hasNext()) {
                            it.next();
                            size++;
                        }
                        return size;
                    }
                    
                    return 0;
                }).build();
    }
    
    static Filter replace() {
        return Filter.builder()
                .fromBiFunction((input, pair) -> {
                    String data = input.toString();
                    if (pair == null) {
                        throw new IllegalArgumentException(MessageFormat.format("The argument ''{0}'' is required.", "pair"));
                    }
                    
                    Map<?, ?> map = (Map) pair;
                    
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                       data = data.replace(entry.getKey().toString(), entry.getValue().toString());
                    }

                    return data;
                }).build();
    }
    
    static Filter merge() {
        return Filter.builder()
                .fromBiFunction((input, items) -> {
                    if (input == null && items == null) {
                        throw new IllegalArgumentException("The two arguments to be merged are null");
                    }
                    
                    if (input != null && items == null) {
                        return input;
                    }
                    
                    if (items != null && input == null) {
                        return items;
                    }
                    // left hand side argument defines resulting type
                    if (input instanceof Map) {
                        return ObjectUtils.mergeAsMap((Map<?, ?>) input, items);
                    }
                    
                    if (input instanceof List) {
                        return ObjectUtils.mergeAsList((List<?>) input, items);
                    }
                    
                    if (input != null && input.getClass().isArray()) {
                        return ObjectUtils.mergeAsArray(input, items);
                    }
                    
                    throw new IllegalArgumentException("The object being filtered is not a Map/List/Array");
                }).build();
    }
}
