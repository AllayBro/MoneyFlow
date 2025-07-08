package com.moneyflow.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyflow.models.Account;
import com.moneyflow.repositories.AccountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAccountsListForUser() throws Exception {
        Account account1 = new Account(1, 1, 1000.0, "RUB", "ACC123");
        Account account2 = new Account(2, 1, 500.0, "USD", "ACC456");

        when(accountRepository.findByUserId(1)).thenReturn(List.of(account1, account2));

        mockMvc.perform(get("/api/accounts/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].accountNumber").value("ACC123"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].balance").value(500.0));
    }

    @Test
    void shouldCreateAccountSuccessfully() throws Exception {
        Map<String, Object> request = Map.of(
                "userId", 1,
                "initialBalance", 1000.0,
                "currency", "RUB",
                "accountNumber", "ACC789"
        );

        // Метод createAccount не возвращает ничего, но может выбросить исключение
        doNothing().when(accountRepository).createAccount(
                1,
                1000.0,
                "RUB",
                "ACC789"
        );

        mockMvc.perform(post("/api/accounts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Счёт успешно создан"));
    }

    @Test
    void shouldDeleteAccountSuccessfully() throws Exception {
        when(accountRepository.deleteAccountById(1)).thenReturn(true);

        mockMvc.perform(delete("/api/accounts/delete")
                        .param("accountId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Счёт успешно удалён"));
    }

    @Test
    void shouldReturnBadRequestWhenDeleteAccountFails() throws Exception {
        when(accountRepository.deleteAccountById(1)).thenReturn(false);

        mockMvc.perform(delete("/api/accounts/delete")
                        .param("accountId", "1"))
                .andExpect(status().isBadRequest());
    }
}
