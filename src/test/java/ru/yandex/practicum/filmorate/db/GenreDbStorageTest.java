package ru.yandex.practicum.filmorate.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest // указываем, о необходимости подготовить бины для работы с БД
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testGetGenreById() {
        GenreDbStorage storage = new GenreDbStorage(jdbcTemplate);
        Genre genre = new Genre(1, "Комедия");

        assertThat(storage.getGenre(1)).isNotNull().usingRecursiveComparison().isEqualTo(genre);
    }

    @Test
    public void testGetAllGenres() {
        GenreDbStorage storage = new GenreDbStorage(jdbcTemplate);

        assertThat(storage.getAllGenres().size()).isEqualTo(6);
    }
}
