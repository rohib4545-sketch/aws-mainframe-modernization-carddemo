package com.carddemo.controller;

import com.carddemo.config.ApiException;
import com.carddemo.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AccountController {
    private final AuthService auth;
    private final AccountService accounts;
    private final BillPaymentService billPay;

    public AccountController(AuthService auth, AccountService accounts, BillPaymentService billPay) {
        this.auth = auth; this.accounts = accounts; this.billPay = billPay;
    }

    /** Account View — accepts acctId path or ?cardNum= lookup. */
    @GetMapping("/accounts/{acctId}")
    public ResponseEntity<Map<String, Object>> view(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable Long acctId) {
        auth.require(token);
        return ResponseEntity.ok(accounts.view(acctId));
    }

    /** Resolve card number or account id to an account (both screens accept either). */
    @GetMapping("/accounts")
    public ResponseEntity<Map<String, Object>> lookup(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestParam(required = false) Long acctId,
            @RequestParam(required = false) String cardNum) {
        auth.require(token);
        return ResponseEntity.ok(accounts.view(accounts.resolveAcctId(acctId, cardNum)));
    }

    /** Account Update. */
    @PutMapping("/accounts/{acctId}")
    public ResponseEntity<Map<String, Object>> update(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable Long acctId,
            @RequestBody Map<String, Object> body) {
        auth.require(token);
        return ResponseEntity.ok(accounts.update(acctId, body));
    }

    /** Bill Payment view (COBIL00C entry). */
    @GetMapping("/accounts/{acctId}/bill-payment")
    public ResponseEntity<Map<String, Object>> billPayView(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable Long acctId) {
        auth.require(token);
        return ResponseEntity.ok(billPay.view(acctId));
    }

    /** Bill Payment confirm (COBIL00C ENTER with CONFIRM=Y). */
    @PostMapping("/accounts/{acctId}/bill-payment")
    public ResponseEntity<Map<String, Object>> billPayPost(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable Long acctId,
            @RequestBody(required = false) Map<String, String> body) {
        auth.require(token);
        String confirm = body == null ? "" : body.getOrDefault("confirm", "");
        return ResponseEntity.ok(billPay.pay(acctId, confirm));
    }
}
