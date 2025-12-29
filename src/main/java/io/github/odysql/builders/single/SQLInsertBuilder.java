package io.github.odysql.builders.single;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.github.odysql.models.SQLParameter;

/**
 * A builder to create SQL INSERT statements, to improve readability and
 * flexibility when construct query. Developer can add the SQL Statement in
 * order they wants, even their don't follow the order of SQL format.
 * <p>
 * Give an example of SQL statement of INSERT: <blockquote> INSERT INTO
 * {tableSource} (col1, col2, col3) VALUES (val1, val2, val3) </blockquote> Then
 * we can build the SQL Statement like this: <blockquote>
 * 
 * <pre>
 * SQLInsertBuilder builder = new SQLInsertBuilder()
 *         .from(tableSource)
 *         .insert("col1", val1)
 *         .insert("col2", val2)
 *         .insert("col3", val3);
 * ParamSQL ps = builder.toParamSQL();
 * 
 * // Debug sql
 * logger.debug(ps.getDebugSQL());
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
public class SQLInsertBuilder implements SingleSQLBuildable {

    /** Table name to insert records. */
    private String targetTable = "";

    /** Use syntax INSERT IGNORE. Only support MariaDB/MySQL. */
    private boolean isInsertIgnore = false;

    /**
     * Column name & value to be update when duplicate key, i.e.
     * {@code ON DUPLICATE KEY UPDATE} . Only support MariaDB/MySQL.
     */
    private LinkedHashMap<String, SQLParameter> duplicateKeyUpdateCols = new LinkedHashMap<>();

    /** Column name & value to be inserted. */
    private LinkedHashMap<String, SQLParameter> insertCols = new LinkedHashMap<>();

    /** Create a new SQL builder for INSERT statement. */
    public SQLInsertBuilder() {
        // This method is used for 1st line of fluent setters
    }

    /**
     * Insert specified columns, which is
     * <code>INSERT INTO (colName) VALUES (value)</code>.
     * 
     * @param colName column name to insert
     * @param value   value to be insert.
     * @return this
     */
    public SQLInsertBuilder insert(String colName, Integer value) {
        insertCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Insert specified columns, which is
     * <code>INSERT INTO (colName) VALUES (value)</code>.
     * 
     * @param colName column name to insert
     * @param value   value to be insert.
     * @return this
     */
    public SQLInsertBuilder insert(String colName, Double value) {
        insertCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Insert specified columns, which is
     * <code>INSERT INTO (colName) VALUES (value)</code>.
     * 
     * @param colName column name to insert
     * @param value   value to be insert.
     * @return this
     */
    public SQLInsertBuilder insert(String colName, Long value) {
        insertCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Insert specified columns, which is
     * <code>INSERT INTO (colName) VALUES (value)</code>.
     * 
     * @param colName column name to insert
     * @param value   value to be insert.
     * @return this
     */
    public SQLInsertBuilder insert(String colName, String value) {
        insertCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Insert specified columns, which is
     * <code>INSERT INTO (colName) VALUES (value)</code>.
     * 
     * @param colName column name to insert
     * @param value   value to be insert.
     * @return this
     */
    public SQLInsertBuilder insert(String colName, Date value) {
        insertCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Insert specified columns, which is
     * <code>INSERT INTO (colName) VALUES (value)</code>. This method will convert
     * <code>java.time.LocalDate</code> to <code>java.sql.Date</code> automatically.
     * 
     * @param colName column name to insert
     * @param value   value to be insert.
     * @return this
     */
    public SQLInsertBuilder insert(String colName, LocalDate value) {
        insertCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Insert specified columns, which is
     * <code>INSERT INTO (colName) VALUES (value)</code>.
     * 
     * @param colName column name to insert
     * @param value   value to be insert.
     * @return this
     */
    public SQLInsertBuilder insert(String colName, Timestamp value) {
        insertCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Insert specified columns, which is
     * <code>INSERT INTO (colName) VALUES (value)</code>. This method will convert
     * <code>java.time.LocalDateTime</code> to <code>java.sql.TimeStamp</code>
     * automatically.
     * 
     * @param colName column name to insert
     * @param value   value to be insert.
     * @return this
     */
    public SQLInsertBuilder insert(String colName, LocalDateTime value) {
        insertCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Insert specified columns, which is
     * <code>INSERT INTO (colName) VALUES (value)</code>.
     * 
     * @param colName column name to insert
     * @param value   value to be insert.
     * @return this
     */
    public SQLInsertBuilder insert(String colName, BigDecimal value) {
        insertCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Use <code>INSERT IGNORE</code> syntax instead of <code>INSERT</code> syntax.
     * Only works with MariaDB.
     * 
     * @return this
     */
    public SQLInsertBuilder insertIgnore() {
        this.isInsertIgnore = true;
        return this;
    }

    /**
     * Set the table name to be inserted.
     * 
     * @param tableName table name
     * @return this
     */
    public SQLInsertBuilder into(String tableName) {
        this.targetTable = tableName;
        return this;
    }

    /**
     * Add a column that will be update when insert not able to run as duplicate
     * key. Only work in MariaDB or MySQL.
     * 
     * @param colName column name to be update
     * @param value   value to be update
     * @return this
     */
    public SQLInsertBuilder onDuplicateKeyUpdate(String colName, Integer value) {
        this.duplicateKeyUpdateCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Add a column that will be update when insert not able to run as duplicate
     * key. Only work in MariaDB or MySQL.
     * 
     * @param colName column name to be update
     * @param value   value to be update
     * @return this
     */
    public SQLInsertBuilder onDuplicateKeyUpdate(String colName, Double value) {
        this.duplicateKeyUpdateCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Add a column that will be update when insert not able to run as duplicate
     * key. Only work in MariaDB or MySQL.
     * 
     * @param colName column name to be update
     * @param value   value to be update
     * @return this
     */
    public SQLInsertBuilder onDuplicateKeyUpdate(String colName, Long value) {
        this.duplicateKeyUpdateCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Add a column that will be update when insert not able to run as duplicate
     * key. Only work in MariaDB or MySQL.
     * 
     * @param colName column name to be update
     * @param value   value to be update
     * @return this
     */
    public SQLInsertBuilder onDuplicateKeyUpdate(String colName, String value) {
        this.duplicateKeyUpdateCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Add a column that will be update when insert not able to run as duplicate
     * key. Only work in MariaDB or MySQL.
     * 
     * @param colName column name to be update
     * @param value   value to be update
     * @return this
     */
    public SQLInsertBuilder onDuplicateKeyUpdate(String colName, Date value) {
        this.duplicateKeyUpdateCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Add a column that will be update when insert not able to run as duplicate
     * key. Only work in MariaDB or MySQL.
     * 
     * @param colName column name to be update
     * @param value   value to be update
     * @return this
     */
    public SQLInsertBuilder onDuplicateKeyUpdate(String colName, LocalDate value) {
        this.duplicateKeyUpdateCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Add a column that will be update when insert not able to run as duplicate
     * key. Only work in MariaDB or MySQL.
     * 
     * @param colName column name to be update
     * @param value   value to be update
     * @return this
     */
    public SQLInsertBuilder onDuplicateKeyUpdate(String colName, Timestamp value) {
        this.duplicateKeyUpdateCols.put(colName, SQLParameter.of(value));
        return this;
    }

    /**
     * Add a column that will be update when insert not able to run as duplicate
     * key. Only work in MariaDB or MySQL.
     * 
     * @param colName column name to be update
     * @param value   value to be update
     * @return this
     */
    public SQLInsertBuilder onDuplicateKeyUpdate(String colName, LocalDateTime value) {
        this.duplicateKeyUpdateCols.put(colName, SQLParameter.of(value));
        return this;
    }

    // ==================== SQL Build =======================

    /**
     * Check if builder is valid, i.e. target table and insert column cannot be
     * empty.
     * 
     * @return true if this builder result is valid, false otherwise
     */
    private boolean checkIfValid() {
        // Ensure target table defined and at least 1 insert column
        if (targetTable.isEmpty() || insertCols.isEmpty()) {
            return false;
        }

        // Ensure INSERT IGNORE not appear with UPSERT
        if (isInsertIgnore || !duplicateKeyUpdateCols.isEmpty()) {
            return isInsertIgnore ^ !duplicateKeyUpdateCols.isEmpty();
        }

        // Everything is ok
        return true;
    }

    @Override
    public List<SQLParameter> getParams() {
        List<SQLParameter> params = new ArrayList<>(insertCols.values());
        params.addAll(duplicateKeyUpdateCols.values());
        return params;
    }

    @Override
    public String toSQL() {
        if (!checkIfValid()) {
            throw new IllegalStateException("SQL builder is not correct");
        }

        // Prepare list of question mark for later join
        List<String> questionMarks = new ArrayList<>();
        for (int i = 0; i < insertCols.keySet().size(); i++) {
            questionMarks.add("?");
        }

        StringBuilder sb = new StringBuilder();

        // Insert keyword handling
        sb.append(isInsertIgnore ? "INSERT IGNORE " : "INSERT ");

        // Target table
        sb.append(" INTO ");
        sb.append(targetTable);

        // Columns name
        sb.append(" (" + String.join(",", insertCols.keySet()) + ") ");

        // Question marks as value
        sb.append(" VALUES (" + String.join(",", questionMarks) + ")");

        // ON DUPLICATE KEY UPDATE handling
        if (!duplicateKeyUpdateCols.isEmpty()) {
            sb.append(" ON DUPLICATE KEY UPDATE ");

            List<String> upsertAssignments = new ArrayList<>();
            for (String colName : duplicateKeyUpdateCols.keySet()) {
                upsertAssignments.add(colName + " = ?");
            }
            sb.append(String.join(", ", upsertAssignments));
        }

        // Remove exceed space and Return final query
        return sb.toString().trim().replace("  ", " ");
    }

    @Override
    public ParamSQL toParamSQL() {
        return new ParamSQL(this.toSQL(), getParams());
    }
}
