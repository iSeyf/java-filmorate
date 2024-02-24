package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private FilmController filmController;
    private Validator validator;

    @BeforeEach
    public void beforeEach() {
        filmController = new FilmController();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void getFilmsTest() {
        assertEquals(0, filmController.getFilms().size(), "Список фильмов должен быть пустым.");
        Film film = new Film(0, "film1", "film1Description",
                LocalDate.of(1999, 11, 11), 180);
        filmController.createFilm(film);

        assertEquals(1, filmController.getFilms().size(), "Количество фильмов не совпадает.");
        assertTrue(filmController.getFilms().contains(film), "Созданного фильма в списке нет.");
    }

    @Test
    public void postFilmTest() {
        Film film = new Film(0, "film1", "film1Description",
                LocalDate.of(1999, 11, 11), 180);
        filmController.createFilm(film);

        assertEquals(1, filmController.getFilms().size(), "Количество фильмов не совпадает.");
        assertEquals(film, filmController.getFilms().get(0), "Фильмы не совпадают.");
    }

    @Test
    public void putFilmTest() {
        Film film = new Film(0, "film1", "film1Description",
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

        Film film1 = new Film(0, "film1", "film1DescriptionFilm1DescriptionFilm1Description" +
                "Film1DescriptionFilm1DescriptionFilm1DescriptionFilm1DescriptionFilm1DescriptionFilm1Description" +
                "Film1DescriptionFilm1DescriptionFilm1DescriptionFilm1Des",
                LocalDate.of(1999, 11, 11), 180);

        Set<ConstraintViolation<Film>> violations1 = validator.validate(film1);
        assertTrue(violations1.isEmpty(), "Описание не равно 200");
    }

    @Test
    public void filmDateTest() {
        Film film = new Film(0, "film1", "film1Description",
                LocalDate.of(1895, 12, 27), 180);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film), "Релиз фильма должен быть раньше 28.12.1895");
        assertEquals(0, filmController.getFilms().size(), "Список фильмов должен быть пустым.");

        Film film1 = new Film(0, "film1", "film1Description",
                LocalDate.of(1895, 12, 28), 180);
        filmController.createFilm(film1);

        assertEquals(1, filmController.getFilms().size(), "Количество фильмов не совпадает.");
    }

    @Test
    public void filmDurationTest() {
        Film film = new Film(0, "film1", "film1Description",
                LocalDate.of(1999, 11, 11), -10);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Продолжительность должна быть отрицательной.");

        Film film1 = new Film(0, "film1", "film1Description",
                LocalDate.of(1999, 11, 11), 10);
        filmController.createFilm(film1);

        assertEquals(1, filmController.getFilms().size(), "Количество фильмов не совпадает.");
    }
}