package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        FilmService filmService = new FilmService(new InMemoryFilmStorage(), new InMemoryUserStorage());
        controller = new FilmController(filmService);
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(90);
        return film;
    }

    @Test
    void addFilmWithEmptyName() {
        Film film = validFilm();
        film.setName("");
        assertThrows(ValidationException.class, () -> controller.add(film));
    }

    @Test
    void addFilmWithNullName() {
        Film film = validFilm();
        film.setName(null);
        assertThrows(ValidationException.class, () -> controller.add(film));
    }

    @Test
    void addFilmWithDescription200Chars() {
        Film film = validFilm();
        film.setDescription("a".repeat(200));
        Film added = controller.add(film);
        assertEquals(1, added.getId());
    }

    @Test
    void addFilmWithDescription201Chars() {
        Film film = validFilm();
        film.setDescription("a".repeat(201));
        assertThrows(ValidationException.class, () -> controller.add(film));
    }

    @Test
    void addFilmWithReleaseDateOnCinemaBirthday() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        Film added = controller.add(film);
        assertEquals(1, added.getId());
    }

    @Test
    void addFilmWithReleaseDateBeforeCinemaBirthday() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> controller.add(film));
    }

    @Test
    void addFilmWithPositiveDuration() {
        Film film = validFilm();
        film.setDuration(1);
        Film added = controller.add(film);
        assertEquals(1, added.getId());
    }

    @Test
    void addFilmWithZeroDuration() {
        Film film = validFilm();
        film.setDuration(0);
        assertThrows(ValidationException.class, () -> controller.add(film));
    }

    @Test
    void addFilmWithNegativeDuration() {
        Film film = validFilm();
        film.setDuration(-1);
        assertThrows(ValidationException.class, () -> controller.add(film));
    }
}
