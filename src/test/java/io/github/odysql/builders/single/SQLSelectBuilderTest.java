package io.github.odysql.builders.single;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.odysql.models.SQLCondition;
import io.github.odysql.models.SQLParameter;

class SQLSelectBuilderTest {

    @Test
    void testBuilderNormal() {
        SQLSelectBuilder builder = new SQLSelectBuilder()
                .select("field1")
                .select("field2")
                .distinct()
                .from("table1")
                .innerJoin("table2", SQLCondition.create("table1.id = table2.id"))
                .leftJoin("table3", SQLCondition.create("table2.value = table3.value"))
                .where(SQLCondition.create("table1.id = 123"))
                .groupBy("field1")
                .having(SQLCondition.create("COUNT(*) > 5"))
                .orderBy("field1")
                .limit(1);

        ParamSQL ps = builder.toParamSQL();

        assertEquals(
                "SELECT DISTINCT field1, field2 FROM table1 "
                        + "INNER JOIN table2 ON table1.id = table2.id "
                        + "LEFT JOIN table3 ON table2.value = table3.value "
                        + "WHERE table1.id = 123 "
                        + "GROUP BY field1 "
                        + "HAVING COUNT(*) > 5 "
                        + "ORDER BY field1 LIMIT 1",
                ps.getPreparedSQL());
    }

    @Test
    void testBuilderPrepare() {
        SQLSelectBuilder builder = new SQLSelectBuilder()
                .select("field1")
                .select("field2")
                .distinct()
                .from("table1")
                .leftJoin("table2", SQLCondition.create("table1.id = table2.id"))
                .where(SQLCondition
                        .create("table1.id = ? AND table1.name = ?"))
                .param(
                        SQLParameter.of(123),
                        SQLParameter.of("abc"))
                .orderBy("field1")
                .limitOffset(1, 10);

        ParamSQL ps = builder.toParamSQL();

        assertEquals("SELECT DISTINCT field1, field2 FROM table1 "
                + "LEFT JOIN table2 ON table1.id = table2.id "
                + "WHERE table1.id = 123 AND table1.name = 'abc' ORDER BY field1 LIMIT 1 OFFSET 10",
                ps.getDebugSQL());

        assertEquals("SELECT DISTINCT field1, field2 FROM table1 "
                + "LEFT JOIN table2 ON table1.id = table2.id "
                + "WHERE table1.id = ? AND table1.name = ? ORDER BY field1 LIMIT 1 OFFSET 10",
                ps.getPreparedSQL());

        // -------------------------------------

        SQLSelectBuilder builder2 = new SQLSelectBuilder()
                .select("field1")
                .select("field2")
                .distinct()
                .from("table1")
                .leftJoin("table2", SQLCondition.create("table1.id = table2.id"))
                .where(SQLCondition
                        .create("table1.id = ?")
                        .and("table1.name = ?"))
                .param(
                        SQLParameter.of(123),
                        SQLParameter.of("abc"))
                .orderBy("field1")
                .limit(1);

        ParamSQL ps2 = builder2.toParamSQL();

        assertEquals("SELECT DISTINCT field1, field2 FROM table1 "
                + "LEFT JOIN table2 ON table1.id = table2.id "
                + "WHERE table1.id = 123 AND table1.name = 'abc' ORDER BY field1 LIMIT 1",
                ps2.getDebugSQL());

        assertEquals("SELECT DISTINCT field1, field2 FROM table1 "
                + "LEFT JOIN table2 ON table1.id = table2.id "
                + "WHERE table1.id = ? AND table1.name = ? ORDER BY field1 LIMIT 1",
                ps2.getPreparedSQL());
    }

    @Test
    void testBuilderWith() {
        SQLSelectBuilder builder = new SQLSelectBuilder()
                .with("tmp", new SQLSelectBuilder()
                        .select("col_wq")
                        .from("w1")
                        .where(SQLCondition.create("w1.id = ?"))
                        .param(SQLParameter.of(123))
                        .limitOffset(20, 10))

                .select("field1")
                .select("field2")
                .distinct()

                .from("table1")
                .leftJoin("table2", SQLCondition.create("table1.id = table2.id"))
                .where(SQLCondition.create("table1.id = ? AND table1.name = ?"))
                .param(SQLParameter.of(456), SQLParameter.of("def"))
                .orderBy("field1")
                .limit(1);

        ParamSQL ps = builder.toParamSQL();

        assertEquals("WITH tmp AS (SELECT col_wq FROM w1 WHERE w1.id = ? LIMIT 20 OFFSET 10) "
                + "SELECT DISTINCT field1, field2 FROM table1 "
                + "LEFT JOIN table2 ON table1.id = table2.id "
                + "WHERE table1.id = ? AND table1.name = ? ORDER BY field1 LIMIT 1",
                ps.getPreparedSQL());

        assertEquals("WITH tmp AS (SELECT col_wq FROM w1 WHERE w1.id = 123 LIMIT 20 OFFSET 10) "
                + "SELECT DISTINCT field1, field2 FROM table1 "
                + "LEFT JOIN table2 ON table1.id = table2.id "
                + "WHERE table1.id = 456 AND table1.name = 'def' ORDER BY field1 LIMIT 1",
                ps.getDebugSQL());
    }

