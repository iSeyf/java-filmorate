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
        String sql = "SELECT name FROM genres WHERE genre_id = ?";
        List<String> name = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("name"), id);
        if (name.isEmpty()) {
            throw new ElementNotFoundException("Такого жанра нет");
        }
        return new Genre(id, name.get(0));
    }
}
