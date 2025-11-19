package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DirectorController.class)
class DirectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DirectorService directorService;

    @Test
    void shouldCreateDirector() throws Exception {
        Director director = Director.builder().id(1).name("Test Director").build();
        when(directorService.create(any())).thenReturn(director);

        mockMvc.perform(post("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(director)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Director"));
    }

    @Test
    void shouldGetAllDirectors() throws Exception {
        Director director1 = Director.builder().id(1).name("Director 1").build();
        Director director2 = Director.builder().id(2).name("Director 2").build();
        when(directorService.findAll()).thenReturn(List.of(director1, director2));

        mockMvc.perform(get("/directors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Director 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Director 2"));
    }

    @Test
    void shouldGetDirectorById() throws Exception {
        Director director = Director.builder().id(1).name("Test Director").build();
        when(directorService.findById(1)).thenReturn(director);

        mockMvc.perform(get("/directors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Director"));
    }

    @Test
    void shouldUpdateDirector() throws Exception {
        Director director = Director.builder().id(1).name("Updated Director").build();
        when(directorService.update(any())).thenReturn(director);

        mockMvc.perform(put("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(director)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Director"));
    }

    @Test
    void shouldDeleteDirector() throws Exception {
        doNothing().when(directorService).deleteById(1);

        mockMvc.perform(delete("/directors/1"))
                .andExpect(status().isNoContent());

        verify(directorService, times(1)).deleteById(1);
    }

    @Test
    void shouldReturn404WhenDirectorNotFound() throws Exception {
        when(directorService.findById(999)).thenThrow(new NotFoundException("Director not found"));

        mockMvc.perform(get("/directors/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateDirectorNameNotBlank() throws Exception {
        Director director = Director.builder().id(1).name("").build();

        mockMvc.perform(post("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(director)))
                .andExpect(status().isBadRequest());
    }
}