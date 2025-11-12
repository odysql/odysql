package io.github.odysql.builders.single;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.odysql.models.SQLCondition;
import io.github.odysql.models.SQLParameter;

class SQLDeleteBuilderTest {
    @Test
    void testBuilderNormal() {
        SQLDeleteBuilder builder = new SQLDeleteBuilder()
                .from("my_table")
                .where(SQLCondition.create("id = ?"))
                .param(SQLParameter.of(123));

        ParamSQL ps = builder.toParamSQL();

        assertEquals("DELETE FROM my_table WHERE id = 123", ps.getDebugSQL());
        assertEquals("DELETE FROM my_table WHERE id = ?", ps.getPreparedSQL());

        // ---------------------------------------

        SQLDeleteBuilder builder2 = new SQLDeleteBuilder()
                .from("my_table")
                .where(SQLCondition
                        .create("id = ?")
                        .orBracket(SQLCondition
                                .create("occupation = ?")
                                .and("age = 24")))
                .param(
                        SQLParameter.of(114514),
                        SQLParameter.of("student"));

        ParamSQL ps2 = builder2.toParamSQL();

        assertEquals(
                "DELETE FROM my_table WHERE id = 114514 OR (occupation = 'student' AND age = 24)",
                ps2.getDebugSQL());
        assertEquals(
                "DELETE FROM my_table WHERE id = ? OR (occupation = ? AND age = 24)",
                ps2.getPreparedSQL());
    }

    @Test
    void testInvalid() {
        SQLDeleteBuilder builder = new SQLDeleteBuilder()
                .where(SQLCondition.create("id = ?"))
                .param(SQLParameter.of(123));

        assertThrows(IllegalStateException.class, builder::toParamSQL);

        SQLDeleteBuilder builder2 = new SQLDeleteBuilder()
                .from("my_table");

        assertThrows(IllegalStateException.class, builder2::toParamSQL);
    }
}
