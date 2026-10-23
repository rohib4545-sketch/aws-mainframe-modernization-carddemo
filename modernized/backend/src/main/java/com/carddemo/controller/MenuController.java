package com.carddemo.controller;

import com.carddemo.service.AuthService;
import com.carddemo.service.AuthService.Session;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class MenuController {
    private final AuthService auth;
    public MenuController(AuthService auth) { this.auth = auth; }

    private record MenuOption(int num, String name, String program, String route) {}

    private static final List<MenuOption> USER_MENU = List.of(
            new MenuOption(1, "Account View", "COACTVWC", "/account/view"),
            new MenuOption(2, "Account Update", "COACTUPC", "/account/update"),
            new MenuOption(3, "Credit Card List", "COCRDLIC", "/cards"),
            new MenuOption(4, "Credit Card View", "COCRDSLC", "/cards/view"),
            new MenuOption(5, "Credit Card Update", "COCRDUPC", "/cards/update"),
            new MenuOption(6, "Transaction List", "COTRN00C", "/transactions"),
            new MenuOption(7, "Transaction View", "COTRN01C", "/transactions/view"),
            new MenuOption(8, "Transaction Add", "COTRN02C", "/transactions/add"),
            new MenuOption(9, "Transaction Reports", "CORPT00C", "/reports"),
            new MenuOption(10, "Bill Payment", "COBIL00C", "/bill-payment"),
            new MenuOption(11, "Pending Authorization View", "COPAUS0C", "/authorizations"));

    private static final List<MenuOption> ADMIN_MENU = List.of(
            new MenuOption(1, "User List (Security)", "COUSR00C", "/admin/users"),
            new MenuOption(2, "User Add (Security)", "COUSR01C", "/admin/users/add"),
            new MenuOption(3, "User Update (Security)", "COUSR02C", "/admin/users/update"),
            new MenuOption(4, "User Delete (Security)", "COUSR03C", "/admin/users/delete"),
            new MenuOption(5, "Transaction Type List/Update (Db2)", "COTRTLIC", "/admin/tran-types"),
            new MenuOption(6, "Transaction Type Maintenance (Db2)", "COTRTUPC", "/admin/tran-types/add"),
            new MenuOption(7, "Batch: Interest Calculation", "CBACT04C", "/admin/batch/interest"),
            new MenuOption(8, "Batch: Account Statement", "CBSTM03A", "/admin/batch/statement"));

    @GetMapping("/menus")
    public ResponseEntity<Map<String, Object>> menus(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        Session s = auth.require(token);
        List<MenuOption> options = s.isAdmin() ? ADMIN_MENU : USER_MENU;
        return ResponseEntity.ok(Map.of(
                "menuType", s.isAdmin() ? "admin" : "user",
                "options", options));
    }

    /** CDRD — inquire system date (MQ module analog). */
    @GetMapping("/system/date")
    public ResponseEntity<Map<String, Object>> systemDate(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        auth.require(token);
        return ResponseEntity.ok(Map.of(
                "date", com.carddemo.service.ValidationUtil.today(),
                "timestamp", com.carddemo.service.ValidationUtil.now()));
    }
}
