package io.github.odysql.models;

import java.util.ArrayList;
import java.util.List;

import io.github.odysql.internal.helpers.SQLNonNullUtils;

/**
 * The class for holding condition of SQL query, which used in WHERE or ON
 * clause.
 * <p>
 * A few example for this condition is
 * 
 * <pre>
 * // column1 = value1
 * Condition.eqTo("column1", "'value1'");
 * // column1 = value1 OR column2 IS NULL
 * Condition
 *      .eqTo("column1", "'value1'")
 *      .or(Condition.isNull("column2"));
 * // (column1 = value1 OR column2 IS NULL) AND (column3 = column4)
 * Condition
 *      .bracket(Condition
 *          .create("column1 = 'value1'")
 *          .or(Condition.isNull("column2")))
 *      .and(Condition.bracket(
 *              Condition.create("column3=column4")))
 * </pre>
 */
public class SQLCondition implements SQLFragment {
    /** Empty string, indicate actually value of condition is empty. */
    private static final String EMPTY_STRING = "";

    private String sql = EMPTY_STRING;

    /**
     * Create a new condition object with string.
     * 
     * @param condition the string to hold the condition, e.g. "column1 = column2"
     */
    private SQLCondition(String condition) {
        this.sql = condition;
    }

    // ======= Suggested Condition constructor ========

    /**
     * Create a empty condition. This condition will be ignore when using
     * {@link #and(SQLCondition)} or {@link #or(SQLCondition)}. Please note that
     * depends on builder implementation, this condition may not be consider as
     * valid condition.
     * <p>
     * This constructor is designed to utilize needs to decide condition by some
     * flags, e.g.
     * 
     * <pre>
     * Condition cond = containsAll ? Condition.empty() : Condition.isNull("expiry_date")
     * </pre>
     * 
     * @return condition that is actually empty.
     */
    public static SQLCondition empty() {
        return new SQLCondition(EMPTY_STRING);
    }

    /**
     * Create a new condition object with string. You MAY use this method if the
     * existing constructor is not fits developer requirement.
     * 
     * @param condition the string to hold the condition, e.g. "column1 = column2"
     * @return new condition object with string given
     */
    public static SQLCondition create(String condition) {
        return new SQLCondition(condition);
    }

    /**
     * Create a condition object with '=' operator, e.g. "column1 = column2".
     * <p>
     * As opinionated reason, this function forbidden usage like
     * <code>eqTo("col1", "?")</code>, as it may lead to possible unclear code.
     * Developer are suggest to use <code>create("col1 = ?")</code> instead.
     * <p>
     * Developer SHOULD only use this function when there has a necessary need, e.g
     * 
     * <pre>
     * Condition.eqTo(
     *         "concat(col1, col2, col3, col4, col5)",
     *         "trim(concat(another1, another2))");
     * </pre>
     * 
     * @param value1 the first value to be compared
     * @param value2 the second value to be compared
     * @return this
     * @throws IllegalArgumentException when value1 or value2 is '?'
     */
    public static SQLCondition eqTo(String value1, String value2) throws IllegalArgumentException {
        if ("?".equals(value1) || "?".equals(value2)) {
            throw new IllegalArgumentException(
                    "Condition created by eqTo should use '?'. Use Condition.create() instead.");
        }

        return new SQLCondition(value1 + " = " + value2);
    }

    /**
     * Create a condition object with <code>IS NULL</code> statement.
     * 
     * @param columnName the column name
     * @return this
     */
    public static SQLCondition isNull(String columnName) {
        return new SQLCondition(columnName + " IS NULL ");
    }

    /**
     * Create a condition object with <code>IS NOT NULL</code> statement.
     * 
     * @param columnName the column name
     * @return this
     */
    public static SQLCondition isNotNull(String columnName) {
        return new SQLCondition(columnName + " IS NOT NULL ");
    }

    /**
     * Create a condition object with <code>IN (.., ..)</code> statement. It is
     * highly recommended to use this to replace unnecessary OR statement.
     * <p>
     * Please note that this function is NOT designed for prepared statement
     * placeholder <code>?</code>, all string will be quoted with <code>'</code>.
     * 
     * @param <T>        <code>Integer</code> or <code>String</code>
     * @param columnName the column name
     * @param values     list of value inside IN statement
     * @throws IllegalArgumentException when type is not integer or string, or list
     *                                  is empty
     * @return this
     */
    public static <T> SQLCondition in(String columnName, List<T> values) throws IllegalArgumentException {
        if (SQLNonNullUtils.isEmpty(values)) {
            throw new IllegalArgumentException("list cannot be empty.");
        }

        // Pre-process the values to quoted one
        List<String> quoted = new ArrayList<>();

        for (T val : values) {
            if (val instanceof Integer) {
                quoted.add(String.valueOf(val));
                continue;
            }

            if (val instanceof String) {
                quoted.add("'" + val + "'");
                continue;
            }

            throw new IllegalArgumentException("Unsupported type for Condition.in().");
        }

        return new SQLCondition(String.format("%s IN (%s)", columnName, String.join(",", quoted)));
    }

