package io.github.odysql.helpers;

import static java.sql.Types.*;

import java.sql.Date;
import java.sql.Timestamp;

/** Mapper for SQL types and java classes. */
public class SQLTypesMapper {
    private SQLTypesMapper() {
    }

    /**
     * Check given class is primitive type or not, which java.sql not provide setter
     * method in statements.
     * 
     * @param clazz class to check
     * @return true if it is primitive (e.g. integer, double); false otherwise (e.g.
     *         string)
     * @throws IllegalArgumentException unsupported class detected
     */
    public static boolean isPrimitive(Class<?> clazz) throws IllegalArgumentException {
        if (clazz == Integer.class || clazz == Long.class || clazz == Double.class) {
            return true;
        }

        if (clazz == String.class || clazz == Date.class || clazz == Timestamp.class) {
            return false;
        }

        throw new IllegalArgumentException("Unsupported class");
    }

    /**
     * Convert given java class to {@link java.sql.Types}.
     * 
     * @param clazz class to convert
     * @return sql type for given java class
     * @throws IllegalArgumentException unsupported class detected
     */
    public static int toSQLTypes(Class<?> clazz) {
        if (clazz == Integer.class) {
            return INTEGER;
        }

        if (clazz == Long.class) {
            return BIGINT;
        }

        if (clazz == Double.class) {
            return DOUBLE;
        }

        if (clazz == String.class) {
            return VARCHAR;
        }

        if (clazz == Date.class) {
            return DATE;
        }

        if (clazz == Timestamp.class) {
            return TIMESTAMP;
        }

        throw new IllegalArgumentException("Unsupported class");
    }
}
