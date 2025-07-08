package com.moneyflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

@SpringBootApplication
@Theme(value = "moneyflow")
public class FrontendApplication implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }
}