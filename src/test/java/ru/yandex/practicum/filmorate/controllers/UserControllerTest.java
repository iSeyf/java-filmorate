package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

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
        userController = new UserController(new UserService(new InMemoryUserStorage()));
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
        assertEquals(user, userController.getUser(1), "Пользователи не совпадают.");
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
        assertEquals(updatedUser, userController.getUser(1), "Пользователи не совпадают.");
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

    @Test
    public void getUserTest() {
        User user = new User("user@mail.ru", "userLogin", "userName",
                LocalDate.of(1999, 11, 11));
        userController.createUser(user);

        assertEquals(user, userController.getUser(1), "Пользователи не совпадают.");
        assertThrows(ElementNotFoundException.class, () -> userController.getUser(2), "Ошибка не выводится");
    }

    @Test
    public void addAndDeleteAndGetFriendsTest() {
        User user1 = new User("user@mail.ru", "userLogin", "userName",
                LocalDate.of(1999, 11, 11));
        userController.createUser(user1);

        assertThrows(ElementNotFoundException.class, () -> userController.addFriend(1, 2), "Ошибка не выводится");

        User user2 = new User("user2@mail.ru", "user2Login", "user2Name",
                LocalDate.of(1999, 11, 11));
        userController.createUser(user2);

        userController.addFriend(1, 2);
        assertTrue(userController.getFriends(1).contains(user2), "user2 не найден в друзьях user1");
        assertTrue(userController.getFriends(2).contains(user1), "user1 не найден в друзьях user2");

        userController.deleteFriend(1, 2);
        assertTrue(userController.getFriends(1).isEmpty(), "user2 не удален из списка друзей user1");
        assertTrue(userController.getFriends(2).isEmpty(), "user1 не удален из списка друзей user2");
        assertThrows(ElementNotFoundException.class, () -> userController.getFriends(3), "Ошибка не выводится");
    }

    @Test
    public void getCommonFriendsTest() {
        User user1 = new User("user@mail.ru", "userLogin", "userName",
                LocalDate.of(1999, 11, 11));
        userController.createUser(user1);

        User user2 = new User("user2@mail.ru", "user2Login", "user2Name",
                LocalDate.of(1999, 11, 11));
        userController.createUser(user2);

        User user3 = new User("user3@mail.ru", "user3Login", "user3Name",
                LocalDate.of(1999, 11, 11));
        userController.createUser(user3);

        assertTrue(userController.getCommonFriends(1, 2).isEmpty(), "У user1 и user2 имеются общие друзья.");

        userController.addFriend(1, 3);
        userController.addFriend(2, 3);

        assertTrue(userController.getCommonFriends(1, 2).contains(user3), "У user1 и user2 нет общих друзей");
        assertThrows(ElementNotFoundException.class, () -> userController.getFriends(4), "Ошибка не выводится");
    }
}