package io.github.odysql.builders.batch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.github.odysql.models.SQLParameterRetriever;

/**
 * SQL Builder for batch INSERT operation.
 * <p>
 * Please note this class has completely different design with
 * <code>SQLInsertBuilder</code>. Full example to use is like: <blockquote>
 * 
 * <pre>
 * SQLBatchInsertBuilder&lt;MyData&gt; builder = new SQLBatchInsertBuilder&lt;MyData&gt;()
 *         .into("my_table")
 *         .insert("col1", item -&gt; SQLParameter.of(item.getCol1Value()))
 *         .insert("col2", item -&gt; SQLParameter.of(item.getCol2Value()))
 *         .insert("total_sum", item -&gt; SQLParameter.of(item.getVal1() + item.getVal2()));
 * 
 * // Get Parameterized SQL string ONLY
 * String sql = builder.toSQL();
 * 
 * // Get Batch support SQL
 * SQLBatchInsertRunner&lt;MyData&gt; batch = builder.toBatchRunner();
 * 
 * // Execute batch
 * int totalAffected = batch.setData(myDataList).executeWith(conn);
 * </pre>
 * 
 * </blockquote>
 * 
 * @param <DataT> Data type to be insert
 */
public class SQLBatchInsertBuilder<DataT> {
    /** Table name to insert records. */
    private String targetTable = "";

    /** Use syntax INSERT IGNORE. Only support MariaDB. */
    private boolean isInsertIgnore = false;

    /**
     * Column name & value to be update when duplicate key, i.e.
     * {@code ON DUPLICATE KEY UPDATE}. Only support MariaDB/MySQL.
     */
    private List<String> duplicateKeyUpdateCols = new ArrayList<>();

    /** Column name & value to be inserted. */
    private LinkedHashMap<String, SQLParameterRetriever<DataT>> insertCols = new LinkedHashMap<>();

    /** Create a new SQL builder for Batch INSERT statement. */
    public SQLBatchInsertBuilder() {
        // This method is used for 1st line of fluent setters
    }

    /**
     * Set column to be inserted.
     * 
     * @param colName   column name
     * @param retriever lambda function to get <code>SQLParameter</code> from given
     *                  data, retrieved parameter will insert to given column name
     * @return this
     */
    public SQLBatchInsertBuilder<DataT> insert(String colName, SQLParameterRetriever<DataT> retriever) {
        insertCols.put(colName, retriever);
        return this;
    }

    /**
     * Use <code>INSERT IGNORE</code> syntax instead of <code>INSERT</code> syntax.
     * Only works with MariaDB.
     * 
     * @return this
     */
    public SQLBatchInsertBuilder<DataT> insertIgnore() {
        this.isInsertIgnore = true;
        return this;
    }

    /**
     * Update specific column when failed to inset due to duplicate key, while using
     * value that will be used in {@code INSERT}. Only work with MariaDB/MySQL.
     * <p>
     * This method has <b>strict limitation</b> that only ables to UPDATE column
     * with value that used in INSERT. For example, Developer cannot INSERT "abc" as
     * "column1" value and set "bcd" as "column1" value when insert is not possible.
     * This builder only support using both "abc" as value that used in both INSERT
     * and UPDATE.
     * 
     * @param colName column name to be updated
     * @return this
     */
    public SQLBatchInsertBuilder<DataT> onDuplicateKeyUpdate(String colName) {
        this.duplicateKeyUpdateCols.add(colName);
        return this;
    }

    /**
     * Set column to be inserted, or update it when duplicate key appear, only work
     * in MariaDB/MySQL.
     * <p>
     * This method has <b>strict limitation</b> that only ables to UPDATE column
     * with value that used in INSERT. For example, Developer cannot INSERT "abc" as
     * "column1" value and set "bcd" as "column1" value when insert is not possible.
     * This builder only support using both "abc" as value that used in both INSERT
     * and UPDATE.
     * <p>
     * This method is a short-hand for
     * {@link #insert(String, SQLParameterRetriever)} and
     * {@link #onDuplicateKeyUpdate(String)}.
     * 
     * @param colName   column name to be insert, or update when duplicate key occur
     * @param retriever lambda function to get <code>SQLParameter</code> from given
     *                  data, retrieved parameter will insert to given column name.
     *                  When duplicate key occur and change to update, the value
     *                  will not be used directly, but via value that used in
     *                  INSERT.
     * @return this
     */
    public SQLBatchInsertBuilder<DataT> insertOnDuplicateUpdate(String colName,
            SQLParameterRetriever<DataT> retriever) {
        this.insertCols.put(colName, retriever);
        this.duplicateKeyUpdateCols.add(colName);
        return this;
    }

    /**
     * Set the table name to be inserted.
     * 
     * @param tableName table name
     * @return this
     */
    public SQLBatchInsertBuilder<DataT> into(String tableName) {
        this.targetTable = tableName;
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
        // Ensure insert target table and columns are not empty
        if (targetTable.isEmpty() || insertCols.isEmpty()) {
            return false;
        }

        // Ensure INSERT IGNORE not appear with ON DUPLICATE KEY UPDATE
        if (isInsertIgnore && !duplicateKeyUpdateCols.isEmpty()) {
            return false;
        }

        return !targetTable.isEmpty() && !insertCols.isEmpty();
    }

    /**
     * Create base Part of SQL.
     * <p>
     * For example, complete SQL is
     * <code>INSERT INTO my_table (column1, column2) VALUES (?, ?)</code>, then this
     * function return <code>INSERT INTO my_table (column1, column2) VALUES</code>.
     * 
     * @return base part of SQL
     */
    private String toBasePartSQL() {
        StringBuilder sb = new StringBuilder();

        // Insert keyword handling
        sb.append(isInsertIgnore ? "INSERT IGNORE " : "INSERT ");

        // Target table
        sb.append(" INTO ");
        sb.append(targetTable);

        // Columns name
        sb.append(" (" + String.join(",", insertCols.keySet()) + ") ");

        // VALUES keyword
        sb.append(" VALUES ");

        // Remove exceed space and Return final query
        return sb.toString().trim().replace("  ", " ");
    }

    /**
     * Create parameter part of SQL, which is multiple '?' character with bracket.
     * <p>
     * For example, complete SQL is
     * <code>INSERT INTO my_table (column1, column2) VALUES (?, ?)</code>, then this
     * function return <code>(?, ?)</code>.
     * 
     * @return parameter part of SQL
     */
    private String toParamPartSQL() {
        // Prepare question marks string
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < insertCols.keySet().size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("?");
        }

        // Question marks as value
        return " (" + sb.toString() + ")";
    }

    /**
     * Create completed and parameterized SQL from this builder.
     * 
     * @return SQL string, parameterized
     * @throws IllegalStateException when SQL builder is invalid to build SQL
     */
    public String toSQL() throws IllegalStateException {
        if (!checkIfValid()) {
            throw new IllegalStateException("SQL builder is not correct");
        }

        // String builder as complex logic needed
        StringBuilder sb = new StringBuilder();

        // Concat two SQL part,
        sb.append(toBasePartSQL());
        sb.append(toParamPartSQL());

        // ON DUPLICATE KEY UPDATE part
        if (!duplicateKeyUpdateCols.isEmpty()) {
            sb.append(" ON DUPLICATE KEY UPDATE ");
            ArrayList<String> parts = new ArrayList<>();
            for (String colName : duplicateKeyUpdateCols) {
                parts.add(colName + "=VALUES(" + colName + ")");
            }
            sb.append(String.join(",", parts));
        }

        // remove exceed space
        return sb.toString().trim().replace("  ", " ");
    }

    @Override
    public String toString() {
        return toSQL();
    }

    /**
     * Get batch SQL object that provide rich support with batch insert to database.
     * 
     * @return object that support batch insert to database and logging in its
     *         method
     * @throws IllegalStateException when SQL builder is invalid to build SQL
     * @see SQLBatchInsertRunner
     */
    public SQLBatchInsertRunner<DataT> toBatchRunner() throws IllegalStateException {
        if (!checkIfValid()) {
            throw new IllegalStateException("SQL builder is not correct");
        }

        return new SQLBatchInsertRunner<>(
                this.toBasePartSQL(),
                this.toParamPartSQL(),
                new ArrayList<>(insertCols.values()));
    }
}
