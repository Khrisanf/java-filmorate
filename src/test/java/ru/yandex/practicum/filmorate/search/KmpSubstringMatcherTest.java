package ru.yandex.practicum.filmorate.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KmpSubstringMatcherTest {

    private final KmpSubstringMatcher matcher = new KmpSubstringMatcher();

    @Test
    void contains_whenPatternAtBeginning_returnsTrue() {
        String text = "крадущийся тигр";
        String pattern = "крад";

        boolean result = matcher.contains(text, pattern);

        assertTrue(result);
    }

    @Test
    void contains_whenPatternInMiddle_returnsTrue() {
        String text = "тихий крадущийся тигр";
        String pattern = "крад";

        boolean result = matcher.contains(text, pattern);

        assertTrue(result);
    }

    @Test
    void contains_whenPatternAtEnd_returnsTrue() {
        String text = "тигр крад";
        String pattern = "крад";

        boolean result = matcher.contains(text, pattern);

        assertTrue(result);
    }

    @Test
    void contains_whenPatternNotInText_returnsFalse() {
        String text = "крадущийся тигр";
        String pattern = "дракон";

        boolean result = matcher.contains(text, pattern);

        assertFalse(result);
    }

    @Test
    void contains_whenPatternHasRepeats_returnsTrue() {
        String text = "абабабака";
        String pattern = "абаба";

        boolean result = matcher.contains(text, pattern);

        assertTrue(result);
    }

    @Test
    void contains_whenPatternLongerThanText_returnsFalse() {
        String text = "крад";
        String pattern = "крадущийся";

        boolean result = matcher.contains(text, pattern);

        assertFalse(result);
    }

    @Test
    void contains_whenTextNullOrEmpty_returnsFalse() {
        assertFalse(matcher.contains(null, "крад"));
        assertFalse(matcher.contains("", "крад"));
    }

    @Test
    void contains_whenPatternNullOrEmpty_returnsTrue() {
        assertTrue(matcher.contains("что угодно", ""));
        assertTrue(matcher.contains("что угодно", null));
    }

    @Test
    void contains_whenTextAndPatternHaveDifferentCase_returnsFalse() {
        String text = "Крадущийся тигр";
        String pattern = "крад";

        boolean result = matcher.contains(text, pattern);

        assertFalse(result); // algorithm depends on the register
    }

    @Test
    void contains_whenInputLowercasedExternally_returnsTrue() {
        String text = "Крадущийся тигр".toLowerCase();
        String pattern = "крад".toLowerCase();

        boolean result = matcher.contains(text, pattern);

        assertTrue(result);
    }
}
