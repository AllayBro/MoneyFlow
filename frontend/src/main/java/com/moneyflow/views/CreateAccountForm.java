package com.moneyflow.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Форма для создания нового счёта пользователя.
 */
public class CreateAccountForm extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();

    private final TextField balanceField = new TextField("Начальный баланс");
    private final TextField currencyField = new TextField("Валюта (например, USD)");
    private final TextField accountNumberField = new TextField("Лицевой счёт");
    private final Button createButton = new Button("Создать счёт");

    private final Set<String> validCurrencyCodes = new HashSet<>();

    public CreateAccountForm(AccountGrid accountGrid) {
        setWidthFull();
        setSpacing(true);
        setPadding(true);
        getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("color", "var(--lumo-body-text-color)")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "8px")
                .set("padding", "1.5rem")
                .set("margin-top", "1rem")
                .set("box-shadow", "0 2px 8px rgba(0, 0, 0, 0.1)");

        // Валюты ISO 4217
        for (Currency currency : Currency.getAvailableCurrencies()) {
            validCurrencyCodes.add(currency.getCurrencyCode());
        }

        // Заголовок карточки
        H3 title = new H3("Создание нового счёта");

        // Настройка полей
        balanceField.setPlaceholder("например: 1000.00");
        currencyField.setPlaceholder("например: USD");
        accountNumberField.setPlaceholder("например: 1234567890");

        balanceField.setClearButtonVisible(true);
        currencyField.setClearButtonVisible(true);
        accountNumberField.setClearButtonVisible(true);

        // Кнопка
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.setWidthFull();
        createButton.getStyle().set("margin-top", "1rem");
        createButton.addClickListener(e -> createAccount(accountGrid));

        // Формат формы
        FormLayout form = new FormLayout();
        form.add(balanceField, currencyField, accountNumberField);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("800px", 3)
        );

        add(title, form, createButton);
    }

    /**
     * Отправляет запрос на создание счёта.
     */
    private void createAccount(AccountGrid accountGrid) {
        Integer userId = (Integer) VaadinSession.getCurrent().getAttribute("userId");

        String balanceStr = balanceField.getValue();
        String currency = currencyField.getValue();
        String accountNumber = accountNumberField.getValue();

        if (userId == null || balanceStr.isBlank() || currency.isBlank() || accountNumber.isBlank()) {
            Notification.show("Пожалуйста, заполните все поля", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (!validCurrencyCodes.contains(currency.toUpperCase())) {
            Notification.show("Неверный код валюты", 3000, Notification.Position.MIDDLE);
            return;
        }

        double balance;
        try {
            balance = Double.parseDouble(balanceStr);
            if (balance < 0) {
                Notification.show("Баланс не может быть отрицательным", 3000, Notification.Position.MIDDLE);
                return;
            }
        } catch (NumberFormatException ex) {
            Notification.show("Некорректный формат суммы", 3000, Notification.Position.MIDDLE);
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("initialBalance", balance);
        payload.put("currency", currency.toUpperCase());
        payload.put("accountNumber", accountNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForObject(
                    "http://localhost:3001/api/accounts/create",
                    request,
                    String.class
            );
            Notification.show("Счёт успешно создан", 3000, Notification.Position.TOP_CENTER);
            accountGrid.loadAccounts();

            balanceField.clear();
            currencyField.clear();
            accountNumberField.clear();
        } catch (Exception ex) {
            Notification.show("Ошибка при создании счёта: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER);
        }
    }
}
