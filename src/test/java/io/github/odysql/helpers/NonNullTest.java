package io.github.odysql.helpers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class NonNullTest {
    @Test
    void testSafeStr() {
        // Null Handling
        assertEquals("", NonNull.safeStr(null));

        // Normal case
        assertEquals("some  words", NonNull.safeStr("some  words"));

        // Space trimmed
        assertEquals("some  words", NonNull.safeStr("   some  words"));
        assertEquals("some  words", NonNull.safeStr("some  words   "));
    }

    @Test
    void testIsEmptyString() {
        // Normal Case
        assertEquals(false, NonNull.isEmpty("abc"));
        assertEquals(false, NonNull.isEmpty(" abc "));

        // Empty string
        assertEquals(true, NonNull.isEmpty("      "));
        assertEquals(true, NonNull.isEmpty(""));

        // Null value
        assertEquals(true, NonNull.isEmpty((String) null));
    }

    @Test
    void testIsEmptyCollection() {
        List<String> empty = new ArrayList<>();
        List<String> nullList = null;
        List<String> something = new ArrayList<>();
        something.add("abc");
        something.add("def");

        // Normal Case
        assertEquals(false, NonNull.isEmpty(something));

        // Empty list
        assertEquals(true, NonNull.isEmpty(empty));

        // Null List
        assertEquals(true, NonNull.isEmpty(nullList));
    }
}
