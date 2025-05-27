package com.github.odysql.models;

/** Object that consider is part of SQL, which cannot run independently. */
interface SQLFragment {
    /**
     * Get the SQL part for this SQL fragment.
     * 
     * @return SQL that is constructed from this object.
     */
    public String asSQL();
}
