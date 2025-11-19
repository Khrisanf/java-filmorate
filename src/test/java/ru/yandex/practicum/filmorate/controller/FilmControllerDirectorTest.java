package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class)
class FilmControllerDirectorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmService filmService;

    @Test
    void shouldGetFilmsByDirectorSortedByLikes() throws Exception {
        Film film1 = createTestFilm(1L, "Film 1", 2020);
        Film film2 = createTestFilm(2L, "Film 2", 2021);

        when(filmService.getFilmsByDirector(1, "likes")).thenReturn(List.of(film1, film2));

        mockMvc.perform(get("/films/director/1?sortBy=likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void shouldGetFilmsByDirectorSortedByYear() throws Exception {
        Film film1 = createTestFilm(1L, "Film 1", 2020);
        Film film2 = createTestFilm(2L, "Film 2", 2021);

        when(filmService.getFilmsByDirector(1, "year")).thenReturn(List.of(film1, film2));

        mockMvc.perform(get("/films/director/1?sortBy=year"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldCreateFilmWithDirectors() throws Exception {
        Film film = createTestFilmWithDirectors(1L, "New Film");

        when(filmService.create(any(Film.class))).thenReturn(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.directors.length()").value(2))
                .andExpect(jsonPath("$.directors[0].id").value(1))
                .andExpect(jsonPath("$.directors[1].id").value(2));
    }

    @Test
    void shouldUpdateFilmWithDirectors() throws Exception {
        Film film = createTestFilmWithDirectors(1L, "Updated Film");

        when(filmService.update(any(Film.class))).thenReturn(film);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.directors.length()").value(2));
    }

    private Film createTestFilm(Long id, String name, int year) {
        return Film.builder()
                .id(id)
                .name(name)
                .description("Test description")
                .releaseDate(LocalDate.of(year, 1, 1))
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