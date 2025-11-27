package ru.yandex.practicum.filmorate.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.SearchBy;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest
class FilmDbStorageSearchTest {

    @Autowired
    private FilmStorage filmStorage;

    @Test
    @Sql(scripts = "/film-search-three-films.sql", executionPhase = BEFORE_TEST_METHOD)
    void search_whenSeveralFilmsMatch_sortsByPopularityDesc() {
        String query = "крад";
        Set<SearchBy> searchBy = EnumSet.of(SearchBy.TITLE);

        List<Film> result = filmStorage.search(query, searchBy);

        assertEquals(3, result.size());
        List<Long> idsInOrder = result.stream().map(Film::getId).toList();
        assertEquals(List.of(2L, 3L, 1L), idsInOrder);
    }

    @Test
    @Sql(scripts = "/film-search-two-films.sql", executionPhase = BEFORE_TEST_METHOD)
    void search_whenQueryMatchesTitle_returnsMatchingFilms() {
        String query = "крад";
        Set<SearchBy> searchBy = EnumSet.of(SearchBy.TITLE);

        List<Film> result = filmStorage.search(query, searchBy);

        assertEquals(2, result.size());
        List<Long> ids = result.stream().map(Film::getId).toList();
        assertEquals(List.of(1L, 2L), ids);
    }

    @Test
    void search_whenQueryEmpty_returnsEmptyList() {
        String query = "";
        Set<SearchBy> searchBy = EnumSet.of(SearchBy.TITLE);

        List<Film> result = filmStorage.search(query, searchBy);

        assertEquals(0, result.size());
    }

    @Test
    @Sql(scripts = "/film-search-two-films.sql", executionPhase = BEFORE_TEST_METHOD)
    void search_whenNoFilmsMatch_returnsEmptyList() {
        String query = "марсианские";
        Set<SearchBy> searchBy = EnumSet.of(SearchBy.TITLE);

        List<Film> result = filmStorage.search(query, searchBy);

        assertEquals(0, result.size());
    }

    @Test
    @Sql(scripts = "/film-search-two-films.sql", executionPhase = BEFORE_TEST_METHOD)
    void search_whenQueryAndTitleHaveDifferentCase_ignoresCase() {
        String query = "КрАд";
        Set<SearchBy> searchBy = EnumSet.of(SearchBy.TITLE);

        List<Film> result = filmStorage.search(query, searchBy);

        assertEquals(2, result.size());
        List<Long> ids = result.stream().map(Film::getId).toList();
        assertEquals(List.of(1L, 2L), ids);
    }
}
