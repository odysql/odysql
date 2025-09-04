package io.github.odysql.internal.helpers;

import java.util.Collection;

/**
 * The class that contains multiple static method, to prevent appear null
 * pointer exception.
 */
public class SQLNonNullUtils {
    /** Private constructor to hide public one. */
    private SQLNonNullUtils() {
    }

    /**
     * Check if given string is empty or null.
     * 
     * @param str string to be checked
     * @return true if trimmed string is empty, or string is already
     *         <code>null</code>; false otherwise
     */
    public static boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }

        return str.trim().isEmpty();
    }

    /**
     * Check if given collection is empty or null.
     * 
     * @param c collection to be checked
     * @return true if collection is empty or <code>null</code>; false otherwise
     */
    public static boolean isEmpty(Collection<?> c) {
        if (c == null) {
            return true;
        }

        return c.isEmpty();
    }

    /**
     * Prevent the string value to be null and cause null pointer exception. Also it
     * will remove the leading and trailing space of string.
     * <p>
     * It is commonly used like <code>rs.getString("Key")</code>, as It may return
     * an empty value.
     *
     * @param str string that have possibility become null
     * @return empty string if str is null, otherwise return result of
     *         <code>str.trim()</code>
     */
    public static String safeStr(String str) {
        if (str != null) {
            return str.trim();
        }

        return "";
    }
}
