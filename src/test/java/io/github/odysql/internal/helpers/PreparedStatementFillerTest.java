package io.github.odysql.internal.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.odysql.models.SQLParameter;

class PreparedStatementFillerTest {
    @Test
    void testAsDebugSQLWithSpCharacter() {
        String sql = "INSERT INTO table (col1, col2) VALUES (?,?)";

        // Normal Parameter
        assertEquals("INSERT INTO table (col1, col2) VALUES ('ABCDEFG',1234567)",
                PreparedStatementFiller.asDebugSQL(sql, Arrays.asList(
                        SQLParameter.of("ABCDEFG"),
                        SQLParameter.of(1234567))));

        // Special Parameter
        assertEquals("INSERT INTO table (col1, col2) VALUES ('ABC  DEF',1234567)",
                PreparedStatementFiller.asDebugSQL(sql, Arrays.asList(
                        SQLParameter.of("ABC  DEF"),
                        SQLParameter.of(1234567))));

        // Special Parameter
        assertEquals("INSERT INTO table (col1, col2) VALUES ('ABC \100 DEF',1234567)",
                PreparedStatementFiller.asDebugSQL(sql, Arrays.asList(
                        SQLParameter.of("ABC \100 DEF"),
                        SQLParameter.of(1234567))));

        // Exceed
        List<SQLParameter> p1 = Arrays.asList(
                SQLParameter.of("ABCDEFG"),
                SQLParameter.of(1234567),
                SQLParameter.of(1234568));
        assertThrows(IllegalArgumentException.class, () -> PreparedStatementFiller.asDebugSQL(sql, p1));

        // Failed case
        List<SQLParameter> p2 = Arrays.asList(SQLParameter.of("ABCDEFG"));
        assertThrows(IllegalArgumentException.class, () -> PreparedStatementFiller.asDebugSQL(sql, p2));
    }

    @Test
    void testAsDebugSQLWithSpace() {
        assertEquals("INSERT INTO table (col1, col2, ' ') VALUES ('value1','value2')",
                PreparedStatementFiller.asDebugSQL(
                        "INSERT INTO table (col1, col2, ' ') VALUES (?,?)",
                        Arrays.asList(SQLParameter.of("value1"), SQLParameter.of("value2"))));

        assertEquals("INSERT INTO table (col1, col2) VALUES ('   ','value2')",
                PreparedStatementFiller.asDebugSQL(
                        "INSERT INTO table (col1, col2) VALUES (?,?)",
                        Arrays.asList(SQLParameter.of("   "), SQLParameter.of("value2"))));
    }

    @Test
    void testAsDebugSQLWithMultiLine() {
        // Demo java string text block
        String multiLine = "SELECT\n" +
                "    col1,\n" +
                "    col2,\n" +
                "    col3,\n" +
                "    col4,\n" +
                "    col5\n" +
                "FROM\n" +
                "    my_table as t1\n" +
                "WHERE\n" +
                "    pk = ?";

        assertEquals(
                "SELECT col1, col2, col3, col4, col5 FROM my_table as t1 WHERE pk = 123",
                PreparedStatementFiller.asDebugSQL(multiLine, Arrays.asList(SQLParameter.of(123))));

        // ----------------------------------

        // Text block with extra new line and indentation
        String textBlock = "\n\n" +
                "    SELECT\n" +
                "        col1,\n" +
                "        col2, \n\n" +
                "          col3,\n        \n" +
                "      col4,\n" +
                "        col5\n" +
                "    FROM\n" +
                "        my_table as t1\n\n\n" +
                "    WHERE\n" +
                "        pk = ?\n" +
                "    ";

        assertEquals(
                "SELECT col1, col2, col3, col4, col5 FROM my_table as t1 WHERE pk = 123",
                PreparedStatementFiller.asDebugSQL(textBlock, Arrays.asList(SQLParameter.of(123))));
    }
}
