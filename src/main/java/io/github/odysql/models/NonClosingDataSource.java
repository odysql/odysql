package io.github.odysql.models;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

/**
 * Interface for data source classes that provide a database connection without
 * proper connection management, i.e. the connection is most likely not managed
 * by pool, and connection is keep opening for application usage.
 * <p>
 * Traditional, developer should use connection pool to manage SQL connections,
 * however, in some extreme case of legacy code, it may not use this concept.
 * This class will provide a minimal impact approach to reduce too complicated
 * code without call {@code getConnection} every time when use SQL builders.
 * <p>
 * Please note that, for long term development, these legacy data source class
 * should be replaced by a proper designed data source, e.g. {@code HikariCP}.
 */
public interface NonClosingDataSource {
    /**
     * Attempts to establish a connection with the data source that this
     * {@code NonClosingDataSource} object represents.
     *
     * @return a connection to the data source
     * @throws SQLException        if a database access error occurs
     * @throws SQLTimeoutException when the driver has determined that the timeout
     *                             value specified by the {@code setLoginTimeout}
     *                             method has been exceeded and has at least tried
     *                             to cancel the current database connection attempt
     */
    Connection getConnection() throws SQLException;
}
