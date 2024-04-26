package ru.yandex.practicum.filmorate.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controllers.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest // указываем, о необходимости подготовить бины для работы с БД
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @DirtiesContext
    @Test
    public void testCreateAndFindUserById() {
        // Подготавливаем данные для теста
        User newUser = new User(1, "user@email.ru", "vanya123", "Ivan Petrov", LocalDate.of(1990, 1, 1));
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        userStorage.createUser(newUser);

        // вызываем тестируемый метод
        User savedUser = userStorage.getUser(1);

        // проверяем утверждения
        assertThat(savedUser)
                .isNotNull() // проверяем, что объект не равен null
                .usingRecursiveComparison() // проверяем, что значения полей нового
                .isEqualTo(newUser);        // и сохраненного пользователя - совпадают
    }

    @DirtiesContext
    @Test
    public void testUpdateUser() {
        User newUser = new User(1, "user@email.ru", "vanya123", "Ivan Petrov", LocalDate.of(1990, 1, 1));
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        userStorage.createUser(newUser);

        User updatedUser = new User(1, "petrov@mail.ru", "vanya1", "Ivan", LocalDate.of(1990, 1, 1));
        userStorage.updateUser(updatedUser);
        assertThat(userStorage.getUser(1))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(updatedUser);

        assertThat(userStorage.getUser(1))
                .isNotNull()
                .usingRecursiveComparison()
                .isNotEqualTo(newUser);
    }

    @DirtiesContext
    @Test
    public void testGetUsers() {
        User user1 = new User(1, "user@mail.ru", "vanya123", "Ivan Petrov", LocalDate.of(1990, 1, 1));
        User user2 = new User(2, "second@mail.ru", "second", "Second User", LocalDate.of(1990, 2, 2));
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        userStorage.createUser(user1);
        userStorage.createUser(user2);
        assertThat(userStorage.getUsers().size())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(2);
    }

    @DirtiesContext
    @Test
    public void testAddAndDeleteFriend() {
        User user1 = new User(1, "user@mail.ru", "vanya123", "Ivan Petrov", LocalDate.of(1990, 1, 1));
        User user2 = new User(2, "second@mail.ru", "second", "Second User", LocalDate.of(1990, 2, 2));
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        userStorage.createUser(user1);
        userStorage.createUser(user2);
        userStorage.addFriend(user1.getId(), user2.getId());

        assertThat(userStorage.getUser(1).getFriendsCount())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(1);

        assertThat(userStorage.getUser(2).getFriendsCount())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(0);

        userStorage.addFriend(user2.getId(), user1.getId());

        assertThat(userStorage.getUser(2).getFriendsCount())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(1);

        userStorage.deleteFriend(user1.getId(), user2.getId());

        assertThat(userStorage.getUser(1).getFriendsCount())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(0);

        assertThat(userStorage.getUser(2).getFriendsCount())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(1);

        userStorage.deleteFriend(user2.getId(), user1.getId());

        assertThat(userStorage.getUser(2).getFriendsCount())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(0);
    }

    @DirtiesContext
    @Test
    public void testGetFriends() {
        User user1 = new User(1, "user@mail.ru", "vanya123", "Ivan Petrov", LocalDate.of(1990, 1, 1));
        User user2 = new User(2, "second@mail.ru", "second", "Second User", LocalDate.of(1990, 2, 2));
        UserController userController = new UserController(new UserService(new UserDbStorage(jdbcTemplate)));
        userController.createUser(user1);
        userController.createUser(user2);
        userController.addFriend(user1.getId(), user2.getId());

        assertThat(userController.getFriends(1).size())
                .isEqualTo(1);
        assertThat(userController.getFriends(2).size())
                .isEqualTo(0);
    }

}