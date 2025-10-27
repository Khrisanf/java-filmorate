package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class})
class FilmorateApplicationTests {

	@Autowired
	private UserDbStorage userStorage;

	private User testUser;

	@BeforeEach
	void setUp() {
		testUser = userStorage.create(createTestUser(
				"test@example.com",
				"testlogin",
				"Test User",
				LocalDate.of(1990, 1, 1)
		));
	}

	private User createTestUser(String email, String login, String name, LocalDate birthday) {
		return User.builder()
				.email(email)
				.login(login)
				.name(name)
				.birthday(birthday)
				.friends(new HashSet<>())
				.build();
	}

	@Test
	public void shouldCreateUserWithValidData_returnUserWithId() {
		User newUser = createTestUser("new@test.com", "newlogin", "New User", LocalDate.now());
		User createdUser = userStorage.create(newUser);

		assertThat(createdUser).isNotNull();
		assertThat(createdUser.getId()).isPositive();
		assertThat(createdUser.getEmail()).isEqualTo(newUser.getEmail());
		assertThat(createdUser.getLogin()).isEqualTo(newUser.getLogin());
	}

	@Test
	public void shouldFindUserById_whenUserExists_returnUserOptional() {
		long savedId = testUser.getId();
		Optional<User> userOptional = userStorage.findById(savedId);

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user ->
						assertThat(user).hasFieldOrPropertyWithValue("id", savedId)
				);
	}

	@Test
	public void shouldUpdateUser_whenValidDataProvided_updateUserInStorage() {
		testUser.setName("Updated Name");
		testUser.setEmail("updated@example.com");

		User updatedUser = userStorage.update(testUser);

		assertThat(updatedUser.getName()).isEqualTo("Updated Name");
		assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");

		Optional<User> refreshedUser = userStorage.findById(testUser.getId());
		assertThat(refreshedUser)
				.isPresent()
				.hasValueSatisfying(u -> {
					assertThat(u.getName()).isEqualTo("Updated Name");
					assertThat(u.getEmail()).isEqualTo("updated@example.com");
				});
	}

	@Test
	public void shouldDeleteUser_whenUserExists_removeUserFromStorage() {
		assertThat(userStorage.findById(testUser.getId())).isPresent();

		userStorage.deleteById(testUser.getId());

		Optional<User> deletedUser = userStorage.findById(testUser.getId());
		assertThat(deletedUser).isEmpty();
	}
}
