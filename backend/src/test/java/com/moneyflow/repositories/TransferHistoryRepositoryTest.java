package com.moneyflow.repositories;

import com.moneyflow.models.Transfer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransferHistoryRepositoryTest {

    private JdbcTemplate jdbc;
    private TransferHistoryRepository repository;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        repository = new TransferHistoryRepository(jdbc);
    }

    @Test
    void testSave() {
        Transfer transfer = new Transfer("A1", "A2", 200.0, 5, "USD");

        repository.save(transfer);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> argsCaptor = ArgumentCaptor.forClass(Object.class);

        verify(jdbc, times(1)).update(anyString(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testFindByUserId() {
        Transfer mockTransfer = new Transfer(
                1, "ACC1", "ACC2", 500.0, 3,
                new Timestamp(System.currentTimeMillis()), "RUB"
        );

        when(jdbc.query(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(List.of(mockTransfer));

        List<Transfer> result = repository.findByUserId(3);

        assertEquals(1, result.size());
        Transfer t = result.get(0);
        assertEquals("ACC1", t.getFromAccountNumber());
        assertEquals("RUB", t.getCurrency());
        assertEquals(3, t.getUserId());
    }
}
