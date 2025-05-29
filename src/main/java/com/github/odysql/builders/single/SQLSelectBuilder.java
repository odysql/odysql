package com.github.odysql.builders.single;

import java.util.ArrayList;
import java.util.List;

import com.github.odysql.helpers.NonNull;
import com.github.odysql.models.SQLCondition;
import com.github.odysql.models.SQLJoinData;
import com.github.odysql.models.SQLParameter;

/** The SQL builder that specified design for SELECT. */
public class SQLSelectBuilder implements SQLBuilder, Conditionable<SQLSelectBuilder> {
    /** The column name to be select */
    private List<String> selectCols = new ArrayList<>();

    /**
     * The column name to use ORDER BY, which the order will affect the final query.
     * There will not have any checking syntax correctness.
     */
    private List<String> orderCols = new ArrayList<>();

    /**
     * The column name to use GROUP BY. There will not have any checking syntax
     * correctness.
     */
    private List<String> groupCols = new ArrayList<>();

    /** Stored record for JOIN. Support LEFT JOIN .etc. */
    private List<SQLJoinData> joinedTables = new ArrayList<>();

    /** The condition for WHERE clause. */
    private SQLCondition whereCondition = null;

    /** Condition parameters. */
    private List<SQLParameter> conditionParam = new ArrayList<>();

    /** Stored query for WITH table syntax. */
    private List<String> withTables = new ArrayList<>();

    /** Condition parameters in WITH syntax. */
    private List<SQLParameter> withParam = new ArrayList<>();

    /**
     * The primary table name, which is not the table to join. Developer should
     * consider use join() if they require.
     */
    private String mainTable = "";

    /** Determine to use SELECT DISTINCT or not. */
    private boolean isDistinct = false;

    /** The value of OFFSET, default is null. */
    private Integer offset = null;

    /** The value of LIMIT. Default row is null. */
    private Integer limitRow = null;

    /** The value of FETCH FIRST ROWS ONLY. Default row is null. */
    private Integer fetchRow = null;

    // ==================================================================

    /** Create a new SQL Select Builder, which is start point fo fluent API. */
    public SQLSelectBuilder() {
        // Default constructor
    }

    // ==================================================================

    /**
     * Specific a column to be select in constructed query.
     * 
     * @param column column name, can include alias (AS), or more complex statement
     * @return this
     * @throws IllegalArgumentException if column is placed with "*" character
     */
    public SQLSelectBuilder select(String column) {
        column = NonNull.safeStr(column);

        if ("*".equals(column)) {
            throw new IllegalArgumentException("'SELECT *' is not allowed.");
        }

        this.selectCols.add(column);
        return this;
    }

    /**
     * Set the table name used in FROM clauses. Please note that, developer should
     * consider use join() if they require select from multiple tables.
     * 
     * @param tableName the table name
     * @return this
     */
    public SQLSelectBuilder from(String tableName) {
        this.mainTable = tableName;
        return this;
    }

    /**
     * Use SELECT DISTINCT syntax. This action is irreversible.
     * 
     * @return this
     */
    public SQLSelectBuilder distinct() {
        this.isDistinct = true;
        return this;
    }

    /**
     * Use ORDER BY syntax. Developer can specific ASC or DESC when input the column
     * name, e.g. "col1 DESC" or "col2 ASC".
     * <p>
     * Please note that ORDER BY syntax is affected by the column ordering.
     * 
     * @param columnName column name, can include alias (AS), or more complex
     *                   statement
     * @return this
     */
    public SQLSelectBuilder orderBy(String columnName) {
        this.orderCols.add(columnName);
        return this;
    }

    /**
     * Use GROUP BY syntax. Please note that GROUP BY syntax is affected by the
     * column ordering.
     * 
     * @param columnName column name, can include alias (AS), or more complex
     *                   statement
     * @return this
     */
    public SQLSelectBuilder groupBy(String columnName) {
        this.groupCols.add(columnName);
        return this;
    }

    /**
     * Perform LEFT JOIN on the query. Developer are allow to use this method for
     * unlimited time.
     * 
     * @param tableName the table to be left join, can include alias (AS)
     * @param criteria  same effect as ON statement in LEFT JOIN
     * @return this
     */
    public SQLSelectBuilder leftJoin(String tableName, SQLCondition criteria) {
        this.joinedTables.add(SQLJoinData.asLeftJoin(tableName, criteria));
        return this;
    }

    /**
     * Perform INNER JOIN on the query. Developer are allow to use this method for
     * unlimited time.
     * 
     * @param tableName the table to be INNER join, can include alias (AS)
     * @param criteria  same effect as ON statement in INNER JOIN
     * @return this
     */
    public SQLSelectBuilder innerJoin(String tableName, SQLCondition criteria) {
        this.joinedTables.add(SQLJoinData.asInnerJoin(tableName, criteria));
        return this;
    }

