package ru.yandex.practicum.filmorate.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(DirectorDbStorage.class)
@Sql(scripts = {"/schema.sql", "/testdata-h2.sql", "/testdata-directors-h2.sql"})
class DirectorDbStorageTest {

    @Autowired
    private DirectorDbStorage directorStorage;

    @Test
    void shouldFindAllDirectors() {
        Collection<Director> directors = directorStorage.findAll();

        assertThat(directors).isNotEmpty();
        assertThat(directors).extracting(Director::getName)
                .contains("Test Director 1", "Test Director 2");
    }

    @Test
    void shouldFindDirectorById() {
        Optional<Director> director = directorStorage.findById(1);

        assertThat(director).isPresent();
        assertThat(director.get().getName()).isEqualTo("Test Director 1");
    }

    @Test
    void shouldReturnEmptyForNonExistentId() {
        Optional<Director> director = directorStorage.findById(999);

        assertThat(director).isEmpty();
    }

    @Test
    void shouldUpdateDirector() {
        Director director = Director.builder().id(1).name("Updated Director").build();

        Director updated = directorStorage.update(director);

        assertThat(updated.getName()).isEqualTo("Updated Director");

        Optional<Director> found = directorStorage.findById(1);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Director");
    }

    @Test
    void shouldDeleteDirector() {
        directorStorage.deleteById(1);

        Optional<Director> found = directorStorage.findById(1);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindMissingIds() {
        Set<Integer> missing = directorStorage.findMissingIds(Set.of(1, 2, 999));

        assertThat(missing).containsExactly(999);
    }

    @Test
    void shouldUpdateFilmDirectors() {
        Set<Director> directors = Set.of(
                Director.builder().id(1).build(),
                Director.builder().id(2).build()
        );

        directorStorage.updateFilmDirectors(1L, directors);

        Set<Director> filmDirectors = directorStorage.findDirectorsByFilmId(1L);
        assertThat(filmDirectors).hasSize(2);
        assertThat(filmDirectors).extracting(Director::getId)
                .containsExactly(1, 2);
    }

    @Test
    void shouldFindDirectorsByFilmId() {
        Set<Director> directors = Set.of(Director.builder().id(1).build());
        directorStorage.updateFilmDirectors(1L, directors);

        Set<Director> filmDirectors = directorStorage.findDirectorsByFilmId(1L);

        assertThat(filmDirectors).hasSize(1);
        assertThat(filmDirectors.iterator().next().getId()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentDirector() {
        Director director = Director.builder().id(999).name("Director").build();

        assertThatThrownBy(() -> directorStorage.update(director))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Director with id 999 not found");
    }
}