package ru.yandex.practicum.filmorate.popularity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * There is a simple way for testing service and dbStorage - into controller.
 */

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dbFilms"})
@Sql(
        scripts = "/film_popular_fixture.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class FilmPopularControllerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserStorage userStorage;

    @Test
    void getPopular_withoutFilters_shouldReturnThreeFilmsSortedByLikesDesc() throws Exception {
        mockMvc.perform(get("/films/popular").param("count", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[2].id").value(3));
    }

    @Test
    void getPopular_withGenreFilter_shouldReturnOnlyGenreSortedByLikesDesc() throws Exception {
        mockMvc.perform(get("/films/popular")
                        .param("genreId", "1")
                        .param("count", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(3));
    }

    @Test
    void getPopular_withYearFilter_shouldReturnOnlyYearSortedByLikesDesc() throws Exception {
        mockMvc.perform(get("/films/popular")
                        .param("year", "2000")
                        .param("count", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(1));
    }

    @Test
    void getPopular_withGenreAndYearFilters_shouldReturnSingleMatchingFilm() throws Exception {
        mockMvc.perform(get("/films/popular")
                        .param("genreId", "1")
                        .param("year", "2000")
                        .param("count", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
