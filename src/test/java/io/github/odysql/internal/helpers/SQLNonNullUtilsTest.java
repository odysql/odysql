package io.github.odysql.internal.helpers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class SQLNonNullUtilsTest {
    @Test
    void testSafeStr() {
        // Null Handling
        assertEquals("", SQLNonNullUtils.safeStr(null));

        // Normal case
        assertEquals("some  words", SQLNonNullUtils.safeStr("some  words"));

        // Space trimmed
        assertEquals("some  words", SQLNonNullUtils.safeStr("   some  words"));
        assertEquals("some  words", SQLNonNullUtils.safeStr("some  words   "));
    }

    @Test
    void testIsEmptyString() {
        // Normal Case
        assertEquals(false, SQLNonNullUtils.isEmpty("abc"));
        assertEquals(false, SQLNonNullUtils.isEmpty(" abc "));

        // Empty string
        assertEquals(true, SQLNonNullUtils.isEmpty("      "));
        assertEquals(true, SQLNonNullUtils.isEmpty(""));

        // Null value
        assertEquals(true, SQLNonNullUtils.isEmpty((String) null));
    }

    @Test
    void testIsEmptyCollection() {
        List<String> empty = new ArrayList<>();
        List<String> nullList = null;
        List<String> something = new ArrayList<>();
        something.add("abc");
        something.add("def");

        // Normal Case
        assertEquals(false, SQLNonNullUtils.isEmpty(something));

        // Empty list
        assertEquals(true, SQLNonNullUtils.isEmpty(empty));

        // Null List
        assertEquals(true, SQLNonNullUtils.isEmpty(nullList));
    }
}
