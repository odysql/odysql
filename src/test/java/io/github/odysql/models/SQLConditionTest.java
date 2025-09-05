package io.github.odysql.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class SQLConditionTest {

    static <T> List<T> createList(T[] items) {
        List<T> list = new ArrayList<>();

        for (T item : items) {
            list.add(item);
        }

        return list;
    }

    @Test
    void testIsEmpty() {
        assertEquals(true, SQLCondition.isEmpty(null));
        assertEquals(true, SQLCondition.isEmpty(SQLCondition.create(" ")));
        assertEquals(true, SQLCondition.isEmpty(SQLCondition.create("")));

        assertEquals(false, SQLCondition.isEmpty(SQLCondition.create("col1 = ?")));
    }

    @Test
    void testBracket() {
        assertEquals("(col1 = 12)", SQLCondition.bracket(SQLCondition.create("col1 = 12")).asSQL());

        // AND bracket, OR Bracket ----------------------------------------------------
        assertEquals("col1 = 12 AND (col2 = '23' AND col3 = 'ABC')",
                SQLCondition
                        .create("col1 = 12")
                        .andBracket(SQLCondition
                                .create("col2 = '23'")
                                .and("col3 = 'ABC'"))
                        .asSQL());

        assertEquals("col1 = 12 OR (col2 = '23' AND col3 = 'ABC')",
                SQLCondition
                        .create("col1 = 12")
                        .orBracket(SQLCondition
                                .create("col2 = '23'")
                                .and("col3 = 'ABC'"))
                        .asSQL());

        // Nested AND bracket, OR Bracket ----------------------------------------------
        assertEquals("col1 = 12 OR (col2 = '23' AND (col3 = 'ABC' AND col6 LIKE '%A%'))",
                SQLCondition
                        .create("col1 = 12")
                        .orBracket(SQLCondition
                                .create("col2 = '23'")
                                .andBracket(SQLCondition
                                        .create("col3 = 'ABC'")
                                        .and(SQLCondition.pickByFlag(
                                                false,
                                                SQLCondition.create("col5 <> 100"),
                                                SQLCondition.create("col6 LIKE '%A%'")))))
                        .asSQL());
    }

    @Test
    void testAnd() {
        // Test SQLCondition.and() in four possibility

        // Both Non-empty
        assertEquals(
                "col1 = 12 AND col2 = '23'",
                SQLCondition.create("col1 = 12").and("col2 = '23'").asSQL());

        // Any one empty
        assertEquals(
                "col1 = 12",
                SQLCondition.create("col1 = 12").and(SQLCondition.empty()).asSQL());
        assertEquals(
                "col2 = '23'",
                SQLCondition.empty().and("col2 = '23'").asSQL());

        // Both are empty
        assertEquals(
                "",
                SQLCondition.empty().and(SQLCondition.empty()).asSQL());

        // -------------------------------------------------

        // Special case for empty SQLCondition created manually
        assertEquals(
                "col1 = 12",
                SQLCondition.create("col1 = 12").and(SQLCondition.create("")).asSQL());

        assertEquals(
                "col1 = 12",
                SQLCondition.create("col1 = 12").and(SQLCondition.create("   ")).asSQL());
    }

    @Test
    void testOr() {
        // Test SQLCondition.or() in four possibility

        // Both Non-empty
        assertEquals(
                "col1 = 12 OR col2 = '23'",
                SQLCondition.create("col1 = 12").or("col2 = '23'").asSQL());

        // Any one empty
        assertEquals(
                "col1 = 12",
                SQLCondition.create("col1 = 12").or(SQLCondition.empty()).asSQL());
        assertEquals(
                "col2 = '23'",
                SQLCondition.empty().or("col2 = '23'").asSQL());

        // Both are empty
        assertEquals(
                "",
                SQLCondition.empty().or(SQLCondition.empty()).asSQL());

        // -------------------------------------------------

        // Special case for empty SQLCondition created manually
        assertEquals(
                "col1 = 12",
                SQLCondition.create("col1 = 12").or(SQLCondition.create("")).asSQL());
        assertEquals(
                "col1 = 12",
                SQLCondition.create("col1 = 12").or(SQLCondition.create("   ")).asSQL());
    }

    @Test
    void testPickByFlag() {
        SQLCondition c1 = SQLCondition.create("col1 = 12");
        SQLCondition c2 = SQLCondition.create("col2 = '23'");

        assertEquals(c1, SQLCondition.pickByFlag(true, c1, c2));
        assertEquals(c2, SQLCondition.pickByFlag(false, c1, c2));
        assertEquals(SQLCondition.empty(), SQLCondition.pickByFlag(null, c1, c2));
    }

    @Test
    void testIn() {
        assertEquals(
                "col1 IN ('1','2','3')",
                SQLCondition.in("col1", createList(new String[] { "1", "2", "3" })).asSQL());

        assertEquals(
                "col1 IN ('1')",
                SQLCondition.in("col1", createList(new String[] { "1" })).asSQL());

        assertEquals(
                "col1 IN (1,2,3)",
                SQLCondition.in("col1", createList(new Integer[] { 1, 2, 3 })).asSQL());

        assertEquals(
                "col1 IN (1)",
                SQLCondition.in("col1", createList(new Integer[] { 1 })).asSQL());

        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> SQLCondition.in("col1", createList(new Double[] { 1.0 })));

        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> SQLCondition.in("col1", new ArrayList<String>()));
    }

    @Test
    void testInPlaceholders() {
        assertEquals(
                "col1 IN (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                SQLCondition.inPlaceholders("col1", 23).asSQL());

        assertEquals(
                "col1 IN (?,?,?)",
                SQLCondition.inPlaceholders("col1", 3).asSQL());

        assertEquals(
                "col1 IN (?)",
                SQLCondition.inPlaceholders("col1", 1).asSQL());

        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> SQLCondition.inPlaceholders("col1", 0));

        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> SQLCondition.inPlaceholders("col1", -1));
    }

    @Test
    void testNotInPlaceholders() {
        assertEquals(
                "col1 NOT IN (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                SQLCondition.notInPlaceholders("col1", 23).asSQL());

        assertEquals(
                "col1 NOT IN (?,?,?)",
                SQLCondition.notInPlaceholders("col1", 3).asSQL());

        assertEquals(
                "col1 NOT IN (?)",
                SQLCondition.notInPlaceholders("col1", 1).asSQL());

        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> SQLCondition.notInPlaceholders("col1", 0));

        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> SQLCondition.notInPlaceholders("col1", -1));
    }

    @Test
    void testNotIn() {
        assertEquals(
                "col1 NOT IN ('1','2','3')",
                SQLCondition.notIn("col1", createList(new String[] { "1", "2", "3" })).asSQL());

        assertEquals(
                "col1 NOT IN ('1')",
                SQLCondition.notIn("col1", createList(new String[] { "1" })).asSQL());

        assertEquals(
                "col1 NOT IN (1,2,3)",
                SQLCondition.notIn("col1", createList(new Integer[] { 1, 2, 3 })).asSQL());

        assertEquals(
                "col1 NOT IN (1)",
                SQLCondition.notIn("col1", createList(new Integer[] { 1 })).asSQL());

        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> SQLCondition.notIn("col1", createList(new Double[] { 1.0 })));

        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> SQLCondition.notIn("col1", new ArrayList<String>()));
    }

    @Test
    void testEqTo() {
        assertEquals("col1||col3 = col2||col4", SQLCondition.eqTo("col1||col3", "col2||col4").asSQL());

        assertEquals(
                "col1||col3 = 'a very long text that may be needed to handle'",
                SQLCondition.eqTo("col1||col3", "'a very long text that may be needed to handle'")
                        .asSQL());

        assertThrows(IllegalArgumentException.class, () -> SQLCondition.eqTo("col1", "?"));
        assertThrows(IllegalArgumentException.class, () -> SQLCondition.eqTo("?", "col2"));
        assertThrows(IllegalArgumentException.class, () -> SQLCondition.eqTo("?", "?"));
    }
}
