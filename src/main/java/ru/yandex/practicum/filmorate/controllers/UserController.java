package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
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
    public User createUser(@RequestBody User user) {
        if (checkValid(user)) {
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }
            if (user.getId() == 0 || users.containsKey(user.getId())) {
                user.setId(getNewId());
            }
            users.put(user.getId(), user);
            log.info("Добавлен новый пользователь: " + user.getName() + ".");
        }
        return users.get(user.getId());
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        if (users.containsKey(user.getId())) {
            if (checkValid(user)) {
                if (user.getName().isBlank()) {
                    user.setName(user.getLogin());
                }
                users.put(user.getId(), user);
                log.info("Пользователь обновлен.");
            }
        } else {
            throw new ValidationException("Объект не найден.");
        }
        return user;
    }

    private boolean checkValid(User user) {
        if (user.getEmail().isBlank() || !user.getEmail().contains("@") || user.getEmail().contains(" ")) {
            log.info("Введена пустая строка или некоректная электронная почта.");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ <<@>>.");
        }
        if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.info("Введена пуста строка или логин содержит пробел.");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы.");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.info("Введена некоректная дата рождения.");
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
        return true;
    }

    private int getNewId() {
        return ++userId;
    }
}
