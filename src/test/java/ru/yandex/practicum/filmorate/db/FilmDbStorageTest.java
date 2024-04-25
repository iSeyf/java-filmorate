package ru.yandex.practicum.filmorate.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest // указываем, о необходимости подготовить бины для работы с БД
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @DirtiesContext
    @Test
    public void testCreateAndGetFilm() {
        Film film = new Film(1, "film", "description", LocalDate.of(1999, 1, 2), 120);
        film.setMpa(new Mpa(1, "G"));
        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate);
        filmStorage.createFilm(film);

        assertThat(filmStorage.getFilm(1))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(film);
    }

    @DirtiesContext
    @Test
    public void testUpdateFilm() {
        Film film = new Film("film", "description",
                LocalDate.of(1999, 1, 2), 120);
        film.setMpa(new Mpa(1, "G"));
        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate);
        filmStorage.createFilm(film);

        Film updatedFilm = new Film(1, "filmName", "filmDescription",
                LocalDate.of(1999, 1, 1), 150);
        updatedFilm.setMpa(new Mpa(2, "PG"));
        filmStorage.updateFilm(updatedFilm);

        assertThat(filmStorage.getFilm(1))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(updatedFilm);

        assertThat(filmStorage.getFilm(1))
                .isNotNull()
                .usingRecursiveComparison()
                .isNotEqualTo(film);
    }

    @DirtiesContext
    @Test
    public void testGetFilms() {
        Film film = new Film(1, "film", "description", LocalDate.of(1999, 1, 2), 120);
        film.setMpa(new Mpa(1, "G"));

        Film film2 = new Film(2, "film2", "description", LocalDate.of(1999, 1, 2), 120);
        film2.setMpa(new Mpa(1, "G"));

        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate);
        filmStorage.createFilm(film);
        filmStorage.createFilm(film2);

        assertThat(filmStorage.getFilms().size()).isEqualTo(2);
    }

    @DirtiesContext
    @Test
    public void testAddAndDeleteLike() {
        Film film = new Film("film", "description",
                LocalDate.of(1999, 1, 2), 120);
        film.setMpa(new Mpa(1, "G"));
        FilmDbStorage filmStorage = new FilmDbStorage(jdbcTemplate);
        UserDbStorage userStorage = new UserDbStorage(jdbcTemplate);
        User user = new User("email@mail.ru", "login", "name", LocalDate.of(1999, 1, 1));
        FilmService filmService = new FilmService(filmStorage, userStorage);
        filmStorage.createFilm(film);
        userStorage.createUser(user);

        filmService.addLike(1, 1);

        assertThat(filmService.getFilm(1).getLikes())
                .isEqualTo(1);

        filmService.deleteLike(1, 1);

        assertThat(filmService.getFilm(1).getLikes())
                .isEqualTo(0);
    }
}
