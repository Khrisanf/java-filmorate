package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.SearchBy;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.search.KmpSubstringMatcher;
import ru.yandex.practicum.filmorate.search.SubstringMatcher;

import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FilmSearchServiceTest {

    private FilmService filmService;
    private SubstringMatcher matcher;
    private FilmSearchService filmSearchService;

    @BeforeEach
    void setUp() {
        filmService = mock(FilmService.class);
        matcher = new KmpSubstringMatcher();
        filmSearchService = new FilmSearchService(filmService, matcher);
    }

    @Test
    void search_whenQueryMatchesTitle_returnsMatchingFilms() {
        Film film1 = film(1L, "Крадущийся тигр, затаившийся дракон", 3);
        Film film2 = film(2L, "Крадущийся в ночи", 1);
        Film film3 = film(3L, "Тихий омут", 5);

        Collection<Film> films = List.of(film1, film2, film3);
        when(filmService.findAll()).thenReturn(films);

        String query = "крад";
        Set<SearchBy> searchBy = EnumSet.of(SearchBy.TITLE);

        List<Film> result = filmSearchService.search(query, searchBy);

        assertEquals(2, result.size());
        List<Long> ids = result.stream().map(Film::getId).toList();
        assertEquals(List.of(1L, 2L), ids);
    }

    @Test
    void search_whenSeveralFilmsMatch_sortsByPopularityDesc() {
        Film film1 = film(1L, "Крадущийся тигр", 1);
        Film film2 = film(2L, "Крадущийся дракон", 5);
        Film film3 = film(3L, "Крадущийся в ночи", 3);

        when(filmService.findAll()).thenReturn(List.of(film1, film2, film3));

        String query = "крад";
        Set<SearchBy> searchBy = EnumSet.of(SearchBy.TITLE);

        List<Film> result = filmSearchService.search(query, searchBy);

        assertEquals(3, result.size());
        List<Long> idsInOrder = result.stream().map(Film::getId).toList();
        assertEquals(List.of(2L, 3L, 1L), idsInOrder);
    }

    @Test
    void search_whenQueryEmpty_returnsEmptyList() {
        when(filmService.findAll()).thenReturn(List.of());

        String query = "";
        Set<SearchBy> searchBy = EnumSet.of(SearchBy.TITLE);

        List<Film> result = filmSearchService.search(query, searchBy);

        assertEquals(0, result.size());
    }

    @Test
    void search_whenNoFilmsMatch_returnsEmptyList() {
        Film film1 = film(1L, "Тихий омут", 2);
        Film film2 = film(2L, "Марсианские хроники", 4);

        when(filmService.findAll()).thenReturn(List.of(film1, film2));

        String query = "крад";
        Set<SearchBy> searchBy = EnumSet.of(SearchBy.TITLE);

        List<Film> result = filmSearchService.search(query, searchBy);

        assertEquals(0, result.size());
    }

    @Test
    void search_whenQueryAndTitleHaveDifferentCase_ignoresCase() {
        Film film = film(1L, "Крадущийся ТИГР", 3);
        when(filmService.findAll()).thenReturn(List.of(film));

        String query = "КрАд";
        Set<SearchBy> searchBy = EnumSet.of(SearchBy.TITLE);

        List<Film> result = filmSearchService.search(query, searchBy);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    // ===helper
    private Film film(Long id, String name, int likesCount) {
        Film film = Film.builder()
                .id(id)
                .name(name)
                .description("test")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        for (int i = 0; i < likesCount; i++) {
            film.getLikes().add((long) (i + 1));
        }

        return film;
    }
}
