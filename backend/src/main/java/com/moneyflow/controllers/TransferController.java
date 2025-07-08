package com.moneyflow.controllers;

import com.moneyflow.models.Transfer;
import com.moneyflow.repositories.TransferHistoryRepository;
import com.moneyflow.services.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/transfer")
public class TransferController {

    private static final Logger logger = LoggerFactory.getLogger(TransferController.class);

    private final TransferService transferService;
    private final TransferHistoryRepository transferHistoryRepository;

    public TransferController(TransferService transferService,
                              TransferHistoryRepository transferHistoryRepository) {
        this.transferService = transferService;
        this.transferHistoryRepository = transferHistoryRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> transfer(@RequestBody Transfer request) {
        logger.info("Запрос перевода: from={} to={} amount={} userId={}",
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount(),
                request.getUserId());

        // Простая валидация
        if (request.getFromAccountNumber() == null || request.getToAccountNumber() == null || request.getAmount() <= 0) {
            return error("Некорректные данные перевода");
        }

        try {
            transferService.transferByAccountNumbers(
                    request.getFromAccountNumber(),
                    request.getToAccountNumber(),
                    request.getAmount(),
                    request.getUserId()
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка перевода: {}", e.getMessage());
            return error(e.getMessage());
        }

        logger.info("Перевод успешно выполнен для userId={}", request.getUserId());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Перевод выполнен");
        return ResponseEntity.ok(response);
    }


    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getTransferHistory(@PathVariable int userId) {
        logger.info("Получение истории переводов: userId={}", userId);

        List<Transfer> history = transferHistoryRepository.findByUserId(userId);

        logger.debug("История переводов: {} записей для userId={}", history.size(), userId);
        return ResponseEntity.ok(history);
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", message);
        return ResponseEntity.badRequest().body(body);
    }
}
