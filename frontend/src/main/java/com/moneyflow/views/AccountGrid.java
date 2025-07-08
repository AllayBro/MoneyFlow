package com.moneyflow.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;

public class AccountGrid extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Grid<Account> grid = new Grid<>(Account.class);
    private final Button deleteButton = new Button("Удалить счёт");

    public AccountGrid() {
        setWidthFull();
        setPadding(true);
        setSpacing(true);

        configureGrid();
        configureDeleteButton();

        add(grid, deleteButton);
        loadAccounts();
    }

    private void configureGrid() {
        grid.removeAllColumns();
        grid.setWidthFull();
        grid.setHeight("300px");

        grid.addColumn(Account::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(account -> formatBalance(account.getBalance()))
                .setHeader("Баланс")
                .setAutoWidth(true);
        grid.addColumn(Account::getCurrency).setHeader("Валюта").setAutoWidth(true);
        grid.addColumn(Account::getAccountNumber).setHeader("Лицевой счёт").setAutoWidth(true);
    }

    private void configureDeleteButton() {
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.getStyle().set("margin-top", "1rem");
        deleteButton.addClickListener(e -> deleteSelectedAccount());
    }

    public void loadAccounts() {
        Integer userId = (Integer) VaadinSession.getCurrent().getAttribute("userId");
        if (userId == null) {
            Notification.show("Пользователь не авторизован");
            return;
        }

        try {
            Account[] accounts = restTemplate.getForObject(
                    "http://localhost:3001/api/accounts/user/" + userId,
                    Account[].class
            );
            grid.setItems(accounts);
        } catch (Exception ex) {
            Notification.show("Ошибка при загрузке счетов: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER);
        }
    }

    private void deleteSelectedAccount() {
        Account selected = grid.asSingleSelect().getValue();
        if (selected == null) {
            Notification.show("Выберите счёт для удаления");
            return;
        }

        if (selected.getBalance() > 0) {
            Dialog balanceDialog = new Dialog();
            balanceDialog.setHeaderTitle("На счету есть средства");

            String msg = "На счёте №" + selected.getAccountNumber() +
                    " есть " + formatBalance(selected.getBalance()) + " " + selected.getCurrency() +
                    ". Вы уверены, что хотите закрыть его?";

            balanceDialog.add(new Span(msg));

            Button next = new Button("Продолжить", e -> {
                balanceDialog.close();
                showFinalDeleteDialog(selected);
            });
            Button cancel = new Button("Отмена", e -> balanceDialog.close());

            balanceDialog.getFooter().add(new HorizontalLayout(next, cancel));
            balanceDialog.open();
        } else {
            showFinalDeleteDialog(selected);
        }
    }

    private void showFinalDeleteDialog(Account selected) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Подтверждение");

        confirmDialog.add(new Span("Удалить счёт №" + selected.getAccountNumber() + "?"));

        Button confirm = new Button("Удалить", e -> {
            confirmDialog.close();
            deleteAccount(selected.getId());
        });
        Button cancel = new Button("Отмена", e -> confirmDialog.close());

        confirmDialog.getFooter().add(new HorizontalLayout(confirm, cancel));
        confirmDialog.open();
    }

    private void deleteAccount(int accountId) {
        try {
            restTemplate.delete("http://localhost:3001/api/accounts/delete?accountId=" + accountId);
            Notification.show("Счёт успешно удалён", 3000, Notification.Position.TOP_CENTER);
            loadAccounts();
        } catch (Exception ex) {
            Notification.show("Ошибка при удалении счёта: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER);
        }
    }

    private String formatBalance(double value) {
        return new DecimalFormat("#,##0.00").format(value);
    }

    // DTO только для чтения
    public static class Account {
        private int id;
        private double balance;
        private String currency;
        private String accountNumber;

        public int getId() { return id; }
        public double getBalance() { return balance; }
        public String getCurrency() { return currency; }
        public String getAccountNumber() { return accountNumber; }
    }
}
