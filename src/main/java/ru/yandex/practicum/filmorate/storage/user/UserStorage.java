package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;

public interface UserStorage {
    HashMap<Integer, User> getUsers();

    User createUser(User user);

    User updateUser(User user);
}