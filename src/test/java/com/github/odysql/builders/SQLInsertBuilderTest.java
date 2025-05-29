package com.github.odysql.builders;

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

class SQLInsertBuilderTest {

    private Connection mockConn;
    private PreparedStatement mockStmt;

    @BeforeEach
    void init() throws SQLException {
        mockConn = mock(Connection.class);
        mockStmt = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
    }

    @Test
    void testParamType() throws SQLException {
        SQLInsertBuilder b = new SQLInsertBuilder()
                .into("some_db.some_table")
                .insert("int_value", 123)
                .insert("double_value", 123.45)
                .insert("long_value", Long.valueOf(456))
                .insert("string_value", "HELLO")
                .insert("java_sql_date", Date.valueOf(LocalDate.of(2024, 1, 31)))
                .insert("local_date", LocalDate.of(2024, 2, 28))
                .insert("java_sql_timestamp",
                        Timestamp.valueOf(LocalDateTime.of(2024, 3, 20, 14, 23, 56)))
                .insert("local_datetime", LocalDateTime.of(2024, 04, 27, 15, 11, 23));

        ParamSQL ps = b.toParamSQL();

        assertEquals(
                "INSERT INTO some_db.some_table "
                        + "(int_value,double_value,long_value,string_value,java_sql_date,local_date,java_sql_timestamp,local_datetime) "
                        + "VALUES (123,123.45,456,'HELLO','2024-01-31','2024-02-28','2024-03-20 14:23:56','2024-04-27 15:11:23')",
                ps.getDebugSQL());

        String expectedSQL = "INSERT INTO some_db.some_table "
                + "(int_value,double_value,long_value,string_value,java_sql_date,local_date,java_sql_timestamp,local_datetime) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        assertEquals(expectedSQL, ps.getPreparedSQL());

        // Test statement
        PreparedStatement stmt = ps.prepare(mockConn);
        verify(mockConn).prepareStatement(expectedSQL);
        verify(mockStmt).setInt(1, 123);
        verify(mockStmt).setDouble(2, 123.45);
        verify(mockStmt).setLong(3, 456);
        verify(mockStmt).setString(4, "HELLO");
        verify(mockStmt).setDate(5, Date.valueOf(LocalDate.of(2024, 1, 31)));
        verify(mockStmt).setDate(6, Date.valueOf(LocalDate.of(2024, 2, 28)));
        verify(mockStmt).setTimestamp(7, Timestamp.valueOf(LocalDateTime.of(2024, 3, 20, 14, 23, 56)));
        verify(mockStmt).setTimestamp(8, Timestamp.valueOf(LocalDateTime.of(2024, 04, 27, 15, 11, 23)));

        assertEquals(mockStmt, stmt);
    }

    @Test
    void testParamTypeWithNull() throws SQLException {
        SQLInsertBuilder b = new SQLInsertBuilder()
                .into("some_db.some_table")
                .insert("int_value", (Integer) null)
                .insert("double_value", (Double) null)
                .insert("long_value", (Long) null)
                .insert("string_value", (String) null)
                .insert("java_sql_date", (Date) null)
                .insert("local_date", (LocalDate) null)
                .insert("java_sql_timestamp", (Timestamp) null)
                .insert("local_datetime", (LocalDateTime) null);

        ParamSQL ps = b.toParamSQL();

        assertEquals(
                "INSERT INTO some_db.some_table "
                        + "(int_value,double_value,long_value,string_value,java_sql_date,local_date,java_sql_timestamp,local_datetime) "
                        + "VALUES (NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL)",
                ps.getDebugSQL());

        String expectedSql = "INSERT INTO some_db.some_table "
                + "(int_value,double_value,long_value,string_value,java_sql_date,local_date,java_sql_timestamp,local_datetime) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        assertEquals(expectedSql, ps.getPreparedSQL());

        // Test statement
        PreparedStatement stmt = ps.prepare(mockConn);
        verify(mockConn).prepareStatement(expectedSql);
        verify(mockStmt).setNull(1, Types.INTEGER);
        verify(mockStmt).setNull(2, Types.DOUBLE);
        verify(mockStmt).setNull(3, Types.BIGINT);
        verify(mockStmt).setString(4, null);
        verify(mockStmt).setDate(5, null);
        verify(mockStmt).setDate(6, null);
        verify(mockStmt).setTimestamp(7, null);
        verify(mockStmt).setTimestamp(8, null);

        assertEquals(mockStmt, stmt);
    }

    @Test
    void testInsertIgnore() {
        SQLInsertBuilder b = new SQLInsertBuilder()
                .into("some_db.some_table")
                .insertIgnore()
                .insert("col1", "abc")
                .insert("col2", 123);

        ParamSQL ps = b.toParamSQL();

        assertEquals("INSERT IGNORE INTO some_db.some_table (col1,col2) VALUES (?,?)",
                ps.getPreparedSQL());
        assertEquals("INSERT IGNORE INTO some_db.some_table (col1,col2) VALUES ('abc',123)",
                ps.getDebugSQL());
    }

    @Test
    void testIsValid() {
        // Test normal case
        SQLInsertBuilder b = new SQLInsertBuilder()
                .into("some_db.some_table")
                .insert("col1", "abc")
                .insert("col2", 123);
        assertDoesNotThrow(b::toSQL);
        assertDoesNotThrow(b::toParamSQL);

        // Test no table name
        SQLInsertBuilder b1 = new SQLInsertBuilder()
                .insert("col1", "abc")
                .insert("col2", 123);
        assertThrows(IllegalStateException.class, b1::toSQL);
        assertThrows(IllegalStateException.class, b1::toParamSQL);

        // Test no column insert
        SQLInsertBuilder b2 = new SQLInsertBuilder()
                .into("some_db.some_table");
        assertThrows(IllegalStateException.class, b2::toSQL);
        assertThrows(IllegalStateException.class, b2::toParamSQL);
    }
}
