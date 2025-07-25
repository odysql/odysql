package io.github.odysql.builders.single;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import io.github.odysql.internal.helpers.PreparedStatementFiller;
import io.github.odysql.models.SQLParameter;

/** Container for completed SQL that ready to be prepared statement. */
public final class ParamSQL {

    private final String preparedSQL;
    private final List<SQLParameter> parameters;

    private final String debugSQL;

    /**
     * Create a new <code>ParamSQL</code> by a parameterized SQL and its parameter.
     * This function will create debug SQL automatically.
     * 
     * @param sql        parameterized SQL string
     * @param parameters list of parameter
     */
    public ParamSQL(String sql, List<SQLParameter> parameters) {
        this.preparedSQL = sql;
        this.parameters = parameters;

        // Run filler for prepared statement
        this.debugSQL = PreparedStatementFiller.asDebugSQL(sql, parameters);
    }

    /**
     * Get parameterized SQL for create Prepared Statement.
     * 
     * @return parameterized SQL
     */
    public String getPreparedSQL() {
        return this.preparedSQL;
    }

    /**
     * Get debug SQL for logging.
     * 
     * @return debug SQL, all <code>?</code> are replaced by its parameter
     */
    public String getDebugSQL() {
        return debugSQL;
    }

    /**
     * Fill the Prepared Statement with the value that already store in the builder.
     * <p>
     * This method is aim to ensure no more counting index for e.g.
     * <code>stmt.setString(int, String)</code>, the index increment and handle will
     * be completed by the builder itself.
     * 
     * @param statement the generated Prepared Statement, which should be get from
     *                  <code>Connection.prepareStatement</code>.
     * @return Prepared Statement, which all designated parameter is set.
     * @throws SQLException when it is unable to assign value to designated
     *                      parameter.
     */
    private PreparedStatement fill(PreparedStatement statement) throws SQLException {
        int index = 0;

        // Fill variable with param list
        for (SQLParameter param : this.parameters) {
            // Index is start from 1
            index++;

            // Handle parameter with designed type
            statement = param.apply(statement, index);
        }

        return statement;
    }

    /**
     * Get a filled Prepared Statement with the value that already store in the
     * builder, with
     * {@link io.github.odysql.builders.single.SingleSQLBuildable#toParamSQL()}
     * used as SQL.
     * <p>
     * This method is designed to use in try-with-resources, as a short-cut method
     * for {@link #fill(PreparedStatement)}
     * 
     * @param conn Connection to database
     * @return Prepared Statement, which all designated parameter is set.
     * @throws SQLException when it is unable to assign value to designated
     *                      parameter.
     */
    public PreparedStatement prepare(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(this.preparedSQL);
        return this.fill(stmt);
    }

    /**
     * Get a filled Prepared Statement with the value that already store in the
     * builder, with
     * {@link io.github.odysql.builders.single.SingleSQLBuildable#toParamSQL()}
     * used as SQL.
     * <p>
     * This method is designed to use in try-with-resources, as a short-cut method
     * for {@link #fill(PreparedStatement)}.
     * <p>
     * Please note that this function provide functionality similar to
     * {@link Connection#prepareStatement(String, int)}.
     * 
     * @param conn              Connection to database
     * @param autoGeneratedKeys a flag indicating whether auto-generated keys should
     *                          be returned; one of Statement.RETURN_GENERATED_KEYS
     *                          or Statement.NO_GENERATED_KEYS
     * @return Prepared Statement, which all designated parameter is set.
     * @throws SQLException when it is unable to assign value to designated
     *                      parameter.
     */
    public PreparedStatement prepare(Connection conn, int autoGeneratedKeys) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(this.preparedSQL, autoGeneratedKeys);
        return this.fill(stmt);
    }
}
