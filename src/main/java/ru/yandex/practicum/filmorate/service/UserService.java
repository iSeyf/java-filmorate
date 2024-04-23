package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public User getUser(int id) {
        return userStorage.getUser(id);
    }

    public List<User> getFriends(int id) {
        Set<Integer> friendsId = getUser(id).getFriends();
        List<User> friends = new ArrayList<>();
        for (Integer friendId : friendsId) {
            friends.add(getUser(friendId));
        }
        return friends;
    }

    public User addFriend(int id, int friendId) {
        return userStorage.addFriend(id, friendId);
    }

    public User deleteFriend(int id, int friendId) {
        return userStorage.deleteFriend(id, friendId);
    }

    public List<User> getCommonFriends(int id, int otherId) {
        Set<Integer> friends1 = getUser(id).getFriends();
        Set<Integer> friends2 = getUser(otherId).getFriends();
        List<User> commonFriend = new ArrayList<>();
        friends1.retainAll(friends2);
        for (Integer friend : friends1) {
            commonFriend.add(getUser(friend));
        }
        return commonFriend;
    }
}