    /**
     * Create a condition object with <code>NOT IN (.., ..)</code> statement. It is
     * highly recommended to use this to replace unnecessary OR statement.
     * <p>
     * Please note that this function is NOT designed for prepared statement
     * placeholder <code>?</code>, all string will be quoted with <code>'</code>.
     * 
     * @param <T>        <code>Integer</code> or <code>String</code>
     * @param columnName the column name
     * @param values     list of value inside NOT IN statement
     * @return this
     * @throws IllegalArgumentException when type is not integer or string, or list
     *                                  is empty
     */
    public static <T> SQLCondition notIn(String columnName, List<T> values) throws IllegalArgumentException {
        if (SQLNonNullUtils.isEmpty(values)) {
            throw new IllegalArgumentException("list cannot be empty.");
        }

        // Pre-process the values to quoted one
        List<String> quoted = new ArrayList<>();

        for (T val : values) {
            if (val instanceof Integer) {
                quoted.add(String.valueOf(val));
                continue;
            }

            if (val instanceof String) {
                quoted.add("'" + val + "'");
                continue;
            }

            throw new IllegalArgumentException("Unsupported type for Condition.notIn().");
        }

        return new SQLCondition(String.format("%s NOT IN (%s)", columnName, String.join(",", quoted)));
    }

    /**
     * Create a bracket statement for given condition.
     * 
     * @param condition the condition to be bracketed
     * @return condition that is bracketed, e.g. <code>(column1 = column2)</code>
     */
    public static SQLCondition bracket(SQLCondition condition) {
        return new SQLCondition("(" + condition.sql + ")");
    }

    /**
     * Pick suitable condition based on the boolean flag value. This function can
     * improve readability and maintain fluent setter when condition is needed to
     * change dynamically.
     * 
     * @param flag    flag to decide which condition to be used. Can be
     *                <code>null</code>.
     * @param ifTrue  condition to be return if flag is true
     * @param ifFalse condition to be return if flag is false
     * @return return condition depends on flag is true/false; or a empty condition
     *         if flag is <code>null</code>.
     */
    public static SQLCondition pickByFlag(Boolean flag, SQLCondition ifTrue, SQLCondition ifFalse) {
        if (flag == null) {
            return SQLCondition.empty();
        }

        return flag ? ifTrue : ifFalse;
    }

    // ============== Condition Operation =================

    @Override
    public int hashCode() {
        final int PRIME = 31;
        return PRIME * 1 + ((sql == null) ? 0 : sql.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        SQLCondition other = (SQLCondition) obj;
        if (sql == null) {
            if (other.sql != null) {
                return false;
            }
        } else if (!sql.equals(other.sql)) {
            return false;
        }

        return true;
    }

    /**
     * Perform AND operation to condition.
     * 
     * @param condition another condition
     * @return this
     */
    public SQLCondition and(String condition) {
        return and(SQLCondition.create(condition));
    }

    /**
     * Perform AND operation to condition.
     * 
     * @param condition another condition
     * @return this
     */
    public SQLCondition and(SQLCondition condition) {
        // If param condition is empty, ignore it
        if (SQLCondition.isEmpty(condition)) {
            return this;
        }

        // When both condition is not empty, normal concat
        if (!SQLCondition.isEmpty(this)) {
            this.sql += " AND " + condition.asSQL();
            return this;
        }

        // When this is empty and parameter is not, then replace
        this.sql = condition.sql;
        return this;
    }

    /**
     * Perform AND operation to condition with bracket surround. This is short hand
     * for <code>SQLCondition.and(SQLCondition.bracket(condition))</code>.
     * 
     * @param condition another condition
     * @return this
     */
    public SQLCondition andBracket(SQLCondition condition) {
        return and(SQLCondition.bracket(condition));
    }

    /**
     * Perform OR operation to condition.
     * 
     * @param condition another condition
     * @return this
     */
    public SQLCondition or(String condition) {
        return or(SQLCondition.create(condition));
    }

    /**
     * Perform OR operation to condition.
     * 
     * @param condition another condition
     * @return this
     */
    public SQLCondition or(SQLCondition condition) {
        boolean isThisEmpty = SQLCondition.isEmpty(this);
        boolean isParamEmpty = SQLCondition.isEmpty(condition);

        // If param condition is empty, ignore it
        if (isParamEmpty) {
            return this;
        }

        if (!isThisEmpty) {
            // When both condition is not empty, normal concat
            this.sql += " OR " + condition.asSQL();
            return this;

        } else {
            // When this is empty and parameter is not, then replace
            this.sql = condition.sql;
            return this;
        }
    }

    /**
     * Perform OR operation to condition with bracket surround. This is short hand
     * for <code>SQLCondition.or(SQLCondition.bracket(condition))</code>.
     * 
     * @param condition another condition
     * @return this
     */
    public SQLCondition orBracket(SQLCondition condition) {
        return or(SQLCondition.bracket(condition));
    }

    // ============== Final SQL Part =================

    @Override
    public String asSQL() {
        return this.sql;
    }

    /**
     * Check if given Condition is empty. <code>null</code> will be also consider as
     * empty.
     * <p>
     * This function design prevent any <code>null</code> related exception by
     * inject. Please note that actual implementation of empty condition is include
     * a empty string inside, but not a <code>null</code> value.
     * 
     * @param cond condition to check
     * @return true if condition is actually empty or <code>null</code>.
     */
    public static final boolean isEmpty(SQLCondition cond) {
        return cond == null || SQLNonNullUtils.isEmpty(cond.sql);
    }
}
