package com.github.odysql.models;

/** The type for JOIN type. */
enum SQLJoinType implements SQLFragment {
    /** same as <code>LEFT JOIN<code> */
    LEFT("LEFT"),
    /** same as <code>INNER JOIN<code> */
    INNER("INNER");

    private String sql;

    private SQLJoinType(String sql) {
        this.sql = sql;
    }

    @Override
    public String asSQL() {
        return sql;
    }
}
