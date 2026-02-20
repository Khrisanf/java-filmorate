package ru.yandex.practicum.filmorate.db;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("dbFilms")
@Import({FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class, DirectorDbStorage.class})
@Sql(scripts = {"/schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        scripts = "/film_popular_fixture.sql",
        executionPhase = BEFORE_TEST_METHOD
)
class FilmDbStorageFindPopularTest {

    @Autowired
    private FilmStorage filmStorage;

    @Test
    void findPopular_withoutFilters_shouldReturnThreeFilmsSortedByLikesDesc() {
        Collection<Film> films = filmStorage.findPopular(3);

        List<Long> ids = films.stream()
                .map(Film::getId)
                .toList();

        assertEquals(List.of(2L, 1L, 3L), ids);
    }

    @Test
    void findPopular_withGenreFilter_shouldReturnOnlyGenreSortedByLikesDesc() {
        Collection<Film> films = filmStorage.findPopular(10, 1, null);

        List<Long> ids = films.stream()
                .map(Film::getId)
                .toList();

        assertEquals(List.of(1L, 3L), ids);
    }

    @Test
    void findPopular_withYearFilter_shouldReturnOnlyYearSortedByLikesDesc() {
        Collection<Film> films = filmStorage.findPopular(10, null, 2000);

        List<Long> ids = films.stream()
                .map(Film::getId)
                .toList();

        assertEquals(List.of(2L, 1L), ids);
    }

    @Test
    void findPopular_withGenreAndYearFilters_shouldReturnSingleMatchingFilm() {
        Collection<Film> films = filmStorage.findPopular(10, 1, 2000);

        List<Long> ids = films.stream()
                .map(Film::getId)
                .toList();

        assertEquals(List.of(1L), ids);
    }
}
