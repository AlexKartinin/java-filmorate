package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
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
        log.info("Создание пользователя: {}", user);
        validate(user);
        normalizeUser(user);
        return userStorage.add(user);
    }

    public User update(User user) {
        log.info("Обновление пользователя с id={}", user.getId());
        userStorage.findById(user.getId()).orElseThrow(() -> {
            log.warn("Пользователь с id={} не найден при обновлении", user.getId());
            return new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        });
        validate(user);
        normalizeUser(user);
        return userStorage.update(user);
    }

    public User findById(int id) {
        log.info("Получение пользователя с id={}", id);
        return userStorage.findById(id).orElseThrow(() -> {
            log.warn("Пользователь с id={} не найден", id);
            return new NotFoundException("Пользователь с id=" + id + " не найден");
        });
    }

    public Collection<User> findAll() {
        log.info("Получение всех пользователей");
        return userStorage.findAll();
    }

    public void addFriend(int userId, int friendId) {
        log.info("Пользователь id={} добавляет в друзья id={}", userId, friendId);
        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }
        User user = findById(userId);
        User friend = findById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователь id={} добавил в друзья id={}", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        log.info("Пользователь id={} удаляет из друзей id={}", userId, friendId);
        if (userId == friendId) {
            throw new ValidationException("Нельзя удалить себя из друзей");
        }
        User user = findById(userId);
        User friend = findById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователь id={} удалил из друзей id={}", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        log.info("Получение друзей пользователя id={}", userId);
        User user = findById(userId);
        return user.getFriends().stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        log.info("Получение общих друзей пользователей id={} и id={}", userId, otherId);
        if (userId == otherId) {
            throw new ValidationException("Нельзя искать общих друзей с самим собой");
        }
        User user = findById(userId);
        User other = findById(otherId);
        return user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .map(this::findById)
                .collect(Collectors.toList());
    }

    private void normalizeUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Валидация не пройдена: пустой email");
            throw new ValidationException("Email не может быть пустым");
        }
        if (!user.getEmail().contains("@")) {
            log.warn("Валидация не пройдена: email не содержит @");
            throw new ValidationException("Email должен содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Валидация не пройдена: пустой логин");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Валидация не пройдена: логин содержит пробелы");
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getBirthday() == null) {
            log.warn("Валидация не пройдена: отсутствует дата рождения");
            throw new ValidationException("Дата рождения не может быть пустой");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена: дата рождения в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
