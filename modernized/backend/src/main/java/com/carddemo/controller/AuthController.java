package com.carddemo.controller;

import com.carddemo.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(auth.login(body.get("userId"), body.get("password")));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        auth.logout(token);
        return ResponseEntity.ok(Map.of("message", "Signed off"));
    }
}
