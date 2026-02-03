package io.github.odysql.builders.batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.odysql.internal.helpers.PreparedStatementFiller;
import io.github.odysql.models.SQLParameter;
import io.github.odysql.models.SQLParameterRetriever;

/**
 * Runner that provide support to bulk insert data.
 * <p>
 * Example to use is like <blockquote>
 * 
 * <pre>
 * // Only created by builders
 * SQLBatchInsertRunner&lt;MyData&gt; batch = builder.toBatchRunner();
 * 
 * // Config
 * batch.setMaxBatchSize(2000); // Batch size
 * batch.setLogEnabled(true); // Enable debug log generation
 * 
 * // Set data
 * batch.setData(myDataList);
 * 
 * // Execute
 * int totalAffected = batch.executeWith(conn);
 * 
 * // Get debug log
 * List&lt;String&gt; sqlExecuted = batch.getDebugSQL();
 * </pre>
 * 
 * </blockquote>
 * 
 * @param <DataT> Data type to be insert
 */
public class SQLBatchInsertRunner<DataT> {
    /**
     * The base part of SQL, e.g.
     * <code>INSERT INTO my_table (column1, column2) VALUES</code>.
     */
    private final String basePartSQL;

    /** Parameter part of SQL, e.g. <code>(?, ?)</code> */
    private final String paramPartSQL;

    /**
     * SQL part that is after parameter part, e.g. {@code ON DUPLICATE KEY UPDATE}.
     */
    private final String afterParamSQL;

    /**
     * Completed SQL that combined from <code>basePartSQL</code>,
     * <code>paramPartSQL</code>, and <code>afterParamSQL</code>.
     */
    private final String preparedSQL;

    /** List of Retriever function for get SQLParameter from given data type. */
    private final List<SQLParameterRetriever<DataT>> retrievers;

    /**
     * Configuration for maximum batch size. For every n-th insert, then
     * <code>executeBatch</code> will be called to prevent too many insert at one
     * time.
     */
    private int maxBatchSize = 1000;

    /** Data list to be insert to database. */
    private List<DataT> data;

    /** Configuration for enable logging. Default is false to reduce overhead. */
    private boolean isLogEnabled = false;

    /**
     * Container for SQL executed, which has converted to non-parameterized SQL as
     * possible.
     */
    private List<String> debugSQL = new ArrayList<>();

    /**
     * Create new SQLBatchInsertRunner object.
     * 
     * @param baseSQL       base part of SQL
     * @param paramSQL      parameter part ('?' characters) of SQL
     * @param afterParamSQL part of SQL that comes after parameter part, e.g.
     *                      {@code ON DUPLICATE KEY UPDATE}
     * @param retrievers    functions to retrieve SQL parameter from data, order
     *                      dependent
     */
    SQLBatchInsertRunner(String baseSQL, String paramSQL, String afterParamSQL,
            List<SQLParameterRetriever<DataT>> retrievers) {
        this.basePartSQL = baseSQL;
        this.paramPartSQL = paramSQL;
        this.afterParamSQL = afterParamSQL;
        this.retrievers = retrievers;

        String completeSQL = baseSQL + " " + paramSQL + " " + afterParamSQL;
        this.preparedSQL = completeSQL.trim();
    }

    /**
     * Function to preview prepared SQL that will be used to insert data.
     * 
     * @return prepared SQL
     */
    public String preparedSQL() {
        return this.preparedSQL;
    }

    /**
     * Set data to be insert to database.
     * 
     * @param data data to be inserted
     * @return this
     */
    public SQLBatchInsertRunner<DataT> setData(List<DataT> data) {
        this.data = data;
        return this;
    }

    /**
     * Set the maximum batch size. For every n-th insert, then
     * <code>executeBatch</code> will be called to prevent too many insert at one
     * time.
     * 
     * @param maxBatchSize batch size to config. Default is 1000.
     * @return this
     * @throws IllegalArgumentException when maxBatchSize is &le; 0
     */
    public SQLBatchInsertRunner<DataT> setMaxBatchSize(int maxBatchSize) throws IllegalArgumentException {
        if (maxBatchSize <= 0) {
            throw new IllegalArgumentException("batch size must be non-zero and positive.");
        }

        this.maxBatchSize = maxBatchSize;
        return this;
    }

    /**
     * Decide to enable debug log generation or not. Log will be only get-able
     * though {@link #getDebugSQL()} after {@link #executeWith(Connection)}
     * completed.
     * 
     * @param isLogEnabled true to enable, false otherwise. Default is false to
     *                     reduce overhead.
     * @return this
     */
    public SQLBatchInsertRunner<DataT> setLogEnabled(boolean isLogEnabled) {
        this.isLogEnabled = isLogEnabled;
        return this;
    }

    /**
     * Utility function to execute batch and create debug log if log is enabled.
     * 
     * @param stmt        statement to execute
     * @param paramRecord parameter to generate debug log
     * @return sum of affected row in this batch
     * @throws SQLException when failed to execute batch
     */
    private int executeBatchAndLog(PreparedStatement stmt, List<String> paramRecord) throws SQLException {
        int[] affected = stmt.executeBatch();

        // Add debug logs
        if (isLogEnabled) {
            String completeSQL = basePartSQL + String.join(",", paramRecord) + " " + afterParamSQL;
            this.debugSQL.add(completeSQL.trim());
        }

        return Arrays.stream(affected).sum();
    }

    /**
     * Execute bulk insert with given connection, but not commit changes. Developer
     * is fully responsible to control connection operation, e.g. rollback and
     * commit.
     * <p>
     * To prevent too many data is inserted at once, every n-th insert, this
     * function will call execute update. The n-th value is set by
     * {@link #setMaxBatchSize(int)}.
     * 
     * @param conn connection that used to generate prepared statement
     * @return total affected row.
     * @throws SQLException when failed to perform insert
     */
    public int executeWith(Connection conn) throws SQLException {
        this.debugSQL.clear();

        int totalAffected = 0;

        try (PreparedStatement stmt = conn.prepareStatement(preparedSQL)) {
            int batchCount = 0;
            List<String> paramRecord = new ArrayList<>();

            for (DataT item : this.data) {
                int idx = 1;

                // Set data into prepared statement
                List<SQLParameter> params = new ArrayList<>();

                for (SQLParameterRetriever<DataT> retriever : retrievers) {
                    SQLParameter p = retriever.retrieve(item);
                    params.add(p);

                    p.apply(stmt, idx);
                    idx++;
                }

                // Add to batch
                stmt.addBatch();
                batchCount++;

                // Add logs to param list
                if (isLogEnabled) {
                    paramRecord.add(PreparedStatementFiller.asDebugSQL(paramPartSQL, params));
                }

                // Execute batch for every n-th
                if (batchCount % maxBatchSize == 0) {
                    totalAffected += executeBatchAndLog(stmt, paramRecord);
                    paramRecord.clear();
                }
            }

            // Run remaining batches
            if (this.data.size() % maxBatchSize != 0) {
                totalAffected += executeBatchAndLog(stmt, paramRecord);
                paramRecord.clear();
            }

            return totalAffected;
        }
    }

    /**
     * Retrieve debug usage SQL (i.e. logs). Only has size &gt; 0 when log mode is
     * enabled and {@link #executeWith(Connection)} is completed.
     * 
     * @return list of debug SQL. Every item represent one batch executed. These SQL
     *         is rewritten that similar to database driver did.
     */
    public List<String> getDebugSQL() {
        return debugSQL;
    }
}
