package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ElementNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Slf4j
@Repository
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int id = rs.getInt("genre_id");
            String name = rs.getString("name");
            return new Genre(id, name);
        });
    }

    @Override
    public Genre getGenre(int id) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
                String name = rs.getString("name");
                return new Genre(id, name);
            });
        } catch (Exception e) {
            throw new ElementNotFoundException("В базе нет таких данных");
        }
    }
}
