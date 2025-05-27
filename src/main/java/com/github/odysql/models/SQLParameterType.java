package com.github.odysql.models;

import java.sql.Types;

/** Parameter type for <code>SQLParameter</code>. */
public enum SQLParameterType {
    /** Integer value. */
    INTEGER(Types.INTEGER, false),
    /** Longer integer value, equals to <code>BIGINT</code> */
    LONG(Types.BIGINT, false),

    /** Double value. */
    DOUBLE(Types.DOUBLE, false),

    /** Common string, using <code>VARCHAR</code>. */
    STRING(Types.VARCHAR, true),

    /** Date (not include time). */
    DATE(Types.DATE, true),
    /** Datetime, or timestamp. */
    DATETIME(Types.TIMESTAMP, true);

    private final int sqlType;
    private final boolean isNonPrimitive;

    private SQLParameterType(int sqlType, boolean isNonPrimitive) {
        this.sqlType = sqlType;
        this.isNonPrimitive = isNonPrimitive;
    }

    /**
     * Get <code>java.sql.Types</code> for Prepared statement.
     * 
     * @return related sql types
     */
    public int getSqlType() {
        return sqlType;
    }

    /**
     * Determine parameter that is considered as primitive type or not by sql
     * Prepared statement, i.e. if Prepared statement setter allow null values.
     * 
     * @return true if type is Non-Primitive Type, i.e. null values are allowed (),
     *         e.g. <code>setString()</code>; Otherwise false, e.g.
     *         <code>setInt()</code>
     */
    public boolean isNonPrimitive() {
        return isNonPrimitive;
    }
}
