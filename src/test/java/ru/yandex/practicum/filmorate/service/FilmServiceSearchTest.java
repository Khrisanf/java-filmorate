package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.model.SearchBy;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmServiceSearchTest {

    @Mock
    private FilmStorage filmStorage;
    @Mock
    private UserStorage userStorage;
    @Mock
    private GenreService genreService;
    @Mock
    private MpaService mpaService;
    @Mock
    private FeedService feedService;
    @Mock
    private DirectorService directorService;

    @InjectMocks
    private FilmService filmService;

    @Test
    void search_whenQueryEmpty_returnsEmptyListAndDoesNotCallStorage() {
        String query = "";
        var searchBy = EnumSet.of(SearchBy.TITLE);

        var result = filmService.search(query, searchBy);

        assertEquals(0, result.size());
        verifyNoInteractions(filmStorage);
    }

    @Test
    void search_whenValidQuery_callsStorageAndReturnsItsResult() {
        String query = "крад";
        var searchBy = EnumSet.of(SearchBy.TITLE);

        Film film = Film.builder()
                .id(1L)
                .name("Крадущийся тигр")
                .description("test")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .directors(new LinkedHashSet<>())
                .build();

        when(filmStorage.search(query, searchBy)).thenReturn(List.of(film));

        var result = filmService.search(query, searchBy);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(filmStorage, times(1)).search(query, searchBy);
    }

    @Test
    void search_whenFilmHasDeletedDirector_filtersItOut() {
        String query = "крад";
        var searchBy = EnumSet.of(SearchBy.TITLE);

        Director existing = Director.builder().id(1).name("Alive Director").build();
        Director deleted = Director.builder().id(2).name("Deleted Director").build();

        Film film = Film.builder()
                .id(1L)
                .name("Крадущийся тигр")
                .description("test")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .directors(new LinkedHashSet<>(Set.of(existing, deleted)))
                .build();

        when(filmStorage.search(query, searchBy)).thenReturn(List.of(film));

        when(directorService.findById(1)).thenReturn(existing);
        doThrow(new ru.yandex.practicum.filmorate.exceptions.NotFoundException("not found"))
                .when(directorService).findById(2);

        var result = filmService.search(query, searchBy);

        assertEquals(1, result.size());
        var directors = result.get(0).getDirectors();
        assertEquals(1, directors.size());
        assertEquals(1, directors.iterator().next().getId());
    }
}
