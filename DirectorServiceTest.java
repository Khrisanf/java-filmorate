package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.storage.film.DirectorStorage;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectorServiceTest {

    @Mock
    private DirectorStorage directorStorage;

    private DirectorService directorService;

    @BeforeEach
    void setUp() {
        directorService = new DirectorService(directorStorage);
    }

    @Test
    void shouldFindAllDirectors() {
        Director director1 = Director.builder().id(1).name("Director 1").build();
        Director director2 = Director.builder().id(2).name("Director 2").build();
        when(directorStorage.findAll()).thenReturn(List.of(director1, director2));

        List<Director> directors = (List<Director>) directorService.findAll();

        assertThat(directors).hasSize(2);
        assertThat(directors.get(0).getName()).isEqualTo("Director 1");
        assertThat(directors.get(1).getName()).isEqualTo("Director 2");
    }

    @Test
    void shouldFindDirectorById() {
        Director director = Director.builder().id(1).name("Test Director").build();
        when(directorStorage.findById(1)).thenReturn(Optional.of(director));

        Director found = directorService.findById(1);

        assertThat(found).isEqualTo(director);
        assertThat(found.getName()).isEqualTo("Test Director");
    }

    @Test
    void shouldThrowWhenDirectorNotFound() {
        when(directorStorage.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> directorService.findById(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Director with id 999 not found");
    }

    @Test
    void shouldCreateDirector() {
        Director directorToCreate = Director.builder().name("New Director").build();
        Director createdDirector = Director.builder().id(1).name("New Director").build();
        when(directorStorage.create(any(Director.class))).thenReturn(createdDirector);

        Director result = directorService.create(directorToCreate);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("New Director");
        verify(directorStorage).create(directorToCreate);
    }

    @Test
    void shouldUpdateDirector() {
        Director existingDirector = Director.builder().id(1).name("Old Name").build();
        Director updatedDirector = Director.builder().id(1).name("New Name").build();
        when(directorStorage.findById(1)).thenReturn(Optional.of(existingDirector));
        when(directorStorage.update(any(Director.class))).thenReturn(updatedDirector);

        Director result = directorService.update(updatedDirector);

        assertThat(result.getName()).isEqualTo("New Name");
        verify(directorStorage).update(updatedDirector);
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentDirector() {
        Director director = Director.builder().id(999).name("Director").build();
        when(directorStorage.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> directorService.update(director))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Director with id 999 not found");
    }

    @Test
    void shouldDeleteDirector() {
        when(directorStorage.findById(1)).thenReturn(Optional.of(Director.builder().id(1).build()));
        doNothing().when(directorStorage).deleteById(1);

        directorService.deleteById(1);

        verify(directorStorage).deleteById(1);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentDirector() {
        when(directorStorage.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> directorService.deleteById(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Director with id 999 not found");
    }

    @Test
    void shouldFindMissingIds() {
        Set<Integer> inputIds = Set.of(1, 2, 3);
        Set<Integer> missingIds = Set.of(3);
        when(directorStorage.findMissingIds(inputIds)).thenReturn(missingIds);

        Set<Integer> result = directorService.findMissingIds(inputIds);

        assertThat(result).isEqualTo(missingIds);
        verify(directorStorage).findMissingIds(inputIds);
    }
}
