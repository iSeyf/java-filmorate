package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ElementNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Slf4j
@Repository
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> getAllMpa() {
        String sql = "SELECT * FROM mpa;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int id = rs.getInt("mpa_id");
            String name = rs.getString("name");
            return new Mpa(id, name);
        });
    }

    @Override
    public Mpa getMpa(int id) {
        String sql = "SELECT name FROM mpa WHERE mpa_id = ?";
        List<String> name = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("name"), id);
        if (name.isEmpty()) {
            throw new ElementNotFoundException("Рейтинг не найден");
        }
        return new Mpa(id, name.get(0));
    }
}
