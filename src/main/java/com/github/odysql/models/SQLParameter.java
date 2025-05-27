package com.github.odysql.models;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

/** Class for store value and type, to help create prepared statements. */
public class SQLParameter {

    private final SQLParameterType paramType;
    private final boolean isNull;

    private String strValue = "";
    private Integer integerValue = 0;
    private Long longValue = (long) 0;
    private Double doubleValue = 0.0;
    private Date dateValue = null;
    private Timestamp timestampValue = null;

    /**
     * Create a new SQL Parameter with given string value.
     * 
     * @param value value of string, can be <code>null</code>
     */
    private SQLParameter(String value) {
        this.paramType = SQLParameterType.STRING;
        this.strValue = value;
        this.isNull = (value == null);
    }

    /**
     * Create a new SQL Parameter with given integer value.
     * 
     * @param value value of integer, can be <code>null</code>
     */
    private SQLParameter(Integer value) {
        this.paramType = SQLParameterType.INTEGER;
        this.integerValue = value;
        this.isNull = (value == null);
    }

    /**
     * Create a new SQL Parameter with given long value.
     * 
     * @param value value of long, can be <code>null</code>
     */
    private SQLParameter(Long value) {
        this.paramType = SQLParameterType.LONG;
        this.longValue = value;
        this.isNull = (value == null);
    }

    /**
     * Create a new SQL Parameter with given double value.
     * 
     * @param value value of double, can be <code>null</code>
     */
    private SQLParameter(Double value) {
        this.paramType = SQLParameterType.DOUBLE;
        this.doubleValue = value;
        this.isNull = (value == null);
    }

    /**
     * Create a new SQL Parameter with given java.sql.Date value.
     * 
     * @param value value of java.sql.Date, can be <code>null</code>
     */
    private SQLParameter(Date value) {
        this.paramType = SQLParameterType.DATE;
        this.dateValue = value;
        this.isNull = (value == null);
    }

    /**
     * Create a new SQL Parameter with given java.sql.Timestamp value.
     * 
     * @param value value of timestamp, can be <code>null</code>
     */
    private SQLParameter(Timestamp value) {
        this.paramType = SQLParameterType.DATETIME;
        this.timestampValue = value;
        this.isNull = (value == null);
    }

    // ========================= Static Getter =========================

    /**
     * Create a new SQL Parameter with given integer value.
     * 
     * @param value value of integer, can be <code>null</code>
     * @return SQL Parameter of integer value
     */
    public static SQLParameter of(Integer value) {
        return new SQLParameter(value);
    }

    /**
     * Create a new SQL Parameter with given double value.
     * 
     * @param value value of double, can be <code>null</code>
     * @return SQL Parameter of long value
     */
    public static SQLParameter of(Long value) {
        return new SQLParameter(value);
    }

    /**
     * Create a new SQL Parameter with given double value.
     * 
     * @param value value of double, can be <code>null</code>
     * @return SQL Parameter of double value
     */
    public static SQLParameter of(Double value) {
        return new SQLParameter(value);
    }

    /**
     * Create a new SQL Parameter with given string value.
     * 
     * @param str value of string, can be <code>null</code>
     * @return SQL Parameter of given string value
     */
    public static SQLParameter of(String str) {
        return new SQLParameter(str);
    }

    /**
     * Create a new SQL Parameter with given java.sql.Date value.
     * 
     * @param sqlDate value of java.sql.Date, can be <code>null</code>
     * @return SQL Parameter of given java.sql.Date value
     */
    public static SQLParameter of(Date sqlDate) {
        return new SQLParameter(sqlDate);
    }

    /**
     * Create a new SQL Parameter with given java.sql.Timestamp value.
     * 
     * @param timestamp value of timestamp, can be <code>null</code>
     * @return SQL Parameter of given java.sql.Timestamp value
     */
    public static SQLParameter of(Timestamp timestamp) {
        return new SQLParameter(timestamp);
    }

    // -------------------- Getter & Setters ----------------------

    /**
     * Get parameter type of this parameter object.
     * 
     * @return parameter type, never null
     */
    public SQLParameterType getParamType() {
        return paramType;
    }

    /**
     * Check if content of this parameter object is <code>null</code>.
     * 
     * @return true if object content is <code>null</code>, false otherwise
     */
    public boolean isNull() {
        return this.isNull;
    }

    /**
     * Get string value stored inside this parameter object..
     * 
     * @return string value if any
     */
    public String getStrValue() {
        return strValue;
    }

    /**
     * Get integer value stored inside this parameter object..
     * 
     * @return integer value if any
     */
    public Integer getIntegerValue() {
        return integerValue;
    }

    /**
     * Get long value stored inside this parameter object..
     * 
     * @return long value if any
     */
    public Long getLongValue() {
        return longValue;
    }

    /**
     * Get double value stored inside this parameter object..
     * 
     * @return double value if any
     */
    public Double getDoubleValue() {
        return doubleValue;
    }

    /**
     * Get date value stored inside this parameter object..
     * 
     * @return date value if any
     */
    public Date getDateValue() {
        return dateValue;
    }

    /**
     * Get timestamp value stored inside this parameter object..
     * 
     * @return timestamp value if any
     */
    public Timestamp getTimestampValue() {
        return timestampValue;
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
    public String getValueAsSQL() {
        // Null value handling
        if (isNull) {
            return "NULL";
        }

        switch (this.paramType) {
            case DOUBLE:
                return String.valueOf(doubleValue);
            case INTEGER:
                return String.valueOf(integerValue);
            case LONG:
                return String.valueOf(longValue);

            case DATE:
                // Use single quotes for sql date format
                return "'" + dateValue.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "'";

            case DATETIME:
                // Use single quotes for sql datetime format
                return "'"
                        + timestampValue
                                .toLocalDateTime()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        + "'";

            case STRING:
                // Add single quotes as sql string format
                return "'" + this.strValue + "'";

            default:
                throw new IllegalArgumentException("Unsupported type detected");
        }
    }
}
