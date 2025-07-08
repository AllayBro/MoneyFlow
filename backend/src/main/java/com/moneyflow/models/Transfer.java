package com.moneyflow.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Модель для представления перевода и истории переводов.
 */
public class Transfer {

    private int id;
    private String fromAccountNumber;
    private String toAccountNumber;
    private double amount;
    private int userId;
    private Timestamp timestamp;
    private String currency;         // ✅ добавлено
    private String operationType;    // ✅ добавлено (входящий / исходящий / личный)

    public Transfer() {}

    // Полный конструктор
    public Transfer(int id, String fromAccountNumber, String toAccountNumber, double amount, int userId, Timestamp timestamp, String currency) {
        this.id = id;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.userId = userId;
        this.timestamp = timestamp;
        this.currency = currency;
    }

    // Конструктор для создания нового перевода
    public Transfer(String fromAccountNumber, String toAccountNumber, double amount, int userId, String currency) {
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.userId = userId;
        this.currency = currency;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getFormattedTimestamp() {
        if (timestamp == null) return "";
        LocalDateTime dateTime = timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

    @Override
    public String toString() {
        return "Transfer{" +
                "id=" + id +
                ", fromAccountNumber='" + fromAccountNumber + '\'' +
                ", toAccountNumber='" + toAccountNumber + '\'' +
                ", amount=" + amount +
                ", userId=" + userId +
                ", timestamp=" + timestamp +
                ", currency='" + currency + '\'' +
                ", operationType='" + operationType + '\'' +
                '}';
    }
}
