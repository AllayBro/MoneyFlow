package com.moneyflow.repositories;

import com.moneyflow.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserRepositoryTest {

    private JdbcTemplate jdbc;
    private UserRepository repository;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        repository = new UserRepository(jdbc);
    }

    @Test
    void testFindByUsernameFound() throws SQLException {
        // Мокаем ResultSet
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("username")).thenReturn("testuser");
        when(rs.getString("password")).thenReturn("secret");

        // Каптурим ResultSetExtractor и вызываем его вручную
        ArgumentCaptor<ResultSetExtractor<Optional<User>>> captor = ArgumentCaptor.forClass(ResultSetExtractor.class);

        when(jdbc.query(eq("SELECT * FROM users WHERE username = ?"), any(Object[].class), captor.capture()))
                .thenReturn(Optional.of(new User(1, "testuser", "secret")));

        Optional<User> result = repository.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void testFindByUsernameNotFound() {
        when(jdbc.query(eq("SELECT * FROM users WHERE username = ?"), any(Object[].class), any(ResultSetExtractor.class)))
                .thenReturn(Optional.empty());

        Optional<User> result = repository.findByUsername("nouser");
        assertTrue(result.isEmpty());
    }

    @Test
    void testSaveSuccess() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("1234");

        when(jdbc.update(anyString(), eq("newuser"), eq("1234"))).thenReturn(1);

        boolean success = repository.save(user);
        assertTrue(success);

        verify(jdbc).update("INSERT INTO users (username, password) VALUES (?, ?)", "newuser", "1234");
    }

    @Test
    void testSaveDuplicateFails() {
        User user = new User();
        user.setUsername("existing");
        user.setPassword("pwd");

        when(jdbc.update(anyString(), any(), any()))
                .thenThrow(new DuplicateKeyException("duplicate"));

        boolean result = repository.save(user);
        assertFalse(result);
    }
}
