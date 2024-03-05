package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ElementNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public HashMap<Integer, User> getUsers() {
        return userStorage.getUsers();
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public User getUser(int id) {
        if (!getUsers().containsKey(id)) {
            throw new ElementNotFoundException("Пользователь " + id + " не найден.");
        }
        return getUsers().get(id);
    }

    public List<User> getFriends(int id) {
        if (!getUsers().containsKey(id)) {
            throw new ElementNotFoundException("Пользователь " + id + " не найден.");
        }
        Set<Integer> friendsId = getUsers().get(id).getFriends();
        List<User> friends = new ArrayList<>();
        for (Integer friendId : friendsId) {
            friends.add(getUsers().get(friendId));
        }
        return friends;
    }

    public User addFriend(int id, int friendId) {
        if (!getUsers().containsKey(id)) {
            throw new ElementNotFoundException("Пользователь " + id + " не найден.");
        }
        if (!getUsers().containsKey(friendId)) {
            throw new ElementNotFoundException("Пользователь " + friendId + " не найден.");
        }
        getUser(id).addFriend(friendId);
        getUser(friendId).addFriend(id);
        return getUser(friendId);
    }

    public User deleteFriend(int id, int friendId) {
        if (!getUsers().containsKey(id)) {
            throw new ElementNotFoundException("Пользователь " + id + " не найден.");
        }
        if (!getUsers().containsKey(friendId)) {
            throw new ElementNotFoundException("Пользователь " + friendId + " не найден.");
        }
        getUser(id).deleteFriend(friendId);
        getUser(friendId).deleteFriend(id);
        return getUser(friendId);
    }

    public List<User> getCommonFriends(int id, int otherId) {
        if (!getUsers().containsKey(id)) {
            throw new ElementNotFoundException("Пользователь " + id + " не найден.");
        }
        if (!getUsers().containsKey(otherId)) {
            throw new ElementNotFoundException("Пользователь " + otherId + " не найден.");
        }
        Set<Integer> friends1 = getUsers().get(id).getFriends();
        Set<Integer> friends2 = getUsers().get(otherId).getFriends();
        List<User> commonFriend = new ArrayList<>();
        for (Integer friend1 : friends1) {
            for (Integer friend2 : friends2) {
                if (friend1.equals(friend2)) {
                    commonFriend.add(getUser(friend1));
                }
            }
        }
        return commonFriend;
    }
}
