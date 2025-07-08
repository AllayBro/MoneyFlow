package com.moneyflow.views;

import com.moneyflow.models.TransferRequest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.web.client.RestTemplate;

public class TransferForm extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();

    private final TextField fromField = new TextField("С лицевого счёта");
    private final TextField toField = new TextField("На лицевой счёт");
    private final TextField amountField = new TextField("Сумма перевода");
    private final Button transferButton = new Button("Перевести");

    public TransferForm(AccountGrid accountGrid) {
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

        H3 title = new H3("Перевод между счетами");

        // Настройка полей
        fromField.setPlaceholder("например: 1234567890");
        toField.setPlaceholder("например: 0987654321");
        amountField.setPlaceholder("например: 100.00");

        fromField.setClearButtonVisible(true);
        toField.setClearButtonVisible(true);
        amountField.setClearButtonVisible(true);

        transferButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        transferButton.setWidthFull();
        transferButton.getStyle().set("margin-top", "1rem");

        FormLayout form = new FormLayout();
        form.add(fromField, toField, amountField);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("800px", 3)
        );

        add(title, form, transferButton);
        transferButton.addClickListener(e -> transfer(accountGrid));
    }

    private void transfer(AccountGrid accountGrid) {
        String from = fromField.getValue();
        String to = toField.getValue();
        String amountStr = amountField.getValue();
        Integer userId = (Integer) VaadinSession.getCurrent().getAttribute("userId");

        if (from == null || to == null || amountStr == null ||
                from.isBlank() || to.isBlank() || amountStr.isBlank()) {
            Notification.show("Пожалуйста, заполните все поля", 3000, Notification.Position.MIDDLE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Notification.show("Сумма должна быть положительной", 3000, Notification.Position.MIDDLE);
                return;
            }
        } catch (NumberFormatException ex) {
            Notification.show("Некорректный формат суммы", 3000, Notification.Position.MIDDLE);
            return;
        }

        TransferRequest request = new TransferRequest(from, to, amount, userId);

        try {
            restTemplate.postForEntity("http://localhost:3001/api/transfer", request, String.class);
            Notification.show("Перевод успешно выполнен", 3000, Notification.Position.TOP_CENTER);
            accountGrid.loadAccounts(); // обновить список счетов
        } catch (Exception ex) {
            Notification.show("Ошибка перевода: " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER);
        }

        // Очистка полей
        fromField.clear();
        toField.clear();
        amountField.clear();
    }
}
