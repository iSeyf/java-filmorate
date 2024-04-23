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
        String sql = "SELECT * FROM films";
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
            String sqlGetGenre = "SELECT COUNT(*) FROM film_genres WHERE film_id = ? AND genre_id = ?";
            for (Genre genre : film.getGenres()) {
                int genreCount = jdbcTemplate.queryForObject(sqlGetGenre, Integer.class, filmId, genre.getId());
                if (genreCount == 0) {
                    jdbcTemplate.update(sqlAddGenre, filmId, genre.getId());
                }
            }

            return new Film(filmId, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), new HashSet<>(), film.getGenres(), film.getMpa()); // Устанавливаем пустое множество для лайков
        } else {
            return null;
        }
    }

    @Override
    public Film updateFilm(Film film) {
        getFilm(film.getId());
        String updateFilmQuery = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        jdbcTemplate.update(updateFilmQuery, film.getName(), film.getDescription(),
                Date.valueOf(film.getReleaseDate()), film.getDuration(),
                film.getMpa().getId(), film.getId());

        String deleteGenresQuery = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteGenresQuery, film.getId());

        String addGenreQuery = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(addGenreQuery, film.getId(), genre.getId());
        }
        return film;
    }

    @Override
    public Film getFilm(int id) {
        try {
            String sql = "SELECT * FROM films WHERE film_id = ?;";
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> mapRowToFilm(rs));
        } catch (Throwable e) {
            throw new ElementNotFoundException("Объект не найден");
        }
    }

    @Override
    public void addLike(int id, int userId) {
        String sql = "INSERT INTO likes(user_id,film_id) VALUES (?, ?);";
        jdbcTemplate.update(sql, userId, id);
    }

    @Override
    public void deleteLike(int id, int userId) {
        String sql = "DELETE FROM likes WHERE user_id = ? AND film_id = ?";
        jdbcTemplate.update(sql, userId, id);
    }

    public Film mapRowToFilm(ResultSet rs) throws SQLException {
        int filmId = rs.getInt("film_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        int duration = rs.getInt("duration");
        int mpaId = rs.getInt("mpa_id");

        String genresSql = "SELECT genre_id FROM film_genres WHERE film_id = ?";
        List<Integer> genreIds = jdbcTemplate.queryForList(genresSql, Integer.class, filmId);
        List<Genre> genres = new ArrayList<>();
        for (Integer genreId : genreIds) {
            String genreNameSql = "SELECT name FROM genres WHERE genre_id = ?";
            String genreName = jdbcTemplate.queryForObject(genreNameSql, String.class, genreId);
            genres.add(new Genre(genreId, genreName));
        }

        String likesSql = "SELECT user_id FROM likes WHERE film_id = ?";
        Set<Integer> likes = new HashSet<>(jdbcTemplate.queryForList(likesSql, Integer.class, filmId));

        String mpaSql = "SELECT name FROM mpa WHERE mpa_id = ?";
        String mpaName = jdbcTemplate.queryForObject(mpaSql, String.class, mpaId);
        Mpa mpa = new Mpa(mpaId, mpaName);

        return new Film(filmId, name, description, releaseDate, duration, likes, genres, mpa);
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