    @Test
    void testBuilderMultipleWith() {
        SQLSelectBuilder builder = new SQLSelectBuilder()
                .with("tmp", new SQLSelectBuilder()
                        .select("col_wq")
                        .from("w1")
                        .where(SQLCondition.create("w1.id = ?"))
                        .param(SQLParameter.of(123))
                        .limit(20))

                .with("tmp2", new SQLSelectBuilder()
                        .select("col_wq")
                        .from("w2")
                        .where(SQLCondition.create("w2.name = ?"))
                        .param(SQLParameter.of("abc"))
                        .limit(20))

                .select("field1")
                .select("field2")
                .distinct()
                .from("table1")
                .leftJoin("table2", SQLCondition.create("table1.id = table2.id"))
                .where(SQLCondition.create("table1.id = ? AND table1.name = ?"))
                .param(SQLParameter.of(456), SQLParameter.of("def"))
                .orderBy("field1")
                .limit(1);

        ParamSQL ps = builder.toParamSQL();

        assertEquals("WITH tmp AS (SELECT col_wq FROM w1 WHERE w1.id = ? LIMIT 20), "
                + "tmp2 AS (SELECT col_wq FROM w2 WHERE w2.name = ? LIMIT 20) "
                + "SELECT DISTINCT field1, field2 FROM table1 "
                + "LEFT JOIN table2 ON table1.id = table2.id "
                + "WHERE table1.id = ? AND table1.name = ? ORDER BY field1 LIMIT 1",
                ps.getPreparedSQL());

        assertEquals("WITH tmp AS (SELECT col_wq FROM w1 WHERE w1.id = 123 LIMIT 20), "
                + "tmp2 AS (SELECT col_wq FROM w2 WHERE w2.name = 'abc' LIMIT 20) "
                + "SELECT DISTINCT field1, field2 FROM table1 "
                + "LEFT JOIN table2 ON table1.id = table2.id "
                + "WHERE table1.id = 456 AND table1.name = 'def' ORDER BY field1 LIMIT 1",
                ps.getDebugSQL());
    }

    @Test
    void testBuilderNestedWith() {
        SQLSelectBuilder builder = new SQLSelectBuilder()
                .with("tmp2", new SQLSelectBuilder()
                        .with("tmp1", new SQLSelectBuilder()
                                .select("col_wq")
                                .from("w1")
                                .where(SQLCondition.create("w1.id = ?"))
                                .param(SQLParameter.of(123))
                                .limit(20))

                        .select("col_wq")
                        .from("w2")
                        .where(SQLCondition.create("w2.name = ?"))
                        .param(SQLParameter.of("abc"))
                        .limit(20))

                .select("field1")
                .select("field2")
                .distinct()
                .from("table1")
                .leftJoin("table2", SQLCondition.create("table1.id = table2.id"))
                .where(SQLCondition.create("table1.id = ? AND table1.name = ?"))
                .param(SQLParameter.of((long) 456), SQLParameter.of("def"))
                .orderBy("field1")
                .limit(1);

        ParamSQL ps = builder.toParamSQL();

        assertEquals("WITH tmp2 AS ("
                + "WITH tmp1 AS (SELECT col_wq FROM w1 WHERE w1.id = ? LIMIT 20) "
                + "SELECT col_wq FROM w2 WHERE w2.name = ? LIMIT 20) "
                + "SELECT DISTINCT field1, field2 FROM table1 "
                + "LEFT JOIN table2 ON table1.id = table2.id "
                + "WHERE table1.id = ? AND table1.name = ? ORDER BY field1 LIMIT 1",
                ps.getPreparedSQL());

        assertEquals("WITH tmp2 AS ("
                + "WITH tmp1 AS (SELECT col_wq FROM w1 WHERE w1.id = 123 LIMIT 20) "
                + "SELECT col_wq FROM w2 WHERE w2.name = 'abc' LIMIT 20) "
                + "SELECT DISTINCT field1, field2 FROM table1 "
                + "LEFT JOIN table2 ON table1.id = table2.id "
                + "WHERE table1.id = 456 AND table1.name = 'def' ORDER BY field1 LIMIT 1",
                ps.getDebugSQL());
    }
}
