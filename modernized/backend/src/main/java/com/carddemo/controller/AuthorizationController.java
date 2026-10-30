package com.carddemo.controller;

import com.carddemo.service.AuthService;
import com.carddemo.service.AuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/authorizations")
@CrossOrigin
public class AuthorizationController {
    private final AuthService auth;
    private final AuthorizationService auths;

    public AuthorizationController(AuthService auth, AuthorizationService auths) {
        this.auth = auth;
        this.auths = auths;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> summary(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestParam(required = false) Long acctId) {
        auth.require(token);
        return ResponseEntity.ok(auths.summary(acctId));
    }

    @GetMapping("/{cardNum}/{authTs}")
    public ResponseEntity<Map<String, Object>> detail(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable String cardNum,
            @PathVariable String authTs) {
        auth.require(token);
        return ResponseEntity.ok(auths.detail(cardNum, authTs.replace('_', ' ')));
    }

    @PostMapping("/{cardNum}/{authTs}/match")
    public ResponseEntity<Map<String, Object>> match(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable String cardNum,
            @PathVariable String authTs,
            @RequestBody(required = false) Map<String, String> body) {
        auth.require(token);
        return ResponseEntity.ok(auths.markMatched(cardNum, authTs.replace('_', ' '),
                body == null ? null : body.get("transactionId")));
    }

    @PostMapping("/{cardNum}/{authTs}/fraud")
    public ResponseEntity<Map<String, Object>> fraud(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable String cardNum,
            @PathVariable String authTs,
            @RequestBody(required = false) Map<String, String> body) {
        auth.require(token);
        return ResponseEntity.ok(auths.markFraud(cardNum, authTs.replace('_', ' '),
                body == null ? "Y" : body.getOrDefault("fraud", "Y")));
    }
}
