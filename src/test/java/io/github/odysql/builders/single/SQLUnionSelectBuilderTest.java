package io.github.odysql.builders.single;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import io.github.odysql.models.SQLCondition;
import io.github.odysql.models.SQLParameter;

class SQLUnionSelectBuilderTest {
    @Test
    void testBuilder() {
        SQLUnionSelectBuilder b = SQLUnionSelectBuilder
                .create(new SQLSelectBuilder()
                        .select("col1 AS some_data")
                        .from("table1")
                        .where(SQLCondition.create("id_col = ?"))
                        .param(SQLParameter.of(123)))

                .union(new SQLSelectBuilder()
                        .select("col2 AS some_data")
                        .from("table2")
                        .where(SQLCondition.create("id_col = ?"))
                        .param(SQLParameter.of(456)))

                .unionAll(new SQLSelectBuilder()
                        .select("col3 AS some_data")
                        .from("table3")
                        .where(SQLCondition.create("col_name = ?"))
                        .param(SQLParameter.of("hello")))

                .orderBy("some_data");

        assertEquals(
                "SELECT col1 AS some_data FROM table1 WHERE id_col = ? UNION SELECT col2 AS some_data FROM table2 WHERE id_col = ? UNION ALL SELECT col3 AS some_data FROM table3 WHERE col_name = ? ORDER BY some_data",
                b.toParamSQL().getPreparedSQL());

        assertEquals(
                "SELECT col1 AS some_data FROM table1 WHERE id_col = ? UNION SELECT col2 AS some_data FROM table2 WHERE id_col = ? UNION ALL SELECT col3 AS some_data FROM table3 WHERE col_name = ? ORDER BY some_data",
                b.toSQL());

        assertEquals(
                Arrays.asList(SQLParameter.of(123), SQLParameter.of(456), SQLParameter.of("hello")),
                b.getParams());

    }

    @Test
    void testInvalid() {
        SQLUnionSelectBuilder invalid = SQLUnionSelectBuilder
                .create(new SQLSelectBuilder()
                        .select("col1")
                        .from("table1")
                        .where(SQLCondition.create("id_col = ?"))
                        .param(SQLParameter.of(123)));

        assertThrows(IllegalStateException.class, invalid::toSQL);
    }
}
