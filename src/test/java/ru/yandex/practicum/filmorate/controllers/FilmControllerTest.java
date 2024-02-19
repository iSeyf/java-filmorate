package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    public void beforeEach() {
        filmController = new FilmController();
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
        Film film = new Film(0, "", "film1Description",
                LocalDate.of(1999, 11, 11), 180);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film), "Создан фильм с названием.");
        assertEquals(0, filmController.getFilms().size(), "Список фильмов должен быть пустым.");
    }

    @Test
    public void filmDescriptionTest() {
        Film film = new Film(0, "film1", "film1DescriptionFilm1DescriptionFilm1Description" +
                "Film1DescriptionFilm1DescriptionFilm1DescriptionFilm1DescriptionFilm1DescriptionFilm1Description" +
                "Film1DescriptionFilm1DescriptionFilm1DescriptionFilm1Description",
                LocalDate.of(1999, 11, 11), 180);

        assertThrows(ValidationException.class, () -> filmController.createFilm(film), "Описание меньше 200 символов.");
        assertEquals(0, filmController.getFilms().size(), "Список фильмов должен быть пустым.");

        Film film1 = new Film(0, "film1", "film1DescriptionFilm1DescriptionFilm1Description" +
                "Film1DescriptionFilm1DescriptionFilm1DescriptionFilm1DescriptionFilm1DescriptionFilm1Description" +
                "Film1DescriptionFilm1DescriptionFilm1DescriptionFilm1Des",
                LocalDate.of(1999, 11, 11), 180);

        assertEquals(200, film1.getDescription().length(), "Описание не равно 200");
        filmController.createFilm(film1);

        assertEquals(1, filmController.getFilms().size(), "Количество фильмов не совпадает.");
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

        assertThrows(ValidationException.class, () -> filmController.createFilm(film), "Продолжительность должна быть отрицательной.");
        assertEquals(0, filmController.getFilms().size(), "Список фильмов должен быть пустым.");

        Film film1 = new Film(0, "film1", "film1Description",
                LocalDate.of(1999, 11, 11), 10);
        filmController.createFilm(film1);

        assertEquals(1, filmController.getFilms().size(), "Количество фильмов не совпадает.");
    }
}