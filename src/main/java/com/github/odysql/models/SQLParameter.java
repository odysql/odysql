package com.github.odysql.models;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.github.odysql.helpers.SQLTypesMapper;

/** Class for store value and type, to help create prepared statements. */
public class SQLParameter {

    private final Class<?> clazz;
    private final Object value;

    /**
     * Create a new SQL Parameter with given class and value.
     * 
     * @param <ValueT> type of value
     * @param clazz    class of given value
     * @param value    value of string, can be <code>null</code>
     */
    private <ValueT> SQLParameter(Class<ValueT> clazz, ValueT value) {
        this.clazz = clazz;
        this.value = value;
    }

    // ========================= Static Getter =========================

    /**
     * Create a new SQL Parameter with given integer value.
     * 
     * @param value value of integer, can be <code>null</code>
     * @return SQL Parameter of integer value
     */
    public static SQLParameter of(Integer value) {
        return new SQLParameter(Integer.class, value);
    }

    /**
     * Create a new SQL Parameter with given long value.
     * 
     * @param value value of long, can be <code>null</code>
     * @return SQL Parameter of long value
     */
    public static SQLParameter of(Long value) {
        return new SQLParameter(Long.class, value);
    }

    /**
     * Create a new SQL Parameter with given double value.
     * 
     * @param value value of double, can be <code>null</code>
     * @return SQL Parameter of double value
     */
    public static SQLParameter of(Double value) {
        return new SQLParameter(Double.class, value);
    }

    /**
     * Create a new SQL Parameter with given string value.
     * 
     * @param str value of string, can be <code>null</code>
     * @return SQL Parameter of given string value
     */
    public static SQLParameter of(String str) {
        return new SQLParameter(String.class, str);
    }

    /**
     * Create a new SQL Parameter with given java.sql.Date value.
     * 
     * @param sqlDate value of java.sql.Date, can be <code>null</code>
     * @return SQL Parameter of given java.sql.Date value
     */
    public static SQLParameter of(Date sqlDate) {
        return new SQLParameter(Date.class, sqlDate);
    }

    /**
     * Create a new SQL Parameter with given <code>LocalDate</code>, which will
     * convert to <code>java.sql.Date</code> automatically.
     * 
     * @param date value of local date, can be <code>null</code>.
     * @return SQL Parameter of given java.sql.Date value
     */
    public static SQLParameter of(LocalDate date) {
        if (date == null) {
            return new SQLParameter(Date.class, null);
        }

        return new SQLParameter(Date.class, Date.valueOf(date));
    }

    /**
     * Create a new SQL Parameter with given java.sql.Timestamp value.
     * 
     * @param timestamp value of timestamp, can be <code>null</code>
     * @return SQL Parameter of given java.sql.Timestamp value
     */
    public static SQLParameter of(Timestamp timestamp) {
        return new SQLParameter(Timestamp.class, timestamp);
    }

    /**
     * Create a new SQL Parameter with given <code>LocalDateTime</code>, which will
     * convert to <code>java.sql.Timestamp</code> automatically.
     * 
     * @param dt local datetime value, can be <code>null</code>
     * @return SQL Parameter of given java.sql.Timestamp value
     */
    public static SQLParameter of(LocalDateTime dt) {
        if (dt == null) {
            return new SQLParameter(Timestamp.class, null);
        }

        return new SQLParameter(Timestamp.class, Timestamp.valueOf(dt));
    }

    /**
     * Check if content of this parameter object is <code>null</code>.
     * 
     * @return true if object content is <code>null</code>, false otherwise
     */
    public boolean isNull() {
        return value == null;
    }

    /**
     * Get value stored in this container. Please note this not ensure type safety.
     * 
     * @return value stored in this parameter
     */
    public Object getValue() {
        return value;
    }

    // ------------------ Special SQL Getters ------------------------------

    /**
     * Get SQLParameter actual value as SQL string. If actual value is null, then
     * return "NULL" instead.
     * <p>
     * Please note that all SQL <code>DATETIME</code> will use
     * <code>yyyy-MM-dd HH:mm:ss</code> as date time format, while <code>DATE</code>
     * use <code>yyyy-MM-dd</code> as date format.
     * 
     * @return string representation of actual value, in SQL format
     */
    public String toDebugSQL() {
        // Null value handling
        if (this.isNull()) {
            return "NULL";
        }

        if (this.clazz == Double.class) {
            return String.valueOf(this.value);
        }

        if (this.clazz == Integer.class) {
            return String.valueOf(this.value);
        }

        if (this.clazz == Long.class) {
            return String.valueOf(this.value);
        }

        if (this.clazz == Date.class) {
            Date dateValue = (Date) this.value;

            // Use single quotes for sql date format
            return "'" + dateValue.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "'";
        }

        if (this.clazz == Timestamp.class) {
            Timestamp timestampValue = (Timestamp) this.value;

            // Use single quotes for sql datetime format
            return "'"
                    + timestampValue
                            .toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    + "'";
        }

        if (this.clazz == String.class) {
            // Add single quotes as sql string format
            return "'" + this.value + "'";
        }

        throw new IllegalArgumentException("Unsupported type detected");
    }

    /**
     * Apply current SQL parameter value to given prepared statement.
     * 
     * @param statement prepared statement to be fill
     * @param index     index for current parameter, start from 1
     * @return filled prepared statement
     * @throws SQLException             when failed to set data
     * @throws IllegalArgumentException when statement is null, or index less or
     *                                  equal to zero
     */
    public PreparedStatement apply(PreparedStatement statement, int index)
            throws SQLException, IllegalArgumentException {

        // Prevent statement & index invalid
        if (statement == null) {
            throw new IllegalArgumentException("Statement cannot be null");
        }

        if (index <= 0) {
            throw new IllegalArgumentException("Invalid index: " + index);
        }

        // -----------------------------------------------------

        // Primitive type with null value that PreparedStatement not supported setter
        if (this.isNull() && SQLTypesMapper.isPrimitive(this.clazz)) {
            statement.setNull(index, SQLTypesMapper.toSQLTypes(this.clazz));
            return statement;
        }

        // Handle parameter with designed type
        if (this.clazz == Double.class) {
            statement.setDouble(index, (double) this.value);
            return statement;
        }

        if (this.clazz == Integer.class) {
            statement.setInt(index, (int) this.value);
            return statement;
        }

        if (this.clazz == Long.class) {
            statement.setLong(index, (long) this.value);
            return statement;
        }

        if (this.clazz == Date.class) {
            statement.setDate(index, (Date) this.value);
            return statement;
        }

        if (this.clazz == Timestamp.class) {
            statement.setTimestamp(index, (Timestamp) this.value);
            return statement;

        }

        if (this.clazz == String.class) {
            statement.setString(index, (String) this.value);
            return statement;
        }

        throw new IllegalStateException("Cannot apply sql parameter with type " + this.clazz + " to statement.");
    }
}
