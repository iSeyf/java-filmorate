package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
public class FilmDbStorage implements FilmStorage {
    private static final LocalDate RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> getFilms() {
        String sql = "SELECT f.*, " +
                "m.name AS mpa_name, " +
                "COUNT(l.user_id) AS likes_count, " +
                "GROUP_CONCAT(CONCAT(g.genre_id, ':', g.name) SEPARATOR ', ') AS genres " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN likes AS l ON l.film_id = f.film_id " +
                "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.genre_id " +
                "GROUP BY f.film_id;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs));
    }

    @Override
    public Film createFilm(Film film) {
        if (checkValid(film)) {
            String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                ps.setInt(4, film.getDuration());
                if (film.getMpa() != null) {
                    ps.setInt(5, film.getMpa().getId());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }
                return ps;
            }, keyHolder);

            int filmId = keyHolder.getKey().intValue();

            String sqlAddGenre = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            List<Object[]> batchArgs = new ArrayList<>();
            Set<Integer> existingGenres = new HashSet<>();
            for (Genre genre : film.getGenres()) {
                int genreId = genre.getId();
                if (!existingGenres.contains(genreId)) {
                    batchArgs.add(new Object[]{filmId, genre.getId()});
                    existingGenres.add(genreId);
                }
            }
            if (!batchArgs.isEmpty()) {
                jdbcTemplate.batchUpdate(sqlAddGenre, batchArgs);
            }
            return new Film(filmId, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), 0, film.getGenres(), film.getMpa()); // Устанавливаем пустое множество для лайков
        } else {
            return null;
        }
    }

    @Override
    public Film updateFilm(Film film) {
        String sqlCheckFilm = "SELECT COUNT(*) FROM films WHERE film_id = ?;";
        int filmCheck = jdbcTemplate.queryForObject(sqlCheckFilm, Integer.class, film.getId());
        if (filmCheck == 0) {
            throw new ElementNotFoundException("Объект не найден");
        }
        String sqlUpdateFilm = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sqlUpdateFilm, film.getName(), film.getDescription(),
                Date.valueOf(film.getReleaseDate()), film.getDuration(),
                film.getMpa().getId(), film.getId());

        String sqlDeleteGenres = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sqlDeleteGenres, film.getId());

        String sqlAddGenre = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        List<Object[]> batchArgs = new ArrayList<>();
        Set<Integer> existingGenres = new HashSet<>();
        for (Genre genre : film.getGenres()) {
            int genreId = genre.getId();
            if (!existingGenres.contains(genreId)) {
                batchArgs.add(new Object[]{film.getId(), genre.getId()});
                existingGenres.add(genreId);
            }
        }
        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sqlAddGenre, batchArgs);
        }
        return film;
    }

    @Override
    public Film getFilm(int id) {
        String sql = "SELECT f.*, m.name AS mpa_name, COUNT(user_id) AS likes_count, " +
                "GROUP_CONCAT(CONCAT(g.genre_id, ':', g.name) SEPARATOR ', ') AS genres " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN likes AS l ON l.film_id = f.film_id " +
                "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.genre_id " +
                "WHERE f.film_id = ?" +
                "GROUP BY f.film_id;";
        Film film = jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> mapRowToFilm(rs));
        if (film == null) {
            throw new ElementNotFoundException("Объект не найден");
        }
        return film;
    }

    @Override
    public void addLike(Integer id, Integer userId) {
        String sqlGetLikes = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?;";
        int userLike = jdbcTemplate.queryForObject(sqlGetLikes, Integer.class, id, userId);
        if (userLike == 0) {
            String sql = "INSERT INTO likes(user_id,film_id) VALUES (?, ?);";
            jdbcTemplate.update(sql, userId, id);
        }
    }

    @Override
    public void deleteLike(Integer id, Integer userId) {
        String sql = "DELETE FROM likes WHERE user_id = ? AND film_id = ?";
        jdbcTemplate.update(sql, userId, id);
    }

    @Override
    public List<Film> getTopFilms(int count) {
        String sql = "SELECT f.*, m.name AS mpa_name, COUNT(l.film_id) AS likes_count, " +
                "GROUP_CONCAT(CONCAT(g.genre_id, ':', g.name) SEPARATOR ', ') AS genres " +
                "FROM films AS f " +
                "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.genre_id " +
                "GROUP BY f.film_id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, new Object[]{count}, (rs, rowNum) -> mapRowToFilm(rs));
    }

    public Film mapRowToFilm(ResultSet rs) throws SQLException {
        int filmId = rs.getInt("film_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        int duration = rs.getInt("duration");
        int mpaId = rs.getInt("mpa_id");
        String mpaName = rs.getString("mpa_name");
        int likesCount = rs.getInt("likes_count");

        String genresStr = rs.getString("genres");
        List<Genre> genres = new ArrayList<>();
        if (!genresStr.equals(":")) {
            String[] genreTokens = genresStr.split(", ");
            for (String token : genreTokens) {
                String[] parts = token.split(":");
                int genreId = Integer.parseInt(parts[0]);
                String genreName = parts[1];
                Genre genre = new Genre(genreId, genreName);

                genres.add(genre);
            }
        }
        Mpa mpa = new Mpa(mpaId, mpaName);

        return new Film(filmId, name, description, releaseDate, duration, likesCount, genres, mpa);
    }

    private boolean checkValid(Film film) {
        String sqlGetMpaSize = "SELECT COUNT(*) FROM mpa;";
        String sqlGetGenreSize = "SELECT COUNT(*) FROM genres;";
        int mpaSize = jdbcTemplate.queryForObject(sqlGetMpaSize, Integer.class);
        int genreSize = jdbcTemplate.queryForObject(sqlGetGenreSize, Integer.class);
        if (film.getReleaseDate().isBefore(RELEASE_DATE)) {
            throw new ValidationException("Фильм не мог выйти раньше 28.12.1895.");
        }
        if (film.getMpa().getId() > mpaSize) {
            throw new ValidationException("Такого MPA не существует");
        }
        for (Genre genre : film.getGenres()) {
            if (genre.getId() > genreSize) {
                throw new ValidationException("Такого жанра не существует");
            }
        }
        return true;
    }
}
