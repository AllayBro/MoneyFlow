package com.moneyflow.repositories;

import com.moneyflow.models.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class TransferHistoryRepository {

    private static final Logger logger = LoggerFactory.getLogger(TransferHistoryRepository.class);

    private final JdbcTemplate jdbc;

    public TransferHistoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void save(Transfer transfer) {
        String sql = """
            INSERT INTO transfers (from_account_number, to_account_number, amount, user_id, timestamp, currency)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        jdbc.update(sql,
                transfer.getFromAccountNumber(),
                transfer.getToAccountNumber(),
                transfer.getAmount(),
                transfer.getUserId(),
                new Timestamp(System.currentTimeMillis()),
                transfer.getCurrency()
        );

        logger.info("Сохранён перевод {} -> {} {} {}",
                transfer.getFromAccountNumber(),
                transfer.getToAccountNumber(),
                transfer.getAmount(),
                transfer.getCurrency()
        );
    }

    public List<Transfer> findByUserId(int userId) {
        String sql = """
            SELECT t.id, t.from_account_number, t.to_account_number,
                   t.amount, t.user_id, t.timestamp, t.currency
            FROM transfers t
            WHERE t.from_account_number IN (
                      SELECT account_number FROM accounts WHERE user_id = ?
                  )
               OR t.to_account_number IN (
                      SELECT account_number FROM accounts WHERE user_id = ?
                  )
            ORDER BY t.timestamp DESC
        """;

        return jdbc.query(sql, new Object[]{userId, userId}, (rs, rowNum) -> new Transfer(
                rs.getInt("id"),
                rs.getString("from_account_number"),
                rs.getString("to_account_number"),
                rs.getDouble("amount"),
                rs.getInt("user_id"),
                rs.getTimestamp("timestamp"),
                rs.getString("currency")
        ));
    }
}
