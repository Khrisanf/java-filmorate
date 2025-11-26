package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({DirectorDbStorage.class, DirectorService.class})
@Sql(scripts = {"/schema.sql", "/testdata-h2.sql", "/testdata-directors-h2.sql"})
class DirectorServiceTest {
    @Autowired
    private DirectorService directorService;

    @Test
    void shouldFindAllDirectors() {
        List<Director> directors = (List<Director>) directorService.findAll();

        assertThat(directors).hasSize(3);
        assertThatCollection(directors.stream().map(Director::getName).toList())
                .containsExactly("Test Director 1", "Test Director 2", "Test Director 3");
    }

    @Test
    void shouldFindDirectorById() {
        Director found = directorService.findById(1);
        assertThat(found.getName()).isEqualTo("Test Director 1");
    }

    @Test
    void shouldThrowWhenDirectorNotFound() {
        assertThatThrownBy(() -> directorService.findById(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Director with id 999 not found");
    }

    @Test
    void shouldCreateDirector() {
        Director directorToCreate = Director.builder().name("New Director").build();
        Director result = directorService.create(directorToCreate);

        assertThat(result.getId()).isEqualTo(4);
        assertThat(result.getName()).isEqualTo("New Director");
    }

    @Test
    void shouldUpdateDirector() {
        Director updatedDirector = Director.builder().id(1).name("New Name").build();
        Director result = directorService.update(updatedDirector);
        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentDirector() {
        assertThatThrownBy(() -> directorService.update(Director.builder().id(999).name("New Name").build()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Director with id 999 not found");
    }

    @Test
    void shouldDeleteDirector() {
        directorService.deleteById(1);
        assertThatThrownBy(() -> directorService.findById(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Director with id 1 not found");
    }

    @Test
    void shouldThrowWhenDeletingNonExistentDirector() {
        assertThatThrownBy(() -> directorService.deleteById(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Director with id 999 not found");
    }

    @Test
    void shouldFindMissingIds() {
        Set<Integer> inputIds = Set.of(1, 2, 3, 4);
        Set<Integer> missingIds = Set.of(4);
        Set<Integer> result = directorService.findMissingIds(inputIds);

        assertThat(result).isEqualTo(missingIds);
    }
}