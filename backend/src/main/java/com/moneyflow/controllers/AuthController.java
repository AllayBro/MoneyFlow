package com.moneyflow.controllers;

import com.moneyflow.models.User;
import com.moneyflow.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserRepository userRepo;

    public AuthController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User input) {
        logger.info("Попытка входа пользователя: {}", input.getUsername());

        if (input.getUsername() == null || input.getPassword() == null) {
            return error("Username и password обязательны");
        }

        Optional<User> userOptional = userRepo.findByUsername(input.getUsername());

        if (userOptional.isEmpty() || !userOptional.get().getPassword().equals(input.getPassword())) {
            return error("Неверное имя пользователя или пароль");
        }

        User user = userOptional.get();

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("message", "LoggedIn");

        logger.info("Успешный вход пользователя: {}", user.getUsername());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User input) {
        logger.info("Регистрация пользователя: {}", input.getUsername());

        if (input.getUsername() == null || input.getPassword() == null) {
            return error("Username и password обязательны");
        }

        if (userRepo.findByUsername(input.getUsername()).isPresent()) {
            return error("Пользователь с таким именем уже существует");
        }

        userRepo.save(input);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registered");
        response.put("username", input.getUsername());

        logger.info("Пользователь зарегистрирован: {}", input.getUsername());
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", message);
        return ResponseEntity.badRequest().body(body);
    }
}
