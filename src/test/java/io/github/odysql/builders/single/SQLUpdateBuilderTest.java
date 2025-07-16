package io.github.odysql.builders.single;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.odysql.models.SQLCondition;
import io.github.odysql.models.SQLParameter;

class SQLUpdateBuilderTest {

    private Connection mockConn;
    private PreparedStatement mockStmt;

    @BeforeEach
    void init() throws SQLException {
        mockConn = mock(Connection.class);
        mockStmt = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
    }

    @Test
    void testSingleCol() throws SQLException {
        ParamSQL ps = new SQLUpdateBuilder()
                .from("some_db.some_table")
                .update("int_value", 759)

                .where(SQLCondition.create("table_name.id = ?"))
                .param(SQLParameter.of(123))

                .toParamSQL();

        assertEquals("UPDATE some_db.some_table SET int_value=759 WHERE table_name.id = 123", ps.getDebugSQL());

        String expectedSQL = "UPDATE some_db.some_table SET int_value=? WHERE table_name.id = ?";
        assertEquals(expectedSQL, ps.getPreparedSQL());

        PreparedStatement stmt = ps.prepare(mockConn);

        // Check if those function in mocked object are called
        verify(mockConn).prepareStatement(expectedSQL);
        verify(mockStmt).setInt(1, 759);
        verify(mockStmt).setInt(2, 123);

        // Assert
        assertEquals(mockStmt, stmt);
    }

    @Test
    void testParamType() throws SQLException {
        ParamSQL ps = new SQLUpdateBuilder()
                .from("some_db.some_table")
                .where(SQLCondition.create("table_name.id = ?"))
                .param(SQLParameter.of(123))

                .update("int_value", 123)
                .update("double_value", 123.45)
                .update("long_value", Long.valueOf(456))
                .update("string_value", "HELLO")
                .update("java_sql_date", Date.valueOf(LocalDate.of(2024, 1, 31)))
                .update("local_date", LocalDate.of(2024, 2, 28))
                .update("java_sql_timestamp", Timestamp.valueOf(LocalDateTime.of(2024, 3, 20, 14, 23, 56)))
                .update("local_datetime", LocalDateTime.of(2024, 04, 27, 15, 11, 23))

                .toParamSQL();

        assertEquals("UPDATE some_db.some_table SET int_value=123,double_value=123.45,long_value=456,"
                + "string_value='HELLO',java_sql_date='2024-01-31',local_date='2024-02-28',java_sql_timestamp='2024-03-20 14:23:56',local_datetime='2024-04-27 15:11:23' "
                + "WHERE table_name.id = 123", ps.getDebugSQL());

        String expectedSQL = "UPDATE some_db.some_table SET int_value=?,double_value=?,long_value=?,"
                + "string_value=?,java_sql_date=?,local_date=?,java_sql_timestamp=?,local_datetime=? "
                + "WHERE table_name.id = ?";
        assertEquals(expectedSQL, ps.getPreparedSQL());

        PreparedStatement stmt = ps.prepare(mockConn);

        // Check if those function in mocked object are called
        verify(mockConn).prepareStatement(expectedSQL);
        verify(mockStmt).setInt(1, 123);
        verify(mockStmt).setDouble(2, 123.45);
        verify(mockStmt).setLong(3, 456);
        verify(mockStmt).setString(4, "HELLO");
        verify(mockStmt).setDate(5, Date.valueOf("2024-01-31"));
        verify(mockStmt).setDate(6, Date.valueOf("2024-02-28"));
        verify(mockStmt).setTimestamp(7, Timestamp.valueOf("2024-03-20 14:23:56"));
        verify(mockStmt).setTimestamp(8, Timestamp.valueOf("2024-04-27 15:11:23"));
        verify(mockStmt).setInt(9, 123);

        // Assert
        assertEquals(mockStmt, stmt);
    }

