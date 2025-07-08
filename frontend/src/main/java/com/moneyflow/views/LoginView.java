package com.moneyflow.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import elemental.json.Json;
import elemental.json.JsonObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Route("")
public class LoginView extends VerticalLayout {

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background-color", "var(--lumo-base-color)");
        getStyle().set("color", "var(--lumo-body-text-color)");

        String sessionUsername = (String) VaadinSession.getCurrent().getAttribute("username");
        if (sessionUsername != null) {
            getUI().ifPresent(ui -> ui.navigate("main"));
            return;
        }

        H2 title = new H2("Вход в систему");
        title.getStyle().set("margin-bottom", "1rem");

        TextField usernameField = new TextField("Email");
        usernameField.setPlaceholder("example@mail.com");
        usernameField.setClearButtonVisible(true);
        usernameField.setWidth("300px");

        PasswordField passwordField = new PasswordField("Пароль");
        passwordField.setPlaceholder("Введите пароль");
        passwordField.setClearButtonVisible(true);
        passwordField.setWidth("300px");

        Button loginBtn = new Button("Войти");
        loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button registerBtn = new Button("Регистрация");
        registerBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        loginBtn.setWidth("300px");
        registerBtn.setWidth("300px");

        add(title, usernameField, passwordField, loginBtn, registerBtn);

        loginBtn.addClickListener(e ->
                login(usernameField.getValue().trim(), passwordField.getValue().trim()));

        registerBtn.addClickListener(e ->
                register(usernameField.getValue().trim(), passwordField.getValue().trim()));
    }

    private void login(String username, String password) {
        if (username.isBlank() || password.isBlank()) {
            Notification.show("Введите логин и пароль", 3000, Notification.Position.MIDDLE);
            return;
        }

        try {
            HttpURLConnection conn = createPostConnection("http://localhost:3001/api/auth/login");

            String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
            sendJson(conn, json);

            if (conn.getResponseCode() == 200) {
                try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                    String response = scanner.useDelimiter("\\A").next();
                    JsonObject jsonObj = Json.parse(response);

                    if (jsonObj.hasKey("error")) {
                        Notification.show(jsonObj.getString("error"), 3000, Notification.Position.MIDDLE);
                        return;
                    }

                    int userId = (int) jsonObj.getNumber("id");
                    String receivedUsername = jsonObj.getString("username");

                    VaadinSession.getCurrent().setAttribute("username", receivedUsername);
                    VaadinSession.getCurrent().setAttribute("userId", userId);

                    getUI().ifPresent(ui -> ui.navigate("main"));
                }
            } else {
                Notification.show("Неверный логин или пароль", 3000, Notification.Position.MIDDLE);
            }
        } catch (Exception ex) {
            Notification.show("Ошибка при входе", 3000, Notification.Position.MIDDLE);
            ex.printStackTrace();
        }
    }

    private void register(String username, String password) {
        if (username.isBlank() || password.isBlank()) {
            Notification.show("Введите email и пароль", 3000, Notification.Position.MIDDLE);
            return;
        }

        try {
            HttpURLConnection conn = createPostConnection("http://localhost:3001/api/auth/register");

            String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
            sendJson(conn, json);

            try (Scanner scanner = new Scanner(
                    conn.getResponseCode() == 200 ? conn.getInputStream() : conn.getErrorStream(),
                    StandardCharsets.UTF_8)) {

                String response = scanner.useDelimiter("\\A").next();
                JsonObject jsonObj = Json.parse(response);

                if (jsonObj.hasKey("error")) {
                    Notification.show(jsonObj.getString("error"), 3000, Notification.Position.MIDDLE);
                } else if ("Registered".equals(jsonObj.getString("message"))) {
                    Notification.show("Регистрация прошла успешно", 3000, Notification.Position.TOP_CENTER);
                } else {
                    Notification.show("Регистрация не удалась", 3000, Notification.Position.MIDDLE);
                }
            }
        } catch (Exception ex) {
            Notification.show("Ошибка регистрации", 3000, Notification.Position.MIDDLE);
            ex.printStackTrace();
        }
    }

    private HttpURLConnection createPostConnection(String endpoint) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        return conn;
    }

    private void sendJson(HttpURLConnection conn, String json) throws Exception {
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }
}
