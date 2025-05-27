package com.github.odysql.models;

/** The container for holding JOIN details. */
public class SQLJoinData implements SQLFragment {
    private SQLJoinType joinType;
    private String tableName;
    private SQLCondition condition;

    /**
     * Create a data container that store JOIN operation.
     * 
     * @param joinType  type of JOIN, e.g. <code>SQLJoinType.LEFT</code>
     * @param tableName the table name to be joined
     * @param condition the condition to JOIN, which is same as <code>ON</code>
     */
    private SQLJoinData(SQLJoinType joinType, String tableName, SQLCondition condition) {
        this.joinType = joinType;
        this.tableName = tableName;
        this.condition = condition;
    }

    /**
     * Create a data container that store LEFT JOIN operation.
     * 
     * @param tableName the table name to be joined
     * @param condition the condition to JOIN, which is same as <code>ON</code>
     * @return data for join operation
     */
    public static SQLJoinData asLeftJoin(String tableName, SQLCondition condition) {
        return new SQLJoinData(SQLJoinType.LEFT, tableName, condition);
    }

    /**
     * Create a data container that store INNER JOIN operation.
     * 
     * @param tableName the table name to be joined
     * @param condition the condition to JOIN, which is same as <code>ON</code>
     * @return data for join operation
     */
    public static SQLJoinData asInnerJoin(String tableName, SQLCondition condition) {
        return new SQLJoinData(SQLJoinType.INNER, tableName, condition);
    }

    @Override
    public String asSQL() {
        return joinType.asSQL() + " JOIN " + tableName + " ON " + condition.asSQL();
    }
}
