package ru.yandex.practicum.filmorate.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class})
@Sql({"/schema.sql"})
class FilmDbStorageTest implements FilmorateTestSupport {

    @Autowired
    private FilmDbStorage filmStorage;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        testFilm = filmStorage.create(
                film("Test Film", "Description of test film", LocalDate.of(2020, 1, 1), 120)
        );
    }

    @Test
    void shouldCreateFilmWithValidData_returnFilmWithId() {
        Film created = filmStorage.create(
                film("New Film", "New description", LocalDate.now(), 90)
        );
        assertThat(created).isNotNull();
        assertThat(created.getId()).isPositive();
        assertThat(created.getName()).isEqualTo("New Film");
        assertThat(created.getDuration()).isEqualTo(90);
    }

    @Test
    void shouldFindFilmById_whenFilmExists_returnFilmOptional() {
        Optional<Film> found = filmStorage.findById(testFilm.getId());
        assertThat(found).isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("id", testFilm.getId()));
    }

    @Test
    void shouldUpdateFilm_whenValidDataProvided_updateFilmInStorage() {
        testFilm.setName("Updated Film Name");
        testFilm.setDescription("Updated description");
        Film updated = filmStorage.update(testFilm);

        assertThat(updated.getName()).isEqualTo("Updated Film Name");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void shouldDeleteFilm_whenFilmExists_removeFilmFromStorage() {
        assertThat(filmStorage.findById(testFilm.getId())).isPresent();
        filmStorage.deleteById(testFilm.getId());
        assertThat(filmStorage.findById(testFilm.getId())).isEmpty();
    }
}
