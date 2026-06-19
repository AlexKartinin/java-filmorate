package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }

    private User validUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void createUserWithEmptyEmail() {
        User user = validUser();
        user.setEmail("");
        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void createUserWithEmailWithoutAt() {
        User user = validUser();
        user.setEmail("invalidemail");
        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void createUserWithEmptyLogin() {
        User user = validUser();
        user.setLogin("");
        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void createUserWithLoginContainingSpace() {
        User user = validUser();
        user.setLogin("log in");
        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void createUserWithEmptyNameUsesLogin() {
        User user = validUser();
        user.setName("");
        User created = controller.create(user);
        assertEquals(user.getLogin(), created.getName());
    }

    @Test
    void createUserWithNullNameUsesLogin() {
        User user = validUser();
        user.setName(null);
        User created = controller.create(user);
        assertEquals(user.getLogin(), created.getName());
    }

    @Test
    void createUserWithTodayBirthday() {
        User user = validUser();
        user.setBirthday(LocalDate.now());
        User created = controller.create(user);
        assertEquals(1, created.getId());
    }

    @Test
    void createUserWithFutureBirthday() {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> controller.create(user));
    }
}
