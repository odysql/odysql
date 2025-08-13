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
}
