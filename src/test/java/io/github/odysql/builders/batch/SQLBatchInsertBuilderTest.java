package io.github.odysql.builders.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void testBatchRunner() throws SQLException {
        // Prepare dummy data
        List<MyData> data = Arrays.asList(
                new MyData("abc", 456, 'a'),
                new MyData("def", 123, 'b'));

        int sum = new SQLBatchInsertBuilder<MyData>()
                .into("my_table")
                .insert("col1", item -> SQLParameter.of(item.getColumn1()))
                .insert("col2", item -> SQLParameter.of(item.getColumn2()))
                .insert("col3", item -> SQLParameter.of(String.valueOf(item.getColumn3())))
                .toBatchRunner()
                .setData(data)
                .executeWith(mockConn);
        assertEquals(2, sum);

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

        new SQLBatchInsertBuilder<MyData>()
                .into("my_table")
                .insert("col1", item -> SQLParameter.of(item.getColumn1()))
                .insert("col2", item -> SQLParameter.of(item.getColumn2()))
                .insert("col3", item -> SQLParameter.of(String.valueOf(item.getColumn3())))
                .toBatchRunner()
                .setData(data)
                .setMaxBatchSize(1)
                .executeWith(mockConn);

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
}
