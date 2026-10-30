package com.carddemo.controller;

import com.carddemo.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AdminController {
    private final AuthService auth;
    private final UserService users;
    private final ReferenceService refs;
    private final BatchService batch;
    private final int pageSize;

    public AdminController(AuthService auth, UserService users, ReferenceService refs,
                           BatchService batch,
                           @Value("${carddemo.page-size.users:10}") int pageSize) {
        this.auth = auth;
        this.users = users;
        this.refs = refs;
        this.batch = batch;
        this.pageSize = pageSize;
    }

    // ---- User security management (COUSR00/01/02/03) ----

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> listUsers(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestParam(required = false) String startId,
            @RequestParam(defaultValue = "0") int page) {
        auth.requireAdmin(token);
        return ResponseEntity.ok(users.list(startId, page, pageSize));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUser(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable String userId) {
        auth.requireAdmin(token);
        return ResponseEntity.ok(users.get(userId));
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> addUser(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody Map<String, String> body) {
        auth.requireAdmin(token);
        return ResponseEntity.ok(users.add(body));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {
        auth.requireAdmin(token);
        return ResponseEntity.ok(users.update(userId, body));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable String userId) {
        var s = auth.requireAdmin(token);
        return ResponseEntity.ok(users.delete(userId, s.userId()));
    }

    // ---- Transaction type maintenance (COTRTLI/COTRTUP, Db2 module) ----

    @GetMapping("/tran-types")
    public ResponseEntity<Object> listTypes(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        auth.require(token);
        return ResponseEntity.ok(refs.listTypes());
    }

    @PostMapping("/tran-types")
    public ResponseEntity<Object> addType(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody Map<String, String> body) {
        auth.requireAdmin(token);
        return ResponseEntity.ok(refs.addType(body.get("typeCd"), body.get("desc")));
    }

    @PutMapping("/tran-types/{typeCd}")
    public ResponseEntity<Object> updateType(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable String typeCd,
            @RequestBody Map<String, String> body) {
        auth.requireAdmin(token);
        return ResponseEntity.ok(refs.updateType(typeCd, body.get("desc")));
    }

    @DeleteMapping("/tran-types/{typeCd}")
    public ResponseEntity<Map<String, Object>> deleteType(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable String typeCd) {
        auth.requireAdmin(token);
        return ResponseEntity.ok(refs.deleteType(typeCd));
    }

    @GetMapping("/tran-categories")
    public ResponseEntity<Object> listCategories(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestParam(required = false) String typeCd) {
        auth.require(token);
        return ResponseEntity.ok(refs.listCategories(typeCd));
    }

    // ---- Batch jobs (INTCALC, CREASTMT, CBPAUP0J) ----

    @PostMapping("/batch/interest")
    public ResponseEntity<Map<String, Object>> interest(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        auth.requireAdmin(token);
        return ResponseEntity.ok(batch.interestCalculation());
    }

    @PostMapping("/batch/statement/{acctId}")
    public ResponseEntity<Map<String, Object>> statement(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @PathVariable Long acctId,
            @RequestBody(required = false) Map<String, String> body) {
        auth.requireAdmin(token);
        String start = body == null ? null : body.get("startDate");
        String end = body == null ? null : body.get("endDate");
        return ResponseEntity.ok(batch.statement(acctId, start, end));
    }

    @PostMapping("/batch/purge-authorizations")
    public ResponseEntity<Map<String, Object>> purgeAuths(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestParam(defaultValue = "90") int days) {
        auth.requireAdmin(token);
        return ResponseEntity.ok(batch.purgeAuthorizations(days));
    }
}
