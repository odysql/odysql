package com.github.odysql.models;

/** Enum for UNION type. */
public enum SQLUnionType implements SQLFragment {
    /** Union Type of <code>UNION</code> syntax. */
    UNION("UNION"),
    /** Union Type of <code>UNION ALL</code> syntax. */
    UNION_ALL("UNION ALL");

    private String sql;

    private SQLUnionType(String sql) {
        this.sql = sql;
    }

    @Override
    public String asSQL() {
        return sql;
    }
}
