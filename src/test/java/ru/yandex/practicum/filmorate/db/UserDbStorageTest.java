package ru.yandex.practicum.filmorate.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class})
class UserDbStorageTest implements FilmorateTestSupport {

    @Autowired
    private UserDbStorage userStorage;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userStorage.create(
                user("test@example.com", "testlogin", "Test User", LocalDate.of(1990, 1, 1))
        );
    }

    @Test
    void shouldCreateUserWithValidData_returnUserWithId() {
        User created = userStorage.create(
                user("new@test.com", "newlogin", "New User", LocalDate.now())
        );
        assertThat(created).isNotNull();
        assertThat(created.getId()).isPositive();
        assertThat(created.getEmail()).isEqualTo("new@test.com");
        assertThat(created.getLogin()).isEqualTo("newlogin");
    }

    @Test
    void shouldFindUserById_whenUserExists_returnUserOptional() {
        Optional<User> found = userStorage.findById(testUser.getId());
        assertThat(found).isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("id", testUser.getId()));
    }

    @Test
    void shouldUpdateUser_whenValidDataProvided_updateUserInStorage() {
        testUser.setName("Updated Name");
        testUser.setEmail("updated@example.com");
        User updated = userStorage.update(testUser);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void shouldDeleteUser_whenUserExists_removeUserFromStorage() {
        assertThat(userStorage.findById(testUser.getId())).isPresent();
        userStorage.deleteById(testUser.getId());
        assertThat(userStorage.findById(testUser.getId())).isEmpty();
    }
}
