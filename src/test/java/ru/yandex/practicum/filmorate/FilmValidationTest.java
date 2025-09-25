package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FilmValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Film validFilm() {
        Film f = new Film();
        f.setName("Matrix");
        f.setDescription("A".repeat(200));
        f.setReleaseDate(LocalDate.of(1895, 12, 28));
        f.setDuration(Duration.ofMinutes(1));
        return f;
    }

    @Test
    void validFilm_shouldPass() {
        Set<ConstraintViolation<Film>> v = validator.validate(validFilm());
        assertThat(v).isEmpty();
    }

    @Test
    void blankName_shouldFail() {
        Film f = validFilm();
        f.setName("   ");
        assertThat(validator.validate(f))
                .anyMatch(cv -> cv.getPropertyPath().toString().equals("name"));
    }

    @Test
    void descriptionOver200_shouldFail() {
        Film f = validFilm();
        f.setDescription("A".repeat(201));
        assertThat(validator.validate(f))
                .anyMatch(cv -> cv.getPropertyPath().toString().equals("description"));
    }

    @Test
    void releaseDateBeforeBirth_shouldFail() {
        Film f = validFilm();
        f.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThat(validator.validate(f))
                .anyMatch(cv -> cv.getPropertyPath().toString().equals("releaseDate"));
    }
}
