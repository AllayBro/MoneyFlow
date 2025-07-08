package com.moneyflow.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AccountModelTest {

    @Test
    void testDefaultConstructorAndSetters() {
        Account account = new Account();

        account.setId(10);
        account.setUserId(100);
        account.setBalance(2500.50);
        account.setCurrency("USD");
        account.setAccountNumber("ACC999");

        assertEquals(10, account.getId());
        assertEquals(100, account.getUserId());
        assertEquals(2500.50, account.getBalance());
        assertEquals("USD", account.getCurrency());
        assertEquals("ACC999", account.getAccountNumber());
    }

    @Test
    void testAllArgsConstructor() {
        Account account = new Account(1, 2, 1000.0, "RUB", "ACC123");

        assertEquals(1, account.getId());
        assertEquals(2, account.getUserId());
        assertEquals(1000.0, account.getBalance());
        assertEquals("RUB", account.getCurrency());
        assertEquals("ACC123", account.getAccountNumber());
    }
}
