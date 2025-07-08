package com.moneyflow.models;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TransferModelTest {

    @Test
    void testDefaultConstructorAndSetters() {
        Transfer transfer = new Transfer();

        Timestamp now = Timestamp.valueOf(LocalDateTime.of(2024, 1, 15, 14, 30));

        transfer.setId(1);
        transfer.setFromAccountNumber("ACC1");
        transfer.setToAccountNumber("ACC2");
        transfer.setAmount(500.0);
        transfer.setUserId(99);
        transfer.setTimestamp(now);
        transfer.setCurrency("RUB");
        transfer.setOperationType("Исходящий");

        assertEquals(1, transfer.getId());
        assertEquals("ACC1", transfer.getFromAccountNumber());
        assertEquals("ACC2", transfer.getToAccountNumber());
        assertEquals(500.0, transfer.getAmount());
        assertEquals(99, transfer.getUserId());
        assertEquals(now, transfer.getTimestamp());
        assertEquals("RUB", transfer.getCurrency());
        assertEquals("Исходящий", transfer.getOperationType());

        String formatted = transfer.getFormattedTimestamp();
        assertTrue(formatted.matches("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testAllArgsConstructor() {
        Timestamp timestamp = Timestamp.valueOf("2023-12-01 10:00:00");
        Transfer transfer = new Transfer(10, "A1", "A2", 1000.0, 5, timestamp, "USD");

        assertEquals(10, transfer.getId());
        assertEquals("A1", transfer.getFromAccountNumber());
        assertEquals("A2", transfer.getToAccountNumber());
        assertEquals(1000.0, transfer.getAmount());
        assertEquals(5, transfer.getUserId());
        assertEquals(timestamp, transfer.getTimestamp());
        assertEquals("USD", transfer.getCurrency());
    }

    @Test
    void testNewTransferConstructor() {
        Transfer transfer = new Transfer("SRC", "DST", 150.0, 3, "EUR");

        assertEquals("SRC", transfer.getFromAccountNumber());
        assertEquals("DST", transfer.getToAccountNumber());
        assertEquals(150.0, transfer.getAmount());
        assertEquals(3, transfer.getUserId());
        assertEquals("EUR", transfer.getCurrency());
        assertNull(transfer.getTimestamp());
    }

    @Test
    void testFormattedTimestampNullSafe() {
        Transfer transfer = new Transfer();
        transfer.setTimestamp(null);
        assertEquals("", transfer.getFormattedTimestamp());
    }

    @Test
    void testToStringNotNull() {
        Transfer transfer = new Transfer();
        assertNotNull(transfer.toString());
    }
}
