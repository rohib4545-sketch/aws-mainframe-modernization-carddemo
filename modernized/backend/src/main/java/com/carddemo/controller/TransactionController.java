package com.carddemo.controller;

import com.carddemo.service.AuthService;
import com.carddemo.service.TransactionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class TransactionController {
    private final AuthService auth;
    private final TransactionService trans;
    private final int pageSize;

    public TransactionController(AuthService auth, TransactionService trans,
                                 @Value("${carddemo.page-size.transactions:10}") int pageSize) {
        this.auth = auth; this.trans = trans; this.pageSize = pageSize;
    }

    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> list(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestParam(required = false) String startId,
            @RequestParam(defaultValue = "0") int page) {
        auth.require(token);
        return ResponseEntity.ok(trans.list(startId, page, pageSize));
    }

    @GetMapping("/transactions/{tranId}")
    public ResponseEntity<Object> view(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable String tranId) {
        auth.require(token);
        return ResponseEntity.ok(trans.view(tranId));
    }

    @PostMapping("/transactions")
    public ResponseEntity<Map<String, Object>> add(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody Map<String, Object> body) {
        auth.require(token);
        return ResponseEntity.ok(trans.add(body));
    }

    /** CORPT00C — Transaction reports: monthly | yearly | custom(startDate,endDate). */
    @GetMapping("/reports/transactions")
    public ResponseEntity<Map<String, Object>> report(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestParam String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        auth.require(token);
        return ResponseEntity.ok(trans.report(type, startDate, endDate));
    }
}
