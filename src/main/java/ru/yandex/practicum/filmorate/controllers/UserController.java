package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exceptions.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequestMapping("/users")
@RestController
@Slf4j
public class UserController {
    private final HashMap<Integer, User> users = new HashMap<>();
    private int userId = 0;

    @GetMapping
    public List<User> getUsers() {
        log.info("Вызван GET-запрос. Выведен список пользователей.");
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        if (checkValid(user)) {
            user.setId(getNewId());
            users.put(user.getId(), user);
            log.info("Добавлен новый пользователь: " + user.getName() + ".");
        }
        return users.get(user.getId());
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        if (!users.containsKey(user.getId())) {
            throw new ElementNotFoundException("Объект не найден.");
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
