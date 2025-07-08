package com.moneyflow.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserModelTest {

    @Test
    void testDefaultConstructorAndSetters() {
        User user = new User();

        user.setId(1);
        user.setUsername("testuser");
        user.setPassword("secret");

        assertEquals(1, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("secret", user.getPassword());
    }

    @Test
    void testAllArgsConstructor() {
        User user = new User(2, "admin", "admin123");

        assertEquals(2, user.getId());
        assertEquals("admin", user.getUsername());
        assertEquals("admin123", user.getPassword());
    }
}
