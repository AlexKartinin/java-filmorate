package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film add(Film film) {
        log.info("Добавление фильма: {}", film);
        validate(film);
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        log.info("Обновление фильма с id={}", film.getId());
        filmStorage.findById(film.getId()).orElseThrow(() -> {
            log.warn("Фильм с id={} не найден при обновлении", film.getId());
            return new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        });
        validate(film);
        return filmStorage.update(film);
    }

    public Film findById(int id) {
        log.info("Получение фильма с id={}", id);
        return filmStorage.findById(id).orElseThrow(() -> {
            log.warn("Фильм с id={} не найден", id);
            return new NotFoundException("Фильм с id=" + id + " не найден");
        });
    }

    public Collection<Film> findAll() {
        log.info("Получение всех фильмов");
        return filmStorage.findAll();
    }

    public void addLike(int filmId, int userId) {
        log.info("Пользователь id={} ставит лайк фильму id={}", userId, filmId);
        Film film = findById(filmId);
        getUserOrThrow(userId);
        film.getLikes().add(userId);
        log.info("Пользователь id={} поставил лайк фильму id={}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        log.info("Пользователь id={} удаляет лайк с фильма id={}", userId, filmId);
        Film film = findById(filmId);
        getUserOrThrow(userId);
        film.getLikes().remove(userId);
        log.info("Пользователь id={} удалил лайк с фильма id={}", userId, filmId);
    }

    public List<Film> getPopular(int count) {
        if (count <= 0) {
            log.warn("Некорректное значение count={} при запросе популярных фильмов", count);
            throw new ValidationException("Количество фильмов должно быть положительным числом");
        }
        log.info("Получение {} популярных фильмов", count);
        return filmStorage.getPopular(count);
    }

    private void getUserOrThrow(int userId) {
        userStorage.findById(userId).orElseThrow(() -> {
            log.warn("Пользователь с id={} не найден", userId);
            return new NotFoundException("Пользователь с id=" + userId + " не найден");
        });
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Валидация не пройдена: пустое название фильма");
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Валидация не пройдена: описание длиннее 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null) {
            log.warn("Валидация не пройдена: отсутствует дата релиза");
            throw new ValidationException("Дата релиза не может быть пустой");
        }
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Валидация не пройдена: дата релиза раньше 28.12.1895");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Валидация не пройдена: продолжительность фильма не положительная");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
