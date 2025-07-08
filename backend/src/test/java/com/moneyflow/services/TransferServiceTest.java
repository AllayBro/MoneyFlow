package com.moneyflow.services;

import com.moneyflow.models.Account;
import com.moneyflow.models.Transfer;
import com.moneyflow.repositories.AccountRepository;
import com.moneyflow.repositories.TransferHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

public class TransferServiceTest {

    private AccountRepository accountRepo;
    private TransferHistoryRepository historyRepo;
    private TransferService transferService;

    @BeforeEach
    void setup() {
        accountRepo = mock(AccountRepository.class);
        historyRepo = mock(TransferHistoryRepository.class);
        transferService = new TransferService(accountRepo, historyRepo);
    }

    @Test
    void transferSuccess() {
        Account from = new Account(1, 1, 1000.0, "RUB", "ACC1");
        Account to = new Account(2, 2, 100.0, "RUB", "ACC2");

        when(accountRepo.findByAccountNumber("ACC1")).thenReturn(from);
        when(accountRepo.findByAccountNumber("ACC2")).thenReturn(to);

        transferService.transferByAccountNumbers("ACC1", "ACC2", 200.0, 1);

        verify(accountRepo).updateBalance(from.getId(), 800.0);
        verify(accountRepo).updateBalance(to.getId(), 300.0);
        verify(historyRepo).save(any(Transfer.class));
    }

    @Test
    void transferFailsDueToInsufficientFunds() {
        Account from = new Account(1, 1, 50.0, "RUB", "ACC1");
        Account to = new Account(2, 2, 100.0, "RUB", "ACC2");

        when(accountRepo.findByAccountNumber("ACC1")).thenReturn(from);
        when(accountRepo.findByAccountNumber("ACC2")).thenReturn(to);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                transferService.transferByAccountNumbers("ACC1", "ACC2", 100.0, 1)
        );

        assertTrue(ex.getMessage().contains("Недостаточно средств"));
    }

    @Test
    void transferFailsIfRecipientNotFound() {
        Account from = new Account(1, 1, 500.0, "RUB", "ACC1");

        when(accountRepo.findByAccountNumber("ACC1")).thenReturn(from);
        when(accountRepo.findByAccountNumber("ACC2")).thenReturn(null); // получатель не найден

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                transferService.transferByAccountNumbers("ACC1", "ACC2", 100.0, 1)
        );

        assertTrue(ex.getMessage().contains("счет") || ex.getMessage().contains("не существует"));
    }

    @Test
    void transferFailsToSameAccount() {
        Account from = new Account(1, 1, 500.0, "RUB", "ACC1");

        when(accountRepo.findByAccountNumber("ACC1")).thenReturn(from);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                transferService.transferByAccountNumbers("ACC1", "ACC1", 100.0, 1)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("тот же самый"));
    }
}
