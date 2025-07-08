package com.moneyflow.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import elemental.json.*;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Route("accounts")
@PageTitle("Управление счетами")
public class AccountView extends VerticalLayout implements BeforeEnterObserver {

    private final Grid<JsonObject> grid = new Grid<>();
    private final NumberField balanceField = new NumberField("Начальный баланс");
    private final TextField currencyField = new TextField("Валюта (например, RUB)");
    private final TextField accountNumberField = new TextField("Лицевой счёт");
    private final Button addButton = new Button("Создать счёт");

    public AccountView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "var(--lumo-base-color)");
        getStyle().set("color", "var(--lumo-body-text-color)");

        H2 title = new H2("Ваши счета");
        title.getStyle().set("margin-bottom", "1rem");

        configureGrid();
        configureForm();

        add(title, balanceField, currencyField, accountNumberField, addButton, grid);

        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.setWidth("300px");
        addButton.addClickListener(e -> addAccount());

        grid.asSingleSelect().addValueChangeListener(event -> {
            JsonObject selected = event.getValue();
            if (selected != null) {
                deleteAccount((int) selected.getNumber("id"));
            }
        });

        loadAccounts();
    }

    private void configureGrid() {
        grid.setWidthFull();
        grid.setHeight("400px");
        grid.removeAllColumns();

        grid.addColumn(obj -> (int) obj.getNumber("id")).setHeader("ID").setAutoWidth(true);
        grid.addColumn(obj -> formatBalance(obj.getNumber("balance"))).setHeader("Баланс").setAutoWidth(true);
        grid.addColumn(obj -> obj.getString("currency")).setHeader("Валюта").setAutoWidth(true);
        grid.addColumn(obj -> {
            JsonValue val = obj.get("accountNumber");
            return val != null && val.getType() == JsonType.STRING ? val.asString() : "(пусто)";
        }).setHeader("Лицевой счёт").setAutoWidth(true);
    }

    private void configureForm() {
        balanceField.setPlaceholder("например: 1000.00");
        currencyField.setPlaceholder("например: RUB");
        accountNumberField.setPlaceholder("например: 1234567890");

        balanceField.setWidth("300px");
        currencyField.setWidth("300px");
        accountNumberField.setWidth("300px");

        balanceField.setClearButtonVisible(true);
        currencyField.setClearButtonVisible(true);
        accountNumberField.setClearButtonVisible(true);
    }

    private void loadAccounts() {
        Integer userId = (Integer) VaadinSession.getCurrent().getAttribute("userId");
        if (userId == null) {
            showNotification("Пользователь не авторизован");
            return;
        }

        try {
            URL url = new URL("http://localhost:3001/api/accounts/user/" + userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            try (var scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                String response = scanner.useDelimiter("\\A").next();

                JsonValue jsonValue = Json.parse(response);
                if (jsonValue instanceof JsonArray arr) {
                    List<JsonObject> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JsonValue val = arr.get(i);
                        if (val instanceof JsonObject obj) {
                            list.add(obj);
                        }
                    }
                    grid.setItems(list);
                } else {
                    showNotification("Ошибка: получен очень странный ответ");
                }
            }
        } catch (Exception ex) {
            showNotification("Ошибка загрузки счетов");
            ex.printStackTrace();
        }
    }

    private void addAccount() {
        Integer userId = (Integer) VaadinSession.getCurrent().getAttribute("userId");
        if (userId == null) {
            showNotification("Пользователь не авторизован");
            return;
        }

        Double balance = balanceField.getValue();
        String currency = currencyField.getValue();
        String accountNumber = accountNumberField.getValue();

        if (balance == null || currency == null || currency.isBlank() || accountNumber == null || accountNumber.isBlank()) {
            showNotification("Заполните все поля");
            return;
        }

        try {
            String urlStr = String.format(
                    "http://localhost:3001/api/accounts/create?userId=%d&initialBalance=%s&currency=%s&accountNumber=%s",
                    userId, balance, currency, accountNumber);
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            int code = conn.getResponseCode();
            if (code == 200) {
                showNotification("Счёт успешно создан");
                loadAccounts();
                balanceField.clear();
                currencyField.clear();
                accountNumberField.clear();
            } else {
                showNotification("Ошибка создания счёта");
            }
        } catch (Exception ex) {
            showNotification("Ошибка создания счёта");
            ex.printStackTrace();
        }
    }

    private void deleteAccount(int accountId) {
        try {
            URL url = new URL("http://localhost:3001/api/accounts/delete?id=" + accountId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");

            int code = conn.getResponseCode();
            if (code == 200) {
                showNotification("Счёт удалён");
                loadAccounts();
            } else {
                showNotification("Ошибка удаления счёта");
            }
        } catch (Exception ex) {
            showNotification("Ошибка удаления счёта");
            ex.printStackTrace();
        }
    }

    private void showNotification(String message) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER);
    }

    private String formatBalance(double value) {
        return String.format("%,.2f", value);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (VaadinSession.getCurrent().getAttribute("username") == null) {
            event.rerouteTo("");
        }
    }
}
