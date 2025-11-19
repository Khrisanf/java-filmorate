package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilmServiceDirectorTest {

    @Mock
    private FilmStorage filmStorage;

    @Mock
    private UserStorage userStorage;

    @Mock
    private GenreService genreService;

    @Mock
    private MpaService mpaService;

    @Mock
    private DirectorService directorService;

    private FilmService filmService;

    @Mock
    private FeedService feedService;

    @BeforeEach
    void setUp() {
        filmService = new FilmService(
                filmStorage,
                userStorage,
                genreService,
                mpaService,
                feedService,
                directorService
        );
    }

    @Test
    void shouldCreateFilmWithDirectors() {
        Film film = createTestFilmWithDirectors(null, "New Film");
        Film createdFilm = createTestFilmWithDirectors(1L, "New Film");

        when(mpaService.existsById(1)).thenReturn(true);
        when(genreService.findMissingIds(anySet())).thenReturn(Set.of());
        when(directorService.findMissingIds(anySet())).thenReturn(Set.of());
        when(filmStorage.create(any(Film.class))).thenReturn(createdFilm);

        Film result = filmService.create(film);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDirectors()).hasSize(2);
        verify(directorService).findMissingIds(Set.of(1, 2));
    }

    @Test
    void shouldThrowWhenDirectorNotFound() {
        Film film = createTestFilmWithDirectors(null, "New Film");

        when(mpaService.existsById(1)).thenReturn(true);
        when(genreService.findMissingIds(anySet())).thenReturn(Set.of());
        when(directorService.findMissingIds(Set.of(1, 2))).thenReturn(Set.of(2));

        assertThatThrownBy(() -> filmService.create(film))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Directors not found: [2]");
    }

    @Test
    void shouldGetFilmsByDirectorSortedByLikes() {
        Film film1 = createTestFilm(1L, "Film 1");
        Film film2 = createTestFilm(2L, "Film 2");

        when(directorService.findById(1)).thenReturn(Director.builder().id(1).build());
        when(filmStorage.findFilmsByDirectorSortedByLikes(1)).thenReturn(List.of(film1, film2));

        List<Film> films = (List<Film>) filmService.getFilmsByDirector(1, "likes");

        assertThat(films).hasSize(2);
        verify(filmStorage).findFilmsByDirectorSortedByLikes(1);
    }

    @Test
    void shouldGetFilmsByDirectorSortedByYear() {
        Film film1 = createTestFilm(1L, "Film 1");
        Film film2 = createTestFilm(2L, "Film 2");

        when(directorService.findById(1)).thenReturn(Director.builder().id(1).build());
        when(filmStorage.findFilmsByDirectorSortedByYear(1)).thenReturn(List.of(film1, film2));

        List<Film> films = (List<Film>) filmService.getFilmsByDirector(1, "year");

        assertThat(films).hasSize(2);
        verify(filmStorage).findFilmsByDirectorSortedByYear(1);
    }

    @Test
    void shouldThrowForInvalidSortParameter() {
        when(directorService.findById(1)).thenReturn(Director.builder().id(1).build());

        assertThatThrownBy(() -> filmService.getFilmsByDirector(1, "invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sort parameter");
    }

    @Test
    void shouldUpdateFilmWithDirectors() {
        Film film = createTestFilmWithDirectors(1L, "Updated Film");

        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));
        when(mpaService.existsById(1)).thenReturn(true);
        when(genreService.findMissingIds(anySet())).thenReturn(Set.of());
        when(directorService.findMissingIds(anySet())).thenReturn(Set.of());
        when(filmStorage.update(any(Film.class))).thenReturn(film);

        Film result = filmService.update(film);

        assertThat(result.getDirectors()).hasSize(2);
        verify(directorService).findMissingIds(Set.of(1, 2));
    }

    private Film createTestFilm(Long id, String name) {
        return Film.builder()
                .id(id)
                .name(name)
                .description("Test description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(MpaRating.builder().id(1).name("G").build())
                .genres(new LinkedHashSet<>())
                .directors(new LinkedHashSet<>())
                .build();
    }

    private Film createTestFilmWithDirectors(Long id, String name) {
        Set<Director> directors = new LinkedHashSet<>();
        directors.add(Director.builder().id(1).name("Director 1").build());
        directors.add(Director.builder().id(2).name("Director 2").build());

        return Film.builder()
                .id(id)
                .name(name)
                .description("Test description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(MpaRating.builder().id(1).name("G").build())
                .genres(new LinkedHashSet<>())
                .directors(directors)
                .build();
    }
}