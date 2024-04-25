package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Integer, User> users = new HashMap<>();
    private final HashMap<Integer, Set<Integer>> userFriends = new HashMap<>();
    private int userId = 0;

    @Override
    public List<User> getUsers() {
        log.info("Вызван GET-запрос. Выведен список пользователей.");
        return new ArrayList<>(users.values());
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

    @Override
    public User getUser(int id) {
        if (!users.containsKey(id)) {
            throw new ElementNotFoundException("Пользователь " + id + " не найден.");
        }
        return users.get(id);
    }

    @Override
    public User addFriend(Integer id, Integer friendId) {
        Set<Integer> userFriendSet = userFriends.getOrDefault(id, new HashSet<>());
        userFriendSet.add(friendId);
        userFriends.put(id, userFriendSet);

        Set<Integer> friendFriendSet = userFriends.getOrDefault(friendId, new HashSet<>());
        friendFriendSet.add(id);
        userFriends.put(friendId, friendFriendSet);

        return getUser(friendId);
    }

    @Override
    public User deleteFriend(Integer id, Integer friendId) {
        getUser(id);
        getUser(friendId);
        if (userFriends.get(id).contains(friendId)) {
            userFriends.get(id).remove(friendId);
            userFriends.get(friendId).remove(id);
        }
        return getUser(friendId);
    }

    @Override
    public List<User> getFriends(int id) {
        getUser(id);
        Set<Integer> friendsId = userFriends.get(id);
        List<User> friends = new ArrayList<>();
        for (Integer friendId : friendsId) {
            friends.add(getUser(friendId));
        }
        return friends;
    }

    @Override
    public List<User> getCommonFriends(Integer id, Integer otherId) {
        getUser(id);
        getUser(otherId);
        Set<Integer> friends1 = userFriends.get(id);
        Set<Integer> friends2 = userFriends.get(otherId);
        List<User> commonFriend = new ArrayList<>();
        if (friends1 != null && friends2 != null) {
            friends1.retainAll(friends2);
            for (Integer friend : friends1) {
                commonFriend.add(getUser(friend));
            }
        }
        return commonFriend;
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
