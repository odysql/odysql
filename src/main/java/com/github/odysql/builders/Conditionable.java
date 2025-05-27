package com.github.odysql.builders;

import java.util.List;

import com.github.odysql.models.SQLCondition;
import com.github.odysql.models.SQLParameter;

/**
 * Interface for builder that has "WHERE" syntax.
 * 
 * @param <ImplT> Implementation of this interface
 */
interface Conditionable<ImplT> {
    /**
     * Add WHERE clause to query. Developer MUST not used this method twice, or the
     * result will be overwritten by latest call.
     * <p>
     * Developer are suggested to use '?' symbol instead of actual value to prevent
     * SQL injection.
     * 
     * @param cond the condition to add
     * @return this
     * @see SQLCondition
     */
    public ImplT where(SQLCondition cond);

    /**
     * Add <code>SQLParameter</code> clause to query.
     * 
     * @param arguments the value(s) of question mark symbols
     * @return this
     * @see SQLParameter
     */
    public ImplT param(SQLParameter... arguments);

    /**
     * Add <code>SQLParameter</code> clause to query.
     * 
     * @param arguments the value(s) of question mark symbols
     * @return this
     * @see SQLParameter
     */
    public default ImplT param(List<SQLParameter> arguments) {
        return param(arguments.toArray(new SQLParameter[0]));
    }
}
