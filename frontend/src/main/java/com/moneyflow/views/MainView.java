package com.moneyflow.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("main")
@PageTitle("Личный кабинет")
public class MainView extends VerticalLayout implements BeforeEnterObserver {

    private final AccountGrid accountGrid = new AccountGrid();

    public MainView() {
        // --- Общий стиль страницы ---
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("color", "var(--lumo-body-text-color)")
                .set("margin", "0")
                .set("gap", "0")
                .set("box-sizing", "border-box");

        // --- Получение пользователя ---
        String username = (String) VaadinSession.getCurrent().getAttribute("username");
        if (username == null) username = "Пользователь";

        H1 greeting = new H1("Добро пожаловать, " + username + "!");
        greeting.getStyle()
                .set("margin", "0")
                .set("font-weight", "700")
                .set("color", "var(--lumo-primary-text-color)");

        // --- Тема (тёмная по сохранению) ---
        Object darkAttr = VaadinSession.getCurrent().getAttribute("dark-theme");
        boolean isDark = darkAttr != null && (boolean) darkAttr;
        if (isDark) {
            UI.getCurrent().getElement().setAttribute("theme", "dark");
        }

        Checkbox darkToggle = new Checkbox("🌙 Тёмная тема");
        darkToggle.setValue(isDark);
        darkToggle.addValueChangeListener(event -> {
            boolean enabled = event.getValue();
            UI.getCurrent().getElement().setAttribute("theme", enabled ? "dark" : "");
            VaadinSession.getCurrent().setAttribute("dark-theme", enabled);
        });

        // --- Кнопки ---
        Button historyButton = new Button("История переводов", e -> UI.getCurrent().navigate("history"));
        Button refreshButton = new Button("Обновить", e -> accountGrid.loadAccounts());
        Button logoutButton = new Button("Выйти", e -> {
            VaadinSession.getCurrent().close();
            UI.getCurrent().navigate("");
        });

        historyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        logoutButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        // --- Верхняя панель ---
        HorizontalLayout actionButtons = new HorizontalLayout(historyButton, refreshButton, logoutButton, darkToggle);
        actionButtons.setSpacing(true);

        HorizontalLayout topBar = new HorizontalLayout(greeting, actionButtons);
        topBar.setWidthFull();
        topBar.setAlignItems(FlexComponent.Alignment.CENTER);
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topBar.getStyle().set("padding", "1rem");

        // --- Заголовок секции ---
        H2 sectionTitle = new H2("Ваши счета");
        sectionTitle.getStyle()
                .set("margin-top", "1rem")
                .set("margin-left", "1rem");

        // --- Компоненты интерфейса ---
        TransferForm transferForm = new TransferForm(accountGrid);
        CreateAccountForm createForm = new CreateAccountForm(accountGrid);

        transferForm.getStyle().set("margin-top", "1rem");
        createForm.getStyle().set("margin-top", "1rem");

        // --- Сборка интерфейса ---
        add(topBar, sectionTitle, accountGrid, transferForm, createForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (VaadinSession.getCurrent().getAttribute("username") == null) {
            event.rerouteTo("");
        }
    }
}
