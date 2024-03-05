package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;


import java.util.HashMap;


@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Integer, User> users = new HashMap<>();
    private int userId = 0;

    @Override
    public HashMap<Integer, User> getUsers() {
        log.info("Вызван GET-запрос. Выведен список пользователей.");
        return users;
    }

    @Override
    public User createUser(User user) {
        if (checkValid(user)) {
            user.setId(getNewId());
            users.put(user.getId(), user);
            log.info("Добавлен новый пользователь: " + user.getName() + ".");
        }
        return users.get(user.getId());
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            throw new ElementNotFoundException("Пользователь не найден.");
        }
        if (checkValid(user)) {
            users.put(user.getId(), user);
            log.info("Пользователь {} обновлен.", user.getId());
        }
        return user;
    }

    private boolean checkValid(User user) {
        if (user.getLogin().contains(" ")) {
            log.info("Логин содержит пробел.");
            throw new ValidationException("Логин не может содержать пробелы.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return true;
    }

    private int getNewId() {
        return ++userId;
    }
}
