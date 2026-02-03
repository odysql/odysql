package io.github.odysql.builders.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import io.github.odysql.models.SQLParameter;

class SQLBatchInsertBuilderTest {

    private Connection mockConn;
    private PreparedStatement mockStmt;

    @BeforeEach
    void init() throws SQLException {
        mockConn = mock(Connection.class);
        mockStmt = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);

        // To prevent null array stream
        when(mockStmt.executeBatch()).thenReturn(new int[] { 1, 1 });
    }

    @Test
    void testPlainSQL() {
        assertEquals(
                "INSERT INTO my_table (col1,col2,col3) VALUES (?,?,?)",
                new SQLBatchInsertBuilder<MyData>()
                        .into("my_table")
                        .insert("col1", item -> SQLParameter.of(item.getColumn1()))
                        .insert("col2", item -> SQLParameter.of(item.getColumn2()))
                        .insert("col3", item -> SQLParameter.of(String.valueOf(item.getColumn3())))
                        .toSQL());

        assertEquals(
                "INSERT IGNORE INTO my_table (col1,col2,col3) VALUES (?,?,?)",
                new SQLBatchInsertBuilder<MyData>()
                        .into("my_table")
                        .insertIgnore()
                        .insert("col1", item -> SQLParameter.of(item.getColumn1()))
                        .insert("col2", item -> SQLParameter.of(item.getColumn2()))
                        .insert("col3", item -> SQLParameter.of(String.valueOf(item.getColumn3())))
                        .toSQL());
    }

    @Test
    void testOnDuplicateKeyUpdateSQL() {
        assertEquals(
                "INSERT INTO my_table (col1,col2,col3) VALUES (?,?,?) ON DUPLICATE KEY UPDATE col2=VALUES(col2),col3=VALUES(col3)",
                new SQLBatchInsertBuilder<MyData>()
                        .into("my_table")
                        .insert("col1", item -> SQLParameter.of(item.getColumn1()))
                        .insert("col2", item -> SQLParameter.of(item.getColumn2()))
                        .onDuplicateKeyUpdate("col2")
                        .insert("col3", item -> SQLParameter.of(String.valueOf(item.getColumn3())))
                        .onDuplicateKeyUpdate("col3")
                        .toSQL());

        // Multiple columns version
        assertEquals(
                "INSERT INTO my_table (col1,col2,col3) VALUES (?,?,?) ON DUPLICATE KEY UPDATE col2=VALUES(col2),col3=VALUES(col3),col1=VALUES(col1)",
                new SQLBatchInsertBuilder<MyData>()
                        .into("my_table")
                        .insert("col1", item -> SQLParameter.of(item.getColumn1()))
                        .insert("col2", item -> SQLParameter.of(item.getColumn2()))
                        .insert("col3", item -> SQLParameter.of(String.valueOf(item.getColumn3())))
                        .onDuplicateKeyUpdate("col2", "col3", "col1")
                        .toSQL());

        // Short hand version
        assertEquals(
                "INSERT INTO my_table (col1,col2,col3) VALUES (?,?,?) ON DUPLICATE KEY UPDATE col2=VALUES(col2),col3=VALUES(col3)",
                new SQLBatchInsertBuilder<MyData>()
                        .into("my_table")
                        .insert("col1", item -> SQLParameter.of(item.getColumn1()))
                        .insertOnDuplicateUpdate("col2", item -> SQLParameter.of(item.getColumn2()))
                        .insertOnDuplicateUpdate("col3", item -> SQLParameter.of(String.valueOf(item.getColumn3())))
                        .toSQL());

        // Invalid builder
        SQLBatchInsertBuilder<MyData> invalid = new SQLBatchInsertBuilder<MyData>()
                .into("my_table")
                .insertIgnore()
                .insert("col1", item -> SQLParameter.of(item.getColumn1()))
                .insert("col2", item -> SQLParameter.of(item.getColumn2()))
                .onDuplicateKeyUpdate("col2");

        assertThrows(IllegalStateException.class, invalid::toSQL);
    }

    @Test
    void testBatchRunner() throws SQLException {
        // Prepare dummy data
        List<MyData> data = Arrays.asList(
                new MyData("abc", 456, 'a'),
                new MyData("def", 123, 'b'));

        SQLBatchInsertRunner<MyData> runner = new SQLBatchInsertBuilder<MyData>()
                .into("my_table")
                .insert("col1", item -> SQLParameter.of(item.getColumn1()))
                .insert("col2", item -> SQLParameter.of(item.getColumn2()))
                .insert("col3", item -> SQLParameter.of(String.valueOf(item.getColumn3())))
                .toBatchRunner()
                .setLogEnabled(true)
                .setData(data);

        int sum = runner.executeWith(mockConn);
        assertEquals(2, sum);

        // Verify debug logs
        assertEquals(
                "INSERT INTO my_table (col1,col2,col3) VALUES('abc',456,'a'),('def',123,'b')",
                runner.getDebugSQL().get(0));

        // Check behavior by mock
        verify(mockConn).prepareStatement("INSERT INTO my_table (col1,col2,col3) VALUES  (?,?,?)");

        InOrder o = inOrder(mockStmt);
        o.verify(mockStmt).setString(1, "abc");
        o.verify(mockStmt).setInt(2, 456);
        o.verify(mockStmt).setString(3, "a");
        o.verify(mockStmt).addBatch();
        o.verify(mockStmt).setString(1, "def");
        o.verify(mockStmt).setInt(2, 123);
        o.verify(mockStmt).setString(3, "b");
        o.verify(mockStmt).addBatch();
        o.verify(mockStmt).executeBatch();
    }

    @Test
    void testBatchRunnerWithSize() throws SQLException {
        // Prepare dummy data
        List<MyData> data = Arrays.asList(
                new MyData("abc", 456, 'a'),
                new MyData("def", 123, 'b'));

        SQLBatchInsertRunner<MyData> runner = new SQLBatchInsertBuilder<MyData>()
                .into("my_table")
                .insert("col1", item -> SQLParameter.of(item.getColumn1()))
                .insert("col2", item -> SQLParameter.of(item.getColumn2()))
                .insert("col3", item -> SQLParameter.of(String.valueOf(item.getColumn3())))
                .toBatchRunner()
                .setData(data)
                .setMaxBatchSize(1)
                .setLogEnabled(true);

        runner.executeWith(mockConn);

        // Verify debug logs
        assertEquals(
                "INSERT INTO my_table (col1,col2,col3) VALUES('abc',456,'a')",
                runner.getDebugSQL().get(0));
        assertEquals(
                "INSERT INTO my_table (col1,col2,col3) VALUES('def',123,'b')",
                runner.getDebugSQL().get(1));

        // Check behavior by mock
        verify(mockConn).prepareStatement("INSERT INTO my_table (col1,col2,col3) VALUES  (?,?,?)");

        InOrder o = inOrder(mockStmt);

        o.verify(mockStmt).setString(1, "abc");
        o.verify(mockStmt).setInt(2, 456);
        o.verify(mockStmt).setString(3, "a");
        o.verify(mockStmt).addBatch();
        o.verify(mockStmt).executeBatch();

        o.verify(mockStmt).setString(1, "def");
        o.verify(mockStmt).setInt(2, 123);
        o.verify(mockStmt).setString(3, "b");
        o.verify(mockStmt).addBatch();
        o.verify(mockStmt).executeBatch();
    }

    @Test
    void testBatchRunnerWithOnDuplicateKeyUpdate() throws SQLException {
        // Prepare dummy data
        List<MyData> data = Arrays.asList(
                new MyData("abc", 456, 'a'),
                new MyData("def", 123, 'b'));

        SQLBatchInsertRunner<MyData> runner = new SQLBatchInsertBuilder<MyData>()
                .into("my_table")
                .insert("col1", item -> SQLParameter.of(item.getColumn1()))
                .insert("col2", item -> SQLParameter.of(item.getColumn2()))
                .onDuplicateKeyUpdate("col2")
                .insert("col3", item -> SQLParameter.of(String.valueOf(item.getColumn3())))
                .onDuplicateKeyUpdate("col3")
                .toBatchRunner()
                .setData(data)
                .setLogEnabled(true);

        runner.executeWith(mockConn);

        // Verify debug logs
        assertEquals(
                "INSERT INTO my_table (col1,col2,col3) VALUES('abc',456,'a'),('def',123,'b')  ON DUPLICATE KEY UPDATE col2=VALUES(col2),col3=VALUES(col3)",
                runner.getDebugSQL().get(0));

        // Check behavior by mock
        verify(mockConn)
                .prepareStatement(
                        "INSERT INTO my_table (col1,col2,col3) VALUES  (?,?,?)  ON DUPLICATE KEY UPDATE col2=VALUES(col2),col3=VALUES(col3)");

        InOrder o = inOrder(mockStmt);

        o.verify(mockStmt).setString(1, "abc");
        o.verify(mockStmt).setInt(2, 456);
        o.verify(mockStmt).setString(3, "a");
        o.verify(mockStmt).addBatch();

        o.verify(mockStmt).setString(1, "def");
        o.verify(mockStmt).setInt(2, 123);
        o.verify(mockStmt).setString(3, "b");
        o.verify(mockStmt).addBatch();

        o.verify(mockStmt).executeBatch();
    }
}
