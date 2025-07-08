package com.moneyflow.services;

import com.moneyflow.models.Account;
import com.moneyflow.models.Transfer;
import com.moneyflow.repositories.AccountRepository;
import com.moneyflow.repositories.TransferHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);

    private final AccountRepository accountRepository;
    private final TransferHistoryRepository transferHistoryRepository;

    public TransferService(AccountRepository accountRepository,
                           TransferHistoryRepository transferHistoryRepository) {
        this.accountRepository = accountRepository;
        this.transferHistoryRepository = transferHistoryRepository;
    }

    /**
     * Основная логика перевода между счётами.
     */
    @Transactional
    public void transferByAccountNumbers(String fromAccountNumber, String toAccountNumber, double amount, int userId) {
        long start = System.currentTimeMillis();

        validateInput(fromAccountNumber, toAccountNumber, amount);

        logger.info("Попытка перевода {} с {} на {} пользователем ID={}", amount, fromAccountNumber, toAccountNumber, userId);

        Account from = accountRepository.findByAccountNumber(fromAccountNumber);
        Account to = accountRepository.findByAccountNumber(toAccountNumber);

        validateAccounts(from, to, userId, fromAccountNumber, toAccountNumber, amount);

        double newFromBalance = from.getBalance() - amount;
        double newToBalance = to.getBalance() + amount;

        accountRepository.updateBalance(from.getId(), newFromBalance);
        accountRepository.updateBalance(to.getId(), newToBalance);

        Transfer transfer = new Transfer(fromAccountNumber, toAccountNumber, amount, userId, from.getCurrency());
        transferHistoryRepository.save(transfer);

        long time = System.currentTimeMillis() - start;
        logger.info("✅ Перевод выполнен: {} → {} на сумму {} {} за {} мс",
                fromAccountNumber, toAccountNumber, amount, from.getCurrency(), time);
    }

    private void validateInput(String from, String to, double amount) {
        if (from == null || to == null || from.isBlank() || to.isBlank()) {
            throw new IllegalArgumentException("Номера счетов не могут быть пустыми");
        }

        if (from.equals(to)) {
            throw new IllegalArgumentException("Нельзя перевести на тот же самый счёт");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");
        }
    }

    private void validateAccounts(Account from, Account to, int userId,
                                  String fromAccountNumber, String toAccountNumber, double amount) {
        if (from == null || to == null) {
            logger.warn("Не найден один из счетов: {} или {}", fromAccountNumber, toAccountNumber);
            throw new IllegalArgumentException("Один из указанных счетов не существует");
        }

        if (from.getUserId() != userId) {
            logger.warn("Попытка перевода с чужого счёта {} пользователем ID={}", fromAccountNumber, userId);
            throw new IllegalArgumentException("Вы не владелец счёта отправителя");
        }

        if (!from.getCurrency().equals(to.getCurrency())) {
            logger.warn("Несовпадение валют: {} vs {}", from.getCurrency(), to.getCurrency());
            throw new IllegalArgumentException("Валюты счётов должны совпадать");
        }

        if (from.getBalance() < amount) {
            logger.warn("Недостаточно средств на счёте {}. Баланс: {}, требуется: {}", fromAccountNumber, from.getBalance(), amount);
            throw new IllegalArgumentException("Недостаточно средств на счёте");
        }
    }
}
