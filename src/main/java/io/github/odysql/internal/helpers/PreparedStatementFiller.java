package io.github.odysql.internal.helpers;

import java.util.List;

import io.github.odysql.models.SQLParameter;

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
     * @throws IllegalArgumentException when no. of "?" character is larger/smaller
     *                                  than params size
     */
    public static String asDebugSQL(String sql, List<SQLParameter> params) throws IllegalArgumentException {
        if (sql == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int paramsIdx = 0;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            // Normal append if not question mark
            if (c != '?') {
                sb.append(c);
                continue;
            }

            // Ensure parameter index not exceed parameters size
            if (paramsIdx >= params.size()) {
                throw new IllegalArgumentException("Placeholder and SQLParameter count is not matched");
            }

            // Append SQLParameter debug value instead of '?'
            sb.append(params.get(paramsIdx).toDebugSQL());
            paramsIdx++;
        }

        if (paramsIdx < params.size()) {
            throw new IllegalArgumentException("Placeholder and SQLParameter count is not matched");
        }

        return sb.toString();
    }
}
