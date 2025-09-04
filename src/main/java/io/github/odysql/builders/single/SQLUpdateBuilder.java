package io.github.odysql.builders.single;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import io.github.odysql.models.SQLCondition;
import io.github.odysql.models.SQLParameter;

/**
 * A utility class for building SQL UPDATE statements, to improve readability
 * &amp; flexibility when construct query. Developer can add the SQL Statement
 * in order they wants, even their don't follow the order of SQL format.
 * <p>
 * Give an example of SQL statement of UPDATE: <blockquote>
 * 
 * <pre>
 * UPDATE SET col1 = 'value' FROM my_table WHERE id = 123
 * </pre>
 * 
 * </blockquote> Then we can build the SQL Statement like this: <blockquote>
 * 
 * <pre>
 * SQLUpdateBuilder builder = new SQLUpdateBuilder()
 *         .from("my_table")
 *         .update("col1", value)
 * 
 *         .where(SQLCondition.create("id = ?"))
 *         .param(SQLParameter.of(123));
 * 
 * // Convert to param SQL
 * ParamSQL paramSql = builder.toParamSQL();
 * 
 * // Debug SQL
 * logger.debug(paramSql.getDebugSQL());
 * 
 * // Build prepared statement by inject connection
 * try (PreparedStatement stmt = ps.prepare(conn)) {
 *     // .......
 * }
 * </pre>
 * 
 * </blockquote> If developer don't want to use <code>ParamSQL</code>, this
 * builder also support get SQL and its parameter by <blockquote>
 * 
 * <pre>
 * // Get Raw SQL and parameters
 * String sql = builder.toSQL();
 * List&lt;SQLParameter&gt; params = builder.getParams();
 * </pre>
 * 
 * </blockquote> Please note that SQL constructed by this builder may be fail to
 * run, as this <b>should be checked by developer themselves</b>.
 */
public class SQLUpdateBuilder implements Conditionable<SQLUpdateBuilder>, SingleSQLBuildable {
    /** Table name to be updated. */
    private String targetTable = "";

    /** Column name & value to be updated. */
    private LinkedHashMap<String, SQLParameter> updateCols = new LinkedHashMap<>();

    /** SQLCondition for update statement. */
    private SQLCondition condition = null;
    private List<SQLParameter> conditionParams = new ArrayList<>();

    /** Create a new SQL builder for UPDATE statement. */
    public SQLUpdateBuilder() {
        // Used for 1st line of chain fluent setters
    }

    /**
     * Update specified columns, which is <code>SET colName = value</code>.
     * 
     * @param colName column name to update
     * @param value   value to be update.
     * @return this
     */
    public SQLUpdateBuilder update(String colName, Integer value) {
        updateCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Update specified columns, which is <code>SET colName = value</code>.
     * 
     * @param colName column name to update
     * @param value   value to be update.
     * @return this
     */
    public SQLUpdateBuilder update(String colName, Long value) {
        updateCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Update specified columns, which is <code>SET colName = value</code>.
     * 
     * @param colName column name to update
     * @param value   value to be update.
     * @return this
     */
    public SQLUpdateBuilder update(String colName, Double value) {
        updateCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Update specified columns, which is <code>SET colName = value</code>.
     * 
     * @param colName column name to update
     * @param value   value to be update.
     * @return this
     */
    public SQLUpdateBuilder update(String colName, String value) {
        updateCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Update specified columns, which is <code>SET colName = value</code>.
     * 
     * @param colName column name to update
     * @param value   value to be update.
     * @return this
     */
    public SQLUpdateBuilder update(String colName, Date value) {
        updateCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Update specified columns, which is <code>SET colName = value</code>.This
     * method will convert <code>java.time.LocalDate</code> to
     * <code>java.sql.Date</code> automatically.
     * 
     * @param colName column name to update
     * @param value   value to be update.
     * @return this
     */
    public SQLUpdateBuilder update(String colName, LocalDate value) {
        updateCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Update specified columns, which is <code>SET colName = value</code>.
     * 
     * @param colName column name to update
     * @param value   value to be update.
     * @return this
     */
    public SQLUpdateBuilder update(String colName, Timestamp value) {
        updateCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Update specified columns, which is <code>SET colName = value</code>. This
     * method will convert <code>java.time.LocalDateTime</code> to
     * <code>java.sql.Timestamp</code> automatically.
     * 
     * @param colName column name to update
     * @param value   value to be update.
     * @return this
     */
    public SQLUpdateBuilder update(String colName, LocalDateTime value) {
        updateCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Set the table Source, same as <code>FROM</code> of SQL syntax.
     * 
     * @param tableName table name
     * @return this
     */
    public SQLUpdateBuilder from(String tableName) {
        this.targetTable = tableName;
        return this;
    }

    // ==================== Common method for SQLConditionable =================

    @Override
    public SQLUpdateBuilder where(SQLCondition cond) {
        this.condition = cond;
        return this;
    }

    @Override
    public SQLUpdateBuilder param(SQLParameter... arguments) {
        for (SQLParameter item : arguments) {
            this.conditionParams.add(item);
        }

        return this;
    }

    // ==================== Common method for DML =================

    /**
     * Check if builder is valid, i.e. target table, update column and condition
     * cannot be empty.
     * 
     * @return true if this builder result is valid, false otherwise
     */
    private boolean checkIfValid() {
        return !targetTable.isEmpty() && !updateCols.isEmpty() && !SQLCondition.isEmpty(condition);
    }

    @Override
    public List<SQLParameter> getParams() {
        List<SQLParameter> paramList = new ArrayList<>(updateCols.values());
        paramList.addAll(conditionParams);

        return paramList;
    }

    /**
     * Construct SQL string from builder.
     * 
     * @return constructed SQL, which is parameterized.
     * @throws IllegalStateException when this builder cannot generate correct SQL
     */
    private String constructSQL() throws IllegalStateException {
        if (!checkIfValid()) {
            throw new IllegalStateException("SQL builder is invalid");
        }

        List<String> cols = this.updateCols
                .keySet()
                .stream()
                .map(key -> key + "=?")
                .collect(Collectors.toList());

        return new StringBuilder()
                .append("UPDATE ")
                .append(this.targetTable)
                .append(" SET ")
                .append(String.join(",", cols))
                .append(" WHERE ")
                .append(this.condition.asSQL())
                .toString();
    }

    @Override
    public ParamSQL toParamSQL() {
        String sql = constructSQL();
        return new ParamSQL(sql, this.getParams());
    }

    @Override
    public String toSQL() {
        return constructSQL();
    }
}
