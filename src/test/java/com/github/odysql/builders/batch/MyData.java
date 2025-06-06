package com.github.odysql.builders.batch;

/** Test usage data container. */
class MyData {
    private final String column1;
    private final Integer column2;
    private final char column3;

    public MyData(String column1, Integer column2, char column3) {
        this.column1 = column1;
        this.column2 = column2;
        this.column3 = column3;
    }

    public String getColumn1() {
        return column1;
    }

    public Integer getColumn2() {
        return column2;
    }

    public char getColumn3() {
        return column3;
    }
}
