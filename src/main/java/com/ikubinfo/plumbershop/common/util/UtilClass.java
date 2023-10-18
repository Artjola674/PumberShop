package com.ikubinfo.plumbershop.common.util;

import static com.ikubinfo.plumbershop.common.constants.Constants.ID;
import static java.util.Arrays.stream;

public class UtilClass {

    public static <T> String getSortField(Class<T> tClass, String field) {
        return fieldExistsInClass(tClass,field) ? field : ID;
    }

    private static <T> boolean fieldExistsInClass(Class<T> tClass, String field) {
        return stream(tClass.getDeclaredFields())
                .anyMatch(f -> f.getName().equals(field));
    }

    private UtilClass() {
    }
}
