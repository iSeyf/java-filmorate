package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    private UserController userController;
    private Validator validator;

    @BeforeEach
    public void beforeEach() {
        userController = new UserController();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void getUsersTest() {
        assertEquals(0, userController.getUsers().size(), "Список должен быть пустым.");

        User user = new User(0, "user@mail.ru", "userLogin", "userName",
                LocalDate.of(1999, 11, 11));
        userController.createUser(user);

        assertEquals(1, userController.getUsers().size(), "Количество пользователей не совпадает.");
        assertEquals(user, userController.getUsers().get(0), "Пользователи не совпадают.");
    }

    @Test
    public void createAndUpdateUser() {
        User user = new User(0, "user@mail.ru", "userLogin", "userName",
                LocalDate.of(1999, 11, 11));
        userController.createUser(user);

        User updatedUser = new User(1, "user@mail.ru", "Login", "Name",
                LocalDate.of(1999, 11, 11));
        userController.updateUser(updatedUser);

        assertEquals(1, userController.getUsers().size(), "Количество пользователей не совпадает.");
        assertEquals(updatedUser, userController.getUsers().get(0), "Пользователи не совпадают.");
    }

    @Test
    public void createUserWithEmptyEmailAndWithoutAT() {
        User user = new User(0, "usermail.ru", "userLogin", "userName",
                LocalDate.of(1999, 11, 11));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Email содержит @!");

        User user1 = new User(0, "", "userLogin", "userName",
                LocalDate.of(1999, 11, 11));

        Set<ConstraintViolation<User>> violations1 = validator.validate(user1);
        assertFalse(violations1.isEmpty(), "Email не пуст!");
    }

    @Test
    public void createUserWithEmptyLoginAndWithSpaceInLogin() {
        User user = new User(0, "user@mail.ru", "", "userName",
                LocalDate.of(1999, 11, 11));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Логин не пуст!");

        User user1 = new User(0, "user@mail.ru", "user Login", "userName",
                LocalDate.of(1999, 11, 11));

        assertThrows(ValidationException.class, () -> userController.createUser(user1), "Логин не содержит пробелы!");
        assertEquals(0, userController.getUsers().size(), "Список должен быть пустым.");
    }

    @Test
    public void createUserWithoutName() {
        User user = new User(0, "user@mail.ru", "userLogin", "",
                LocalDate.of(1999, 11, 11));
        userController.createUser(user);

        assertEquals(user.getName(), user.getLogin(), "Имя пользователя не совпадает с логином.");
    }

    @Test
    public void createUserWithBirthdayInFuture() {
        User user = new User(0, "user@mail.ru", "userLogin", "userName",
                LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Дата рождения не некоректно!");
    }
}