    @Test
    void testParamTypeWithNull() throws SQLException {
        ParamSQL ps = new SQLUpdateBuilder()
                .from("some_db.some_table")
                .update("int_value", (Integer) null)
                .update("double_value", (Double) null)
                .update("long_value", (Long) null)
                .update("string_value", (String) null)
                .update("java_sql_date", (Date) null)
                .update("local_date", (LocalDate) null)
                .update("java_sql_timestamp", (Timestamp) null)
                .update("local_datetime", (LocalDateTime) null)
                .where(SQLCondition
                        .create("table_name.id = ?")
                        .and(SQLCondition.isNull("table_name.col2")))
                .param(SQLParameter.of(123))
                .toParamSQL();

        assertEquals("UPDATE some_db.some_table SET int_value=NULL,double_value=NULL,long_value=NULL,"
                + "string_value=NULL,java_sql_date=NULL,local_date=NULL,java_sql_timestamp=NULL,local_datetime=NULL "
                + "WHERE table_name.id = 123 AND table_name.col2 IS NULL ", ps.getDebugSQL());

        String expectedSQL = "UPDATE some_db.some_table SET int_value=?,double_value=?,long_value=?,"
                + "string_value=?,java_sql_date=?,local_date=?,java_sql_timestamp=?,local_datetime=? "
                + "WHERE table_name.id = ? AND table_name.col2 IS NULL ";
        assertEquals(expectedSQL, ps.getPreparedSQL());

        PreparedStatement stmt = ps.prepare(mockConn);

        // Check if those function in mocked object are called
        verify(mockConn).prepareStatement(expectedSQL);
        verify(mockStmt).setNull(1, Types.INTEGER);
        verify(mockStmt).setNull(2, Types.DOUBLE);
        verify(mockStmt).setNull(3, Types.BIGINT);
        verify(mockStmt).setString(4, null);
        verify(mockStmt).setDate(5, null);
        verify(mockStmt).setDate(6, null);
        verify(mockStmt).setTimestamp(7, null);
        verify(mockStmt).setTimestamp(8, null);
        verify(mockStmt).setInt(9, 123);

        // Assert
        assertEquals(mockStmt, stmt);
    }

    @Test
    void testIsValid() {
        SQLUpdateBuilder builder = new SQLUpdateBuilder()
                .update("col1", 456)
                .from("table_name")
                .where(SQLCondition.create("table_name.id = ?"))
                .param(SQLParameter.of(123));
        assertDoesNotThrow(builder::toParamSQL);
        assertDoesNotThrow(builder::toSQL);

        // ----------- Incorrect ------------
        SQLUpdateBuilder noFrom = new SQLUpdateBuilder()
                .update("col1", 456)
                .where(SQLCondition.create("table_name.id = ?"))
                .param(SQLParameter.of(123));
        assertThrows(IllegalStateException.class, noFrom::toParamSQL);
        assertThrows(IllegalStateException.class, noFrom::toSQL);

        SQLUpdateBuilder noCols = new SQLUpdateBuilder()
                .from("table_name")
                .where(SQLCondition.create("table_name.id = ?"))
                .param(SQLParameter.of(123));
        assertThrows(IllegalStateException.class, noCols::toParamSQL);
        assertThrows(IllegalStateException.class, noCols::toSQL);

        SQLUpdateBuilder noFromCols = new SQLUpdateBuilder()
                .where(SQLCondition.create("table_name.id = ?"))
                .param(SQLParameter.of(123));
        assertThrows(IllegalStateException.class, noFromCols::toParamSQL);
        assertThrows(IllegalStateException.class, noFromCols::toSQL);

        SQLUpdateBuilder noWhere = new SQLUpdateBuilder()
                .update("col1", 456)
                .from("table_name");
        assertThrows(IllegalStateException.class, noWhere::toParamSQL);
        assertThrows(IllegalStateException.class, noWhere::toSQL);
    }
}
