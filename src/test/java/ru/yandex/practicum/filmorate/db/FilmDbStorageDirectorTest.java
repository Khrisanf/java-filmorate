package ru.yandex.practicum.filmorate.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("dbFilms")
@Import({FilmDbStorage.class, DirectorDbStorage.class, GenreDbStorage.class, MpaDbStorage.class})
@Sql(scripts = {"/schema.sql", "/testdata-h2.sql", "/testdata-directors-h2.sql"})
class FilmDbStorageDirectorTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private DirectorDbStorage directorStorage;

    @Test
    void shouldCreateFilmWithDirectors() {
        Film film = createTestFilm();
        Set<Director> directors = Set.of(
                Director.builder().id(1).build(),
                Director.builder().id(2).build()
        );
        film.setDirectors(directors);

        Film created = filmStorage.create(film);

        Film found = filmStorage.findById(created.getId()).orElseThrow();
        assertThat(found.getDirectors()).hasSize(2);
        assertThat(found.getDirectors()).extracting(Director::getId)
                .containsExactly(1, 2);
    }

    @Test
    void shouldUpdateFilmDirectors() {
        Film film = filmStorage.findById(1L).orElseThrow();

        Set<Director> newDirectors = Set.of(Director.builder().id(3).build());
        film.setDirectors(newDirectors);

        Film updated = filmStorage.update(film);

        Film found = filmStorage.findById(1L).orElseThrow();
        assertThat(found.getDirectors()).hasSize(1);
        assertThat(found.getDirectors().iterator().next().getId()).isEqualTo(3);
    }

    @Test
    void shouldFindFilmsByDirectorSortedByLikes() {
        // Setup: Add directors to films and add likes
        Film film1 = filmStorage.findById(1L).orElseThrow();
        Film film2 = filmStorage.findById(2L).orElseThrow();

        film1.setDirectors(Set.of(Director.builder().id(1).build()));
        film2.setDirectors(Set.of(Director.builder().id(1).build()));

        filmStorage.update(film1);
        filmStorage.update(film2);

        // Add likes to film2
        filmStorage.addLike(2L, 1L);

        Collection<Film> films = filmStorage.findFilmsByDirectorSortedByLikes(1);

        assertThat(films).isNotEmpty();
        // film2 should come first because it has more likes
        assertThat(films.iterator().next().getId()).isEqualTo(2L);
    }

    @Test
    void shouldFindFilmsByDirectorSortedByYear() {
        Film film1 = filmStorage.findById(1L).orElseThrow();
        Film film2 = filmStorage.findById(2L).orElseThrow();

        film1.setDirectors(Set.of(Director.builder().id(1).build()));
        film2.setDirectors(Set.of(Director.builder().id(1).build()));

        filmStorage.update(film1);
        filmStorage.update(film2);

        Collection<Film> films = filmStorage.findFilmsByDirectorSortedByYear(1);

        assertThat(films).isNotEmpty();
        // Should be sorted by release date
        assertThat(films).extracting(Film::getReleaseDate)
                .isSorted();
    }

    private Film createTestFilm() {
        return Film.builder()
                .name("Test Film with Directors")
                .description("Test description")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(120)
                .genres(new LinkedHashSet<>())
                .directors(new LinkedHashSet<>())
                .build();
    }
}