package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private User validUser() {
        User u = new User();
        u.setEmail("user@example.com");
        u.setLogin("neo123");
        u.setName("");
        u.setBirthday(LocalDate.now().minusYears(18));
        return u;
    }

    @Test
    void validUser_shouldPass() {
        assertThat(validator.validate(validUser())).isEmpty();
    }

    @Test
    void blankEmail_shouldFail() {
        User u = validUser();
        u.setEmail("   ");
        assertThat(validator.validate(u))
                .anyMatch(cv -> cv.getPropertyPath().toString().equals("email"));
    }

    @Test
    void badEmailFormat_shouldFail() {
        User u = validUser();
        u.setEmail("user_at_example.com");
        assertThat(validator.validate(u))
                .anyMatch(cv -> cv.getPropertyPath().toString().equals("email"));
    }

    @Test
    void blankLogin_shouldFail() {
        User u = validUser();
        u.setLogin("   ");
        assertThat(validator.validate(u))
                .anyMatch(cv -> cv.getPropertyPath().toString().equals("login"));
    }

    @Test
    void loginWithSpaces_shouldFail() {
        User u = validUser();
        u.setLogin("neo 123");
        assertThat(validator.validate(u))
                .anyMatch(cv -> cv.getPropertyPath().toString().equals("login"));
    }

    @Test
    void birthdayInFuture_shouldFail() {
        User u = validUser();
        u.setBirthday(LocalDate.now().plusDays(1));
        assertThat(validator.validate(u))
                .anyMatch(cv -> cv.getPropertyPath().toString().equals("birthday"));
    }
}