    /**
     * Use LIMIT statement. This method SHOULD not be called more than once, or the
     * result will be overwritten by latest call.
     * 
     * @param value value to be limit
     * @return this
     */
    public SQLSelectBuilder limit(int value) {
        this.limitRow = value;
        return this;
    }

    /**
     * Use LIMIT OFFSET statement. This method SHOULD not be called more than once,
     * or the result will be overwritten by latest call.
     * 
     * @param limit  value of limit
     * @param offset value of offset
     * @return this
     */
    public SQLSelectBuilder limitOffset(int limit, int offset) {
        this.limitRow = limit;
        this.offset = offset;
        return this;
    }

    /**
     * Use <code>FETCH FIRST {@literal ?} ROW ONLY</code> statement. This method
     * SHOULD not be called more than once, or the result will be overwritten by
     * latest call.
     * 
     * @param rowCount the number of row to be fetched
     * @return this
     */
    public SQLSelectBuilder fetchFirst(int rowCount) {
        this.fetchRow = rowCount;
        return this;
    }

    /**
     * Use WITH statement. Developer are allowed to call this method multiple times.
     * <p>
     * Please note that there will not have guaranteed syntax checking for generated
     * query which builder used this method.
     * 
     * @param tableName the table name of with statement
     * @param query     the another SQLSelectBuilder, that stored completed SELECT
     *                  statement
     * @return this
     */
    public SQLSelectBuilder with(String tableName, SQLSelectBuilder query) {
        // Extract condition param from another builder to with param
        this.withParam.addAll(query.withParam);
        this.withParam.addAll(query.conditionParam);

        // Put WITH statement before main query
        this.withTables.add(String.format("%s AS (%s)", tableName, query.toSQL()));
        return this;
    }

    // ======================= Condition -able =======================

    @Override
    public SQLSelectBuilder where(SQLCondition cond) {
        this.whereCondition = cond;
        return this;
    }

    @Override
    public SQLSelectBuilder param(SQLParameter... arguments) {
        for (SQLParameter item : arguments) {
            this.conditionParam.add(item);
        }

        return this;
    }

    @Override
    public List<SQLParameter> getParams() {
        // Get parameters list, with first, then condition
        List<SQLParameter> paramList = new ArrayList<>();
        paramList.addAll(withParam);
        paramList.addAll(conditionParam);

        return paramList;
    }

    // ================= SQL Builder Methods ===================

    /**
     * Construct SQL string from builder.
     * 
     * @return constructed SQL, which is parameterized.
     */
    private String constructSQL() {
        StringBuilder builder = new StringBuilder();

        // WITH table if any
        if (!this.withTables.isEmpty()) {
            builder.append("WITH ");
            builder.append(String.join(", ", withTables));
            builder.append(" ");
        }

        // Select command
        if (this.isDistinct) {
            builder.append("SELECT DISTINCT ");
        } else {
            builder.append("SELECT ");
        }

        // Column to be select
        builder.append(String.join(", ", this.selectCols));

        // FROM
        builder.append(" FROM " + this.mainTable);

        // JOIN
        if (!joinedTables.isEmpty()) {
            for (SQLJoinData item : joinedTables) {
                builder.append(" " + item.asSQL());
            }
        }

        // WHERE
        if (!SQLCondition.isEmpty(whereCondition)) {
            builder.append(" WHERE " + whereCondition.asSQL());
        }

        // GROUP BY
        if (!this.groupCols.isEmpty()) {
            builder.append(" GROUP BY " + String.join(", ", this.groupCols));
        }

        // ORDER BY
        if (!this.orderCols.isEmpty()) {
            builder.append(" ORDER BY " + String.join(", ", this.orderCols));
        }

        // LIMIT & OFFSET
        if (this.limitRow != null && this.offset != null) {
            builder.append(" LIMIT " + limitRow + " OFFSET " + this.offset);
        } else if (this.limitRow != null) {
            builder.append(" LIMIT " + limitRow);
        }

        // FETCH FIRST
        if (this.fetchRow != null) {
            builder.append(" FETCH FIRST " + this.fetchRow + " ROWS ONLY ");
        }

        // Remove exceed space and Return final query
        return builder.toString().replace("  ", " ");
    }

    @Override
    public String toSQL() {
        return constructSQL();
    }

    @Override
    public ParamSQL toParamSQL() {
        String sql = constructSQL();
        return new ParamSQL(sql, this.getParams());
    }
}
