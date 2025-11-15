package ru.yandex.practicum.filmorate.search;

import org.springframework.stereotype.Component;

/**
 * algorithm for searching a substring in a string.
 */

@Component
public class KmpSubstringMatcher implements SubstringMatcher {
    @Override
    public boolean contains(String text, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return true;
        }
        if (text == null || text.isEmpty()) {
            return false;
        }
        if (pattern.length() > text.length()) {
            return false;
        }

        // pi-func
        int[] pi = buildPrefixFunction(pattern);
        int j = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            while (j > 0 && c != pattern.charAt(j)) {
                j = pi[j - 1];
            }

            if (c == pattern.charAt(j)) {
                j++;
                if (j == pattern.length()) {
                    return true;
                }
            }
        }

        return false;
    }

    private int[] buildPrefixFunction(String pattern) {
        int m = pattern.length();
        int[] pi = new int[m];
        pi[0] = 0;
        int j = 0;

        for (int i = 1; i < m; i++) {
            char c = pattern.charAt(i);

            while (j > 0 && c != pattern.charAt(j)) {
                j = pi[j - 1];
            }

            if (c == pattern.charAt(j)) {
                j++;
            }

            pi[i] = j;
        }

        return pi;
    }
}
