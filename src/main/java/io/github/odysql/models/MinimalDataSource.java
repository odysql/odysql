package io.github.odysql.models;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

/**
 * Minimal interface for data source, which help developer to easily migrate
 * from their custom SQL connection class.
 * <p>
 * Traditional, developer should use connection pool to manage SQL connections,
 * however, in some extreme case of legacy code, it may not use this concept.
 * This class will provide a minimal impact approach to reduce too complicated
 * code without call <code>getConnection</code> every time when use SQL
 * builders.
 * 
 * @see javax.sql.DataSource
 */
public interface MinimalDataSource {

    /**
     * Attempts to establish a connection with the data source that
     * this {@code MinimalDataSource} object represents.
     * <p>
     * This method is same definition with
     * {@link javax.sql.DataSource#getConnection()}
     * method.
     *
     * @return a connection to the data source
     * @exception SQLException if a database access error occurs
     * @throws SQLTimeoutException when the driver has determined that the
     *                             timeout value specified by the
     *                             {@code setLoginTimeout} method
     *                             has been exceeded and has at least tried
     *                             to cancel the
     *                             current database connection attempt
     * @see javax.sql.DataSource#getConnection()
     */
    Connection getConnection() throws SQLException;
}
