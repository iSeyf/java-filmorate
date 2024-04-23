package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ElementNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> getUsers() {
        String sql = "SELECT * FROM users;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> (mapRowToUser(rs)));
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
        String sql = "SELECT * FROM users WHERE user_id = ?;";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> mapRowToUser(rs));
        } catch (Throwable e) {
            throw new ElementNotFoundException("Объект не найден");
        }
    }

    @Override
    public User addFriend(int id, int friendId) {
        getUser(id);
        getUser(friendId);
        String sqlCreate = "INSERT INTO friends(user_id, friend_id, status) VALUES (?, ?, ?);";
        String sqlUpdate = "UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?";
        if (checkFriend(friendId, id)) {
            if (!checkFriend(id, friendId)) {
                jdbcTemplate.update(sqlCreate, id, friendId, true);
                jdbcTemplate.update(sqlUpdate, true, friendId, id);
            }
        } else {
            jdbcTemplate.update(sqlCreate, id, friendId, false);
        }
        return getUser(friendId);
    }

    @Override
    public User deleteFriend(int id, int friendId) {
        getUser(id);
        getUser(friendId);
        String sqlDelete = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?;";
        String sqlUpdate = "UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?";
        if (checkFriend(id, friendId)) {
            jdbcTemplate.update(sqlDelete, id, friendId);
            jdbcTemplate.update(sqlUpdate, false, friendId, id);
        }
        return getUser(friendId);
    }

    private boolean checkFriend(int id, int friendId) {
        String sql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, id, friendId);
        return count > 0;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        String sqlGetFriends = "SELECT friend_id FROM friends WHERE user_id = ?";
        String sqlGetFriendStatus = "SELECT * FROM friends WHERE user_id = ?";

        int id = rs.getInt("user_id");
        String email = rs.getString("email");
        String login = rs.getString("login");
        String name = rs.getString("name");
        LocalDate birthday = rs.getDate("birthday").toLocalDate();

        HashSet<Integer> friends = new HashSet<>(jdbcTemplate.query(sqlGetFriends,
                (rs1, rowNum) -> (rs1.getInt("friend_id")), id));

        Map<Integer, Boolean> friendStatus = new HashMap<>();
        SqlRowSet friendsRows = jdbcTemplate.queryForRowSet(sqlGetFriendStatus, id);
        if (friendsRows.next()) {
            friendStatus.put(friendsRows.getInt("friend_id"), friendsRows.getBoolean("status"));
        }

        return new User(id, email, login, name, birthday, friends, friendStatus);
    }
}
