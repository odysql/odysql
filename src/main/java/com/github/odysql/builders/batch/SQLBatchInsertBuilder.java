package com.github.odysql.builders.batch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * SQL Builder for batch INSERT operation.
 * <p>
 * Please note this class has completely different design with
 * <code>SQLInsertBuilder</code>. Full example to use is like:
 * <blockquote>
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
        // Prepare list of question mark for later join
        List<String> questionMarks = new ArrayList<>();
        for (int i = 0; i < insertCols.keySet().size(); i++) {
            questionMarks.add("?");
        }

        // Question marks as value
        return " (" + String.join(",", questionMarks) + ")";
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

        // Concat two SQL part, and remove exceed space
        String sql = toBasePartSQL() + toParamPartSQL();
        return sql.trim().replace("  ", " ");
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
