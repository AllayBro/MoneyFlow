package com.moneyflow.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyflow.models.Transfer;
import com.moneyflow.repositories.TransferHistoryRepository;
import com.moneyflow.services.TransferService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
public class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @MockBean
    private TransferHistoryRepository transferHistoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn200OnSuccessfulTransfer() throws Exception {
        Transfer request = new Transfer();
        request.setFromAccountNumber("ACC1");
        request.setToAccountNumber("ACC2");
        request.setAmount(100.0);
        request.setCurrency("RUB");
        request.setUserId(1);

        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isOk())
                .andExpect(content().string("Перевод выполнен"));

        Mockito.verify(transferService).transferByAccountNumbers("ACC1", "ACC2", 100.0, 1);
    }

    @Test
    void shouldReturn400WhenBusinessExceptionThrown() throws Exception {
        Transfer request = new Transfer();
        request.setFromAccountNumber("ACC1");
        request.setToAccountNumber("ACC2");
        request.setAmount(100.0);
        request.setCurrency("RUB");
        request.setUserId(1);

        // симулируем бизнес-ошибку
        Mockito.doThrow(new IllegalArgumentException("Ошибка")).when(transferService)
                .transferByAccountNumbers("ACC1", "ACC2", 100.0, 1);

        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Ошибка")));
    }

    @Test
    void shouldReturnTransferHistoryForUser() throws Exception {
        Transfer t1 = new Transfer();
        t1.setId(1);
        t1.setFromAccountNumber("A1");
        t1.setToAccountNumber("A2");
        t1.setAmount(100.0);
        t1.setUserId(1);
        t1.setCurrency("RUB");

        Mockito.when(transferHistoryRepository.findByUserId(1))
                .thenReturn(List.of(t1));

        mockMvc.perform(get("/api/transfer/history/1")
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromAccountNumber").value("A1"))
                .andExpect(jsonPath("$[0].toAccountNumber").value("A2"))
                .andExpect(jsonPath("$[0].amount").value(100.0));
    }
}
