package ru.yandex.practicum.filmorate.search;

public interface SubstringMatcher {
    /**
     * Returns true if pattern contains text like substring
     */
    boolean contains(String text, String pattern);
}
