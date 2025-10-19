package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@lombok.RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        userStorage.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userStorage.update(user);
    }

    public User findById(long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public void deleteById(long id) {
        userStorage.deleteById(id);
    }

    public User addFriends(long userId, long friendId) {
        if (userId == friendId) {
            throw new IllegalArgumentException("Cannot add yourself as a friend");
        }
        User user = findById(userId);
        User friend = findById(friendId);

        boolean isUser = user.getFriends().add(friendId);
        boolean isFriend = friend.getFriends().add(userId);

        if (isUser && isFriend) {
            userStorage.update(user);
            userStorage.update(friend);
        }
        return user;
    }

    public User removeFriends(long userId, long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        boolean isUser = user.getFriends().remove(friendId);
        boolean isFriend = friend.getFriends().remove(userId);

        if (isUser && isFriend) {
            userStorage.update(user);
            userStorage.update(friend);
        }
        return user;
    }

    public Collection<User> findFriends(long userId) {
        User user = findById(userId);
        return user.getFriends().stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public Collection<User> findMutualFriends(long userId, long anotherUserId) {
        User user = findById(userId);
        User anotherUser = findById(anotherUserId);

        return user.getFriends().stream()
                .filter(anotherUser.getFriends()::contains)
                .map(this::findById)
                .collect(Collectors.toList());
    }
}
