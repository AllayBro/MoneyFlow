package com.moneyflow.controllers;

import com.moneyflow.models.Account;
import com.moneyflow.repositories.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Account>> getAccountsByUserId(@PathVariable int userId) {
        logger.info("Получен запрос на получение счетов пользователя ID={}", userId);
        List<Account> accounts = accountRepository.findByUserId(userId);
        logger.debug("Найдено {} счетов для пользователя ID={}", accounts.size(), userId);
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createAccount(@RequestBody Map<String, Object> payload) {
        logger.info("Запрос на создание счёта: {}", payload);

        try {
            int userId = (int) payload.get("userId");
            double initialBalance = Double.parseDouble(payload.get("initialBalance").toString());
            String currency = payload.get("currency").toString();
            String accountNumber = payload.get("accountNumber").toString();

            if (currency == null || currency.isBlank() || accountNumber == null || accountNumber.isBlank()) {
                throw new IllegalArgumentException("Валюта и номер счёта обязательны");
            }

            if (initialBalance < 0) {
                throw new IllegalArgumentException("Начальный баланс не может быть отрицательным");
            }

            accountRepository.createAccount(userId, initialBalance, currency, accountNumber);
            logger.info("Счёт успешно создан: userId={}, номер счёта={}", userId, accountNumber);
            return ResponseEntity.ok("Счёт успешно создан");

        } catch (NullPointerException | ClassCastException e) {
            throw new IllegalArgumentException("Некорректный формат данных для создания счёта");
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAccount(@RequestParam int accountId) {
        logger.info("Запрос на удаление счёта ID={}", accountId);

        boolean deleted = accountRepository.deleteAccountById(accountId);
        if (!deleted) {
            logger.warn("Счёт ID={} не найден или не удалён", accountId);
            throw new IllegalArgumentException("Счёт не найден или уже удалён: ID=" + accountId);
        }

        logger.info("Счёт ID={} успешно удалён", accountId);
        return ResponseEntity.ok("Счёт успешно удалён");
    }
}
