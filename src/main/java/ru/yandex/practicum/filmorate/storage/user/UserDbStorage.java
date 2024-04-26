package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ElementNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Repository
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final String sqlGetUsers = "SELECT * FROM users ";

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> getUsers() {
        return jdbcTemplate.query(sqlGetUsers, (rs, rowNum) -> (mapRowToUser(rs)));
    }

    @Override
    public User createUser(User user) {
        String sql = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        int userId = keyHolder.getKey().intValue();
        return new User(userId, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
    }

    @Override
    public User updateUser(User user) {
        getUser(user.getId());
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?;";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public User getUser(int id) {
        List<User> user = jdbcTemplate.query(sqlGetUsers + "WHERE user_id = ?;", (rs, rowNum) -> mapRowToUser(rs), id);
        if (user.isEmpty()) {
            throw new ElementNotFoundException("Объект не найден");
        }
        return user.get(0);
    }

    @Override
    public List<User> getFriends(int id) {
        getUser(id);
        String sql = "SELECT u.* " +
                "FROM friends AS f " +
                "JOIN users AS u ON f.friend_id = u.user_id " +
                "WHERE f.user_id = ?;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToUser(rs), id);
    }

    @Override
    public List<User> getCommonFriends(Integer id, Integer otherId) {
        String sql = "SELECT u.* " +
                "FROM users AS u " +
                "JOIN friends AS f1 ON f1.friend_id = u.user_id " +
                "JOIN friends AS f2 ON f2.friend_id = u.user_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?;";

        return jdbcTemplate.query(sql, new Object[]{id, otherId}, (rs, rowNum) -> mapRowToUser(rs));
    }

    @Override
    public User addFriend(Integer id, Integer friendId) {
        getUser(id);
        getUser(friendId);
        String sqlCreate = "INSERT INTO friends(user_id, friend_id, status) VALUES (?, ?, ?);";
        String sqlUpdate = "UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?";
        if (!checkFriend(id, friendId)) {
            if (checkFriend(friendId, id)) {
                jdbcTemplate.update(sqlCreate, id, friendId, true);
                jdbcTemplate.update(sqlUpdate, true, friendId, id);
            } else {
                jdbcTemplate.update(sqlCreate, id, friendId, false);
            }
            String sqlUpdateFriendsCount = "UPDATE users SET friends_count = friends_count + 1 WHERE user_id = ?";
            jdbcTemplate.update(sqlUpdateFriendsCount, id);
        }
        return getUser(friendId);
    }

    @Override
    public User deleteFriend(Integer id, Integer friendId) {
        getUser(id);
        getUser(friendId);
        String sqlDelete = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?;";
        String sqlUpdate = "UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?";
        if (checkFriend(id, friendId)) {
            jdbcTemplate.update(sqlDelete, id, friendId);
            if (checkFriend(friendId, id)) {
                jdbcTemplate.update(sqlUpdate, false, friendId, id);
            }
            String updateFriendsCountSql = "UPDATE users SET friends_count = friends_count - 1 WHERE user_id = ?";
            jdbcTemplate.update(updateFriendsCountSql, id);
        }
        return getUser(friendId);
    }

    private boolean checkFriend(int id, int friendId) {
        String sql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, id, friendId);
        return count > 0;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String email = rs.getString("email");
        String login = rs.getString("login");
        String name = rs.getString("name");
        LocalDate birthday = rs.getDate("birthday").toLocalDate();
        int friends = rs.getInt("friends_count");


        return new User(id, email, login, name, birthday, friends);
    }
}