package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ElementNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private FilmController filmController;
    private UserStorage userStorage;
    private Validator validator;

    @BeforeEach
    public void beforeEach() {
        userStorage = new InMemoryUserStorage();
        filmController = new FilmController(new FilmService(new InMemoryFilmStorage(), userStorage));
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void getFilmsTest() {
        assertEquals(0, filmController.getFilms().size(), "Список фильмов должен быть пустым.");
        Film film = new Film("film1", "film1Description",
                LocalDate.of(1999, 11, 11), 180);
        filmController.createFilm(film);

        assertEquals(1, filmController.getFilms().size(), "Количество фильмов не совпадает.");
        assertTrue(filmController.getFilms().contains(film), "Созданного фильма в списке нет.");
    }

    @Test
    public void postFilmTest() {
        Film film = new Film("film1", "film1Description",
                LocalDate.of(1999, 11, 11), 180);
        filmController.createFilm(film);

        assertEquals(1, filmController.getFilms().size(), "Количество фильмов не совпадает.");
        assertEquals(film, filmController.getFilms().get(0), "Фильмы не совпадают.");
    }

    @Test
    public void putFilmTest() {
        Film film = new Film("film1", "film1Description",
                LocalDate.of(1999, 11, 11), 180);
        filmController.createFilm(film);

        Film updatedFilm = new Film(1, "filmName", "filmDescription",
                LocalDate.of(2000, 11, 11), 120);
        filmController.updateFilm(updatedFilm);

        assertEquals(1, filmController.getFilms().size(), "Количество фильмов не совпадает.");
        assertEquals(updatedFilm, filmController.getFilms().get(0), "Фильмы не совпадают.");
    }

    @Test
    public void filmWithEmptyNameTest() {
        Film film = new Film();
        film.setName("");
        film.setDescription("description");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(2000, 11, 11));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Создан фильм с названием.");
    }

    @Test
    public void filmDescriptionTest() {
        Film film = new Film();
        film.setName("film");
        film.setDescription("film1DescriptionFilm1DescriptionFilm1Description" +
                "Film1DescriptionFilm1DescriptionFilm1DescriptionFilm1DescriptionFilm1DescriptionFilm1Description" +
                "Film1DescriptionFilm1DescriptionFilm1DescriptionFilm1Description");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(2000, 11, 11));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Описание меньше 200 символов.");

        Film film1 = new Film("film1", "film1DescriptionFilm1DescriptionFilm1Description" +
                "Film1DescriptionFilm1DescriptionFilm1DescriptionFilm1DescriptionFilm1DescriptionFilm1Description" +
                "Film1DescriptionFilm1DescriptionFilm1DescriptionFilm1Des",
                LocalDate.of(1999, 11, 11), 180);

        Set<ConstraintViolation<Film>> violations1 = validator.validate(film1);
        assertTrue(violations1.isEmpty(), "Описание не равно 200");
    }

    @Test
    public void filmDateTest() {
        Film film = new Film("film1", "film1Description",
                LocalDate.of(1895, 12, 27), 180);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film), "Релиз фильма должен быть раньше 28.12.1895");
        assertEquals(0, filmController.getFilms().size(), "Список фильмов должен быть пустым.");

        Film film1 = new Film("film1", "film1Description",
                LocalDate.of(1895, 12, 28), 180);
        filmController.createFilm(film1);

        assertEquals(1, filmController.getFilms().size(), "Количество фильмов не совпадает.");
    }

    @Test
    public void filmDurationTest() {
        Film film = new Film("film1", "film1Description",
                LocalDate.of(1999, 11, 11), -10);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Продолжительность должна быть отрицательной.");

        Film film1 = new Film("film1", "film1Description",
                LocalDate.of(1999, 11, 11), 10);
        filmController.createFilm(film1);

        assertEquals(1, filmController.getFilms().size(), "Количество фильмов не совпадает.");
    }

    @Test
    public void addAndDeleteLikeTest() {
        Film film = new Film("film", "filmDescription",
                LocalDate.of(1999, 11, 11), 180);
        filmController.createFilm(film);

        User user = new User(0, "user@mail.ru", "userLogin", "userName",
                LocalDate.of(1999, 11, 11));
        userStorage.createUser(user);

        filmController.addLike(1, 1);

        assertThrows(ElementNotFoundException.class, () -> filmController.addLike(2, 1), "Ошибка не выводится");
        assertEquals(1, filmController.getFilm(1).getLikes(), "Количество лайков не совпадает.");

        Film updatedFilm = new Film(1, "updatedFilm", "filmDescription",
                LocalDate.of(1999, 11, 11), 180);
        filmController.updateFilm(updatedFilm);

        assertEquals(1, filmController.getFilm(1).getLikes(), "Лайки не передаются в обновленный фильм.");

        filmController.deleteLike(1, 1);

        assertThrows(ElementNotFoundException.class, () -> filmController.deleteLike(2, 1), "Ошибка не выводится");
        assertEquals(0, filmController.getFilm(1).getLikes(), "Количество лайков не совпадает.");
    }

    @Test
    public void getTopFilmsTest() {
        Film film = new Film("film1", "film1Description",
                LocalDate.of(1999, 11, 11), 180);
        filmController.createFilm(film);
        Film film2 = new Film("film2", "film2Description",
                LocalDate.of(1999, 11, 11), 180);
        filmController.createFilm(film2);
        Film film3 = new Film("film3", "film3Description",
                LocalDate.of(1999, 11, 11), 180);
        filmController.createFilm(film3);

        User user1 = new User(0, "user@mail.ru", "userLogin", "userName",
                LocalDate.of(1999, 11, 11));
        userStorage.createUser(user1);
        User user2 = new User(0, "user@mail.ru", "userLogin", "userName",
                LocalDate.of(1999, 11, 11));
        userStorage.createUser(user2);
        User user3 = new User(0, "user@mail.ru", "userLogin", "userName",
                LocalDate.of(1999, 11, 11));
        userStorage.createUser(user3);
        User user4 = new User(0, "user@mail.ru", "userLogin", "userName",
                LocalDate.of(1999, 11, 11));
        userStorage.createUser(user4);

        filmController.addLike(1, 3);
        filmController.addLike(1, 4);
        filmController.addLike(2, 4);
        filmController.addLike(3, 1);
        filmController.addLike(3, 4);
        filmController.addLike(3, 3);
        filmController.addLike(3, 2);

        List<Film> best = filmController.getTopFilms(3);
        List<Film> best1 = new ArrayList<>();
        best1.add(film3);
        best1.add(film);
        best1.add(film2);
        assertEquals(best, best1, "Ошибка в списке.");
    }

    @Test
    public void getFilmTest() {
        Film film = new Film("film1", "film1Description",
                LocalDate.of(1999, 11, 11), 180);
        filmController.createFilm(film);

        assertEquals(film, filmController.getFilm(1), "Фильмы не совпадают.");

        assertThrows(ElementNotFoundException.class, () -> filmController.getFilm(2), "Ошибка не выводится");
    }
}