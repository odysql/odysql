package io.github.odysql.builders.single;

import java.util.ArrayList;
import java.util.List;

import io.github.odysql.models.SQLCondition;
import io.github.odysql.models.SQLParameter;

/** A simple SQL DELETE query builder. */
public class SQLDeleteBuilder implements Conditionable<SQLDeleteBuilder>, SingleSQLBuildable {
    /** Table name to preform delete. */
    private String targetTable = "";

    /** SQLCondition for update statement. */
    private SQLCondition condition = null;
    private List<SQLParameter> conditionParams = new ArrayList<>();

    /** Create a new SQL builder for DELETE statement. */
    public SQLDeleteBuilder() {
        // Used for 1st line of chain fluent setters
    }

    /**
     * Set target table to delete.
     *
     * @param tableName target table name
     * @return this
     */
    public SQLDeleteBuilder from(String tableName) {
        this.targetTable = tableName;
        return this;
    }

    /**
     * Add WHERE clause to query. Developer MUST not used this method twice, or the
     * result will be overwritten by latest call.
     * <p>
     * <b>Since {@code DELETE} query is dangerous, condition MUST NOT be empty.</b>
     * <p>
     * Developer are suggested to use '?' symbol instead of actual value to prevent
     * SQL injection.
     * 
     * @param cond the condition to add
     * @return this
     * @see SQLCondition
     */
    @Override
    public SQLDeleteBuilder where(SQLCondition cond) {
        this.condition = cond;
        return this;
    }

    @Override
    public SQLDeleteBuilder param(SQLParameter... arguments) {
        for (SQLParameter item : arguments) {
            this.conditionParams.add(item);
        }
        return this;
    }

    /**
     * Check if builder is valid, i.e. target table and condition cannot be empty.
     * <p>
     * Due to DELETE query has danger to delete whole table, therefore
     * {@code SQLCondition} can not be empty.
     * 
     * @return true if this builder result is valid, false otherwise
     */
    private boolean checkIfValid() {
        return !targetTable.isEmpty() && !SQLCondition.isEmpty(condition);
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

        return new StringBuilder()
                .append("DELETE FROM ")
                .append(this.targetTable)
                .append(" WHERE ")
                .append(this.condition.asSQL())
                .toString();
    }

    @Override
    public List<SQLParameter> getParams() {
        return this.conditionParams;
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
