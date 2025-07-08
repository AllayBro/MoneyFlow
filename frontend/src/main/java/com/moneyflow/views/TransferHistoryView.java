package com.moneyflow.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Route("history")
@PageTitle("История переводов")
public class TransferHistoryView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Grid<Transfer> grid = new Grid<>(Transfer.class);
    private final TextField filterField = new TextField("Фильтр по счёту или валюте");

    private List<Transfer> allTransfers;
    private final Set<String> myAccounts = new HashSet<>();

    public TransferHistoryView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "var(--lumo-base-color)");
        getStyle().set("color", "var(--lumo-body-text-color)");

        configureFilterField();
        configureGrid();

        add(filterField, grid);
        loadTransfers();
    }

    private void configureFilterField() {
        filterField.setPlaceholder("Например: 123456 / USD / входящий");
        filterField.setClearButtonVisible(true);
        filterField.setWidth("400px");
        filterField.addValueChangeListener(e -> applyFilter());
    }

    private void configureGrid() {
        grid.removeAllColumns();
        grid.setWidthFull();
        grid.setHeight("500px");

        grid.addColumn(Transfer::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(Transfer::getFromAccountNumber).setHeader("Отправитель").setAutoWidth(true);
        grid.addColumn(Transfer::getToAccountNumber).setHeader("Получатель").setAutoWidth(true);
        grid.addColumn(Transfer::getAmount).setHeader("Сумма").setAutoWidth(true);
        grid.addColumn(Transfer::getCurrency).setHeader("Валюта").setAutoWidth(true);
        grid.addColumn(Transfer::getOperationType).setHeader("Тип").setAutoWidth(true);
        grid.addColumn(Transfer::getFormattedTimestamp).setHeader("Дата и время").setAutoWidth(true);
    }

    private void loadTransfers() {
        Integer userId = (Integer) VaadinSession.getCurrent().getAttribute("userId");
        if (userId == null) {
            Notification.show("Вы не авторизованы", 3000, Notification.Position.TOP_CENTER);
            return;
        }

        try {
            // Получение переводов
            Transfer[] transfers = restTemplate.getForObject(
                    "http://localhost:3001/api/transfer/history/" + userId,
                    Transfer[].class
            );
            allTransfers = Arrays.asList(transfers);

            // Получение аккаунтов
            Account[] accounts = restTemplate.getForObject(
                    "http://localhost:3001/api/accounts/user/" + userId,
                    Account[].class
            );
            myAccounts.clear();
            for (Account acc : accounts) {
                myAccounts.add(acc.accountNumber);
            }

            // Тип операции
            for (Transfer t : allTransfers) {
                boolean fromMine = myAccounts.contains(t.getFromAccountNumber());
                boolean toMine = myAccounts.contains(t.getToAccountNumber());

                if (fromMine && toMine) {
                    t.setOperationType("Личный");
                } else if (fromMine) {
                    t.setOperationType("Исходящий");
                } else if (toMine) {
                    t.setOperationType("Входящий");
                } else {
                    t.setOperationType("Прочее");
                }
            }

            applyFilter();
        } catch (Exception e) {
            Notification.show("Ошибка загрузки истории переводов", 5000, Notification.Position.TOP_CENTER);
            e.printStackTrace();
        }
    }

    private void applyFilter() {
        String filterText = filterField.getValue().trim().toLowerCase();

        if (filterText.isEmpty()) {
            grid.setItems(allTransfers);
            return;
        }

        List<Transfer> filtered = allTransfers.stream()
                .filter(t ->
                        t.getFromAccountNumber().toLowerCase().contains(filterText) ||
                                t.getToAccountNumber().toLowerCase().contains(filterText) ||
                                t.getCurrency().toLowerCase().contains(filterText) ||
                                t.getOperationType().toLowerCase().contains(filterText))
                .toList();

        grid.setItems(filtered);
    }

    // DTO-модель
    public static class Transfer {
        private int id;
        private String fromAccountNumber;
        private String toAccountNumber;
        private double amount;
        private String currency;
        private String operationType;
        private String timestamp;

        public int getId() { return id; }
        public String getFromAccountNumber() { return fromAccountNumber; }
        public String getToAccountNumber() { return toAccountNumber; }
        public double getAmount() { return amount; }
        public String getCurrency() { return currency; }
        public String getOperationType() { return operationType; }
        public String getTimestamp() { return timestamp; }

        public void setId(int id) { this.id = id; }
        public void setFromAccountNumber(String fromAccountNumber) { this.fromAccountNumber = fromAccountNumber; }
        public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }
        public void setAmount(double amount) { this.amount = amount; }
        public void setCurrency(String currency) { this.currency = currency; }
        public void setOperationType(String operationType) { this.operationType = operationType; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

        public String getFormattedTimestamp() {
            try {
                OffsetDateTime dt = OffsetDateTime.parse(timestamp);
                return dt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
            } catch (Exception e) {
                return timestamp;
            }
        }
    }

    private static class Account {
        public String accountNumber;
    }
}
