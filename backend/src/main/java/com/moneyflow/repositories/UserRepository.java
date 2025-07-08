package com.moneyflow.repositories;

import com.moneyflow.models.User;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<User> findByUsername(String username) {
        return jdbc.query("SELECT * FROM users WHERE username = ?", new Object[]{username}, rs -> {
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                return Optional.of(user);
            }
            return Optional.empty();
        });
    }

    public boolean save(User user) {
        try {
            int rows = jdbc.update("INSERT INTO users (username, password) VALUES (?, ?)",
                    user.getUsername(), user.getPassword());
            return rows == 1;
        } catch (DuplicateKeyException e) {
            // Можно логировать или пробрасывать, но в контроллере уже проверяется наличие пользователя
            return false;
        }
    }

}