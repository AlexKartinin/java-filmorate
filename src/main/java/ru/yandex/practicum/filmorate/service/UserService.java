package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User add(User user) {
        return userStorage.add(user);
    }

    public User update(User user) {
        userStorage.findById(user.getId()).orElseThrow(() -> {
            log.warn("Пользователь с id={} не найден при обновлении", user.getId());
            return new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        });
        return userStorage.update(user);
    }

    public User findById(int id) {
        return userStorage.findById(id).orElseThrow(() -> {
            log.warn("Пользователь с id={} не найден", id);
            return new NotFoundException("Пользователь с id=" + id + " не найден");
        });
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public void addFriend(int userId, int friendId) {
        User user = findById(userId);
        User friend = findById(friendId);
        user.getFriends().add((long) friendId);
        friend.getFriends().add((long) userId);
        log.info("Пользователь id={} добавил в друзья id={}", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        User user = findById(userId);
        User friend = findById(friendId);
        user.getFriends().remove((long) friendId);
        friend.getFriends().remove((long) userId);
        log.info("Пользователь id={} удалил из друзей id={}", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        User user = findById(userId);
        return user.getFriends().stream()
                .map(id -> findById(id.intValue()))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = findById(userId);
        User other = findById(otherId);
        return user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .map(id -> findById(id.intValue()))
                .collect(Collectors.toList());
    }
}
