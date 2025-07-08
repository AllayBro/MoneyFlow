package com.moneyflow.repositories;

import com.moneyflow.models.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AccountRepositoryTest {

    private JdbcTemplate jdbc;
    private AccountRepository repository;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        repository = new AccountRepository(jdbc);
    }

    @Test
    void testFindByUserId() {
        Account mockAccount = new Account(1, 2, 1000.0, "USD", "ACC123");

        when(jdbc.query(eq("SELECT * FROM accounts WHERE user_id = ?"),
                any(Object[].class),
                any(RowMapper.class)))
                .thenReturn(List.of(mockAccount));

        List<Account> result = repository.findByUserId(2);

        assertEquals(1, result.size());
        assertEquals("ACC123", result.get(0).getAccountNumber());
    }

    @Test
    void testCreateAccount() {
        repository.createAccount(1, 500.0, "RUB", "ACC456");

        verify(jdbc, times(1)).update(
                eq("INSERT INTO accounts (user_id, balance, currency, account_number) VALUES (?, ?, ?, ?)"),
                eq(1), eq(500.0), eq("RUB"), eq("ACC456")
        );
    }

    @Test
    void testDeleteAccountByIdSuccess() {
        when(jdbc.update(anyString(), anyInt())).thenReturn(1);

        boolean deleted = repository.deleteAccountById(42);

        assertTrue(deleted);
        verify(jdbc).update("DELETE FROM accounts WHERE id = ?", 42);
    }

    @Test
    void testDeleteAccountByIdFailure() {
        when(jdbc.update(anyString(), anyInt())).thenReturn(0);

        boolean deleted = repository.deleteAccountById(99);

        assertFalse(deleted);
    }

    @Test
    void testFindByAccountNumberReturnsAccount() {
        Account mock = new Account(5, 3, 750.0, "EUR", "ACC789");

        when(jdbc.query(eq("SELECT * FROM accounts WHERE account_number = ?"),
                any(Object[].class),
                any(RowMapper.class)))
                .thenReturn(List.of(mock));

        Account result = repository.findByAccountNumber("ACC789");

        assertNotNull(result);
        assertEquals("EUR", result.getCurrency());
    }

    @Test
    void testFindByAccountNumberReturnsNull() {
        when(jdbc.query(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(List.of());

        Account result = repository.findByAccountNumber("NOT_FOUND");
        assertNull(result);
    }

    @Test
    void testUpdateBalance() {
        repository.updateBalance(7, 999.99);

        verify(jdbc).update("UPDATE accounts SET balance = ? WHERE id = ?", 999.99, 7);
    }
}
