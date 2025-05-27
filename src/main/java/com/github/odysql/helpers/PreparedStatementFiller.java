package com.github.odysql.helpers;

import java.util.List;

import com.github.odysql.models.SQLParameter;

/** Utils to fill a prepared statement by SQL parameter. */
public class PreparedStatementFiller {
    private PreparedStatementFiller() {
    }

    /**
     * Get SQL for debugging, which is not for prepared statement.
     * <p>
     * Please note that this method is only for debugging purpose, the SQL returned
     * may not able to generate SQL that runnable in all SQL server, as date format
     * may be different.
     * <p>
     * The returned SQL will able to run in some MariaDB server when it contains
     * <code>DATE</code> &amp; <code>DATETIME</code> values, BUT NOT GUARANTEED.
     * Developer SHOULD always use PreparedStatement when possible.
     * <p>
     * Please note that all SQL <code>DATETIME</code> will use
     * <code>yyyy-MM-dd HH:mm:ss</code> as date time format, while <code>DATE</code>
     * use <code>yyyy-MM-dd</code> as date format.
     * 
     * @param sql    the final sql built
     * @param params list of SQL parameters to inject into SQL
     * @return complete SQL statement that not contain "?" character; or
     *         <code>null</code> if sql is <code>null</code>
     */
    public static String asDebugSQL(String sql, List<SQLParameter> params) {
        if (sql == null) {
            return null;
        }

        // Loop through all parameters
        for (SQLParameter param : params) {
            // Replace question mark character
            sql = sql.replaceFirst("\\?", param.getValueAsSQL());
        }

        return sql;
    }
}
