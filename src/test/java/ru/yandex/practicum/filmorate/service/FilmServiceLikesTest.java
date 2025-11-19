package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.memory.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.memory.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceLikesTest {

    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private FilmService filmService;
    private long userA;
    private long userB;
    private long f1;
    private long f2;
    private long f3;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();

        filmService = new FilmService(
                filmStorage,
                userStorage,
                new DummyGenreService(),
                new DummyMpaService(),
                new TestFeedService()
        );

        userA = createAndSaveUser("alice@example.com", "alice", "Alice").getId();
        userB = createAndSaveUser("bob@example.com", "bob", "Bob").getId();

        f1 = createAndSaveFilm("Film One", "Desc1", LocalDate.of(1995, 1, 1), 120).getId();
        f2 = createAndSaveFilm("Film Two", "Desc2", LocalDate.of(2000, 2, 2), 100).getId();
        f3 = createAndSaveFilm("Film Three", "Desc3", LocalDate.of(2010, 3, 3), 90).getId();

        assertNotEquals(userA, userB);
        assertNotEquals(f1, f2);
    }

    private User createAndSaveUser(String email, String login, String name) {
        User u = User.builder()
                .email(email)
                .login(login)
                .name(name)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        return userStorage.create(u);
    }

    private Film createAndSaveFilm(String name, String description, LocalDate releaseDate, int duration) {
        Film f = Film.builder()
                .name(name)
                .description(description)
                .releaseDate(releaseDate)
                .duration(duration)
                .build();
        return filmStorage.create(f);
    }

    @Test
    void addLike_shouldAddOnce_andBeIdempotent() {
        Film after = filmService.addLike(f1, userA);
        assertTrue(after.getLikes().contains(userA));
        assertEquals(1, after.getLikes().size());

        Film again = filmService.addLike(f1, userA);
        assertTrue(again.getLikes().contains(userA));
        assertEquals(1, again.getLikes().size(), "Повторный лайк не должен дублироваться");
    }

    @Test
    void removeLike_shouldRemove_andBeIdempotent() {
        filmService.addLike(f2, userA);

        Film afterRemove = filmService.removeLike(f2, userA);
        assertFalse(afterRemove.getLikes().contains(userA));
        assertEquals(0, afterRemove.getLikes().size());

        Film again = filmService.removeLike(f2, userA);
        assertFalse(again.getLikes().contains(userA));
        assertEquals(0, again.getLikes().size(), "Повторное удаление лайка не должно падать/менять состояние");
    }

    @Test
    void addLike_unknownFilm_shouldThrow404() {
        assertThrows(NotFoundException.class, () -> filmService.addLike(999_999L, userA));
    }

    @Test
    void addLike_unknownUser_shouldThrow404() {
        assertThrows(NotFoundException.class, () -> filmService.addLike(f1, 999_999L));
    }

    @Test
    @DisplayName("При корректных вводных, должен возвращаться правильный фильм в качестве рекомендации")
    void shouldFindRecommendationsCorrectly() {
        Long userC = createAndSaveUser("john@example.com", "john", "John").getId();
        Long userD = createAndSaveUser("jeffry@example.com", "jeffry", "Jeffry").getId();
        filmService.addLike(f1, userA);
        filmService.addLike(f2, userA);
        filmService.addLike(f2, userB);
        filmService.addLike(f3, userB);
        filmService.addLike(f1, userC);
        filmService.addLike(f3, userC);
        filmService.addLike(f1, userD);
        filmService.addLike(f2, userD);
        filmService.addLike(f3, userD);
        List<Film> recommendations = filmService.getRecommendations(userA);
        assertNotNull(recommendations);
        assertEquals(1, recommendations.size());
        assertNotNull(recommendations.get(0));
        assertNotNull(recommendations.get(0).getId());
        assertEquals(3, recommendations.get(0).getId());
    }

    @Test
    @DisplayName("Должен выбрасывать исключение, если пользователь не существует")
    void shouldThrowNotFoundExceptionFOrNotExistedUser() {
        assertThrows(NotFoundException.class, () -> filmService.getRecommendations(10L));
    }

    @Test
    @DisplayName("Если у всех пользователей одинаковые лайки, возвращать пустой список")
    void shouldReturnEmptyListIfEveryUserHaveSameLikes() {
        Long userC = createAndSaveUser("john@example.com", "john", "John").getId();
        Long userD = createAndSaveUser("jeffry@example.com", "jeffry", "Jeffry").getId();
        filmService.addLike(f1, userA);
        filmService.addLike(f2, userA);
        filmService.addLike(f1, userB);
        filmService.addLike(f2, userB);
        List<Film> recommendations = filmService.getRecommendations(userA);
        assertNotNull(recommendations);
        assertEquals(0, recommendations.size());
    }


    @Test
    void popular_shouldOrderByLikes_andRespectCount() {
        filmService.addLike(f1, userA);
        filmService.addLike(f2, userA);
        filmService.addLike(f2, userB);
        filmService.addLike(f3, userB);

        Collection<Film> top2Coll = filmService.getPopular(2);
        assertEquals(2, top2Coll.size());

        List<Film> top2 = new ArrayList<>(top2Coll);

        assertEquals(f2, top2.get(0).getId(), "Первым должен быть фильм с наибольшим числом лайков");
        Set<Long> secondCandidates = Set.of(f1, f3);
        assertTrue(secondCandidates.contains(top2.get(1).getId()),
                "Вторым должен быть один из фильмов с по одному лайку");
    }

    @Test
    void getPopular_invalidCount_shouldThrowIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> filmService.getPopular(0));
        assertThrows(IllegalArgumentException.class, () -> filmService.getPopular(-5));
    }

    static class DummyGenreService extends GenreService {
        public DummyGenreService() {
            super(null);
        }
    }

    static class DummyMpaService extends MpaService {
        public DummyMpaService() {
            super(null);
        }
    }
}
