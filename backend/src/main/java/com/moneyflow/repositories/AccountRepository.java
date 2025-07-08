package com.moneyflow.repositories;

import com.moneyflow.models.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AccountRepository {
    private final JdbcTemplate jdbc;

    public AccountRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Account> findByUserId(int userId) {
        return jdbc.query("SELECT * FROM accounts WHERE user_id = ?",
                new Object[]{userId},
                (rs, rowNum) -> new Account(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getDouble("balance"),
                        rs.getString("currency"),
                        rs.getString("account_number")
                ));
    }

    public void createAccount(int userId, double initialBalance, String currency, String accountNumber) {
        jdbc.update(
                "INSERT INTO accounts (user_id, balance, currency, account_number) VALUES (?, ?, ?, ?)",
                userId, initialBalance, currency, accountNumber
        );
    }

    public boolean deleteAccountById(int accountId) {
        String sql = "DELETE FROM accounts WHERE id = ?";
        int rows = jdbc.update(sql, accountId);
        return rows > 0;
    }

    // 🆕 Метод для поиска счёта по номеру
    public Account findByAccountNumber(String accountNumber) {
        List<Account> list = jdbc.query("SELECT * FROM accounts WHERE account_number = ?",
                new Object[]{accountNumber},
                (rs, rowNum) -> new Account(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getDouble("balance"),
                        rs.getString("currency"),
                        rs.getString("account_number")
                ));

        return list.isEmpty() ? null : list.get(0);
    }

    // 🆕 Метод обновления баланса
    public void updateBalance(int accountId, double newBalance) {
        jdbc.update("UPDATE accounts SET balance = ? WHERE id = ?", newBalance, accountId);
    }
}
