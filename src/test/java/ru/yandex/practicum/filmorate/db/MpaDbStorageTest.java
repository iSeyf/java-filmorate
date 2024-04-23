package ru.yandex.practicum.filmorate.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest // указываем, о необходимости подготовить бины для работы с БД
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaDbStorageTest {
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testGetMpaById() {
        MpaDbStorage storage = new MpaDbStorage(jdbcTemplate);
        Mpa mpa = new Mpa(1, "G");
        assertThat(storage.getMpa(1)).isNotNull().usingRecursiveComparison().isEqualTo(mpa);
    }

    @Test
    public void testGetAllMpa() {
        MpaDbStorage storage = new MpaDbStorage(jdbcTemplate);
        assertThat(storage.getAllMpa().size()).isEqualTo(5);
    }
}
