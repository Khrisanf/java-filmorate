package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmServiceGetPopularTest {

    @Mock
    private FilmStorage filmStorage;

    @InjectMocks
    private FilmService filmService;

    @Test
    void getPopular_whenCountLessOrEqualZero_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> filmService.getPopular(0));
        assertThrows(IllegalArgumentException.class, () -> filmService.getPopular(-5));
        verifyNoInteractions(filmStorage);
    }

    @Test
    void getPopular_withoutFilters_shouldDelegateToSimpleStorageMethod() {
        Film film = Film.builder()
                .id(1L)
                .name("Test film")
                .description("desc")
                .duration(100)
                .directors(new LinkedHashSet<>()) // важно, чтобы filterExistingDirectors не лез в directorService
                .build();

        when(filmStorage.findPopular(3)).thenReturn(List.of(film));

        Collection<Film> result = filmService.getPopular(3, null, null);

        assertEquals(1, result.size());
        assertEquals(1L, result.iterator().next().getId());

        verify(filmStorage, times(1)).findPopular(3);
        verify(filmStorage, never()).findPopular(anyInt(), any(), any());
    }

    @Test
    void getPopular_withGenreOrYear_shouldDelegateToOverloadedStorageMethod() {
        Film film = Film.builder()
                .id(2L)
                .name("Another film")
                .description("desc")
                .duration(100)
                .directors(new LinkedHashSet<>())
                .build();

        when(filmStorage.findPopular(10, 1, 2000)).thenReturn(List.of(film));

        Collection<Film> result = filmService.getPopular(10, 1, 2000);

        assertEquals(1, result.size());
        assertEquals(2L, result.iterator().next().getId());

        verify(filmStorage, times(1)).findPopular(10, 1, 2000);
        verify(filmStorage, never()).findPopular(anyInt());
    }
}
