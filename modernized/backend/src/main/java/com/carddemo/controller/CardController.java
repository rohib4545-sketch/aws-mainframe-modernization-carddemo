package com.carddemo.controller;

import com.carddemo.service.AuthService;
import com.carddemo.service.CardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin
public class CardController {
    private final AuthService auth;
    private final CardService cards;
    private final int pageSize;

    public CardController(AuthService auth, CardService cards,
                          @Value("${carddemo.page-size.cards:7}") int pageSize) {
        this.auth = auth; this.cards = cards; this.pageSize = pageSize;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestParam(required = false) String acctId,
            @RequestParam(required = false) String cardNum,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size) {
        auth.require(token);
        return ResponseEntity.ok(cards.list(acctId, cardNum, page, size == null ? pageSize : size));
    }

    @GetMapping("/view")
    public ResponseEntity<Map<String, Object>> view(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestParam(required = false) String cardNum,
            @RequestParam(required = false) Long acctId) {
        auth.require(token);
        return ResponseEntity.ok(cards.view(cardNum, acctId));
    }

    @PutMapping("/{cardNum}")
    public ResponseEntity<Map<String, Object>> update(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable String cardNum,
            @RequestBody Map<String, Object> body) {
        auth.require(token);
        return ResponseEntity.ok(cards.update(cardNum, body));
    }
}
