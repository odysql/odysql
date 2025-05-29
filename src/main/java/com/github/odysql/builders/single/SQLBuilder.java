package com.github.odysql.builders.single;

import java.util.List;

import com.github.odysql.models.SQLParameter;

/** Interface for SQL builder pattern. */
interface SQLBuilder {

    /**
     * Get ordered parameters list, which is the values that will be put into
     * statement with '?' character.
     * 
     * @return list of parameters, its order is same as sql string creation order.
     */
    public List<SQLParameter> getParams();

    /**
     * Get a new <code>PreparedSQL</code> from this builder. Developer can create
     * <code>PreparedStatement</code> or debug SQL easier from returned object.
     * 
     * @return <code>PreparedSQL</code> object that is ready to create
     *         <code>PreparedStatement</code> or debug SQL.
     */
    public ParamSQL toParamSQL();

    /**
     * Get parameterized SQL as string. Developer will lose easy access to create
     * prepared statement when compare to
     * {@link com.github.odysql.builders.single.SQLBuilder#toParamSQL()}, use this
     * will
     * caution.
     * 
     * @return parameterized SQL string
     */
    public String toSQL();

}
