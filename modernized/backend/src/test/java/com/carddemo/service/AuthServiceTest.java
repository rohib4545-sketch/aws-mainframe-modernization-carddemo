package com.carddemo.service;

import com.carddemo.config.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Sign-on (COSGN00C) behavior against the seeded USRSEC data. */
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService auth;

    @Test
    void login_withValidUserCredentials_returnsTokenAndProfile() {
        Map<String, Object> res = auth.login("USER0001", "PASSWORD");
        assertNotNull(res.get("token"));
        assertEquals("USER0001", res.get("userId"));
        assertEquals("U", res.get("userType"));
        assertNotNull(res.get("firstName"));
    }

    @Test
    void login_isCaseInsensitiveForUserIdAndPassword() {
        Map<String, Object> res = auth.login("user0001", "password");
        assertEquals("USER0001", res.get("userId"));
    }

    @Test
    void login_withValidAdminCredentials_returnsAdminType() {
        Map<String, Object> res = auth.login("ADMIN001", "PASSWORD");
        assertEquals("A", res.get("userType"));
    }

    @Test
    void login_withWrongPassword_throwsUnauthorized() {
        ApiException ex = assertThrows(ApiException.class,
                () -> auth.login("USER0001", "WRONG"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertTrue(ex.getMessage().contains("Wrong Password"));
    }

    @Test
    void login_withUnknownUser_throwsUnauthorized() {
        ApiException ex = assertThrows(ApiException.class,
                () -> auth.login("NOUSER99", "PASSWORD"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void login_withBlankFields_throwsBadRequest() {
        assertThrows(ApiException.class, () -> auth.login("", "PASSWORD"));
        assertThrows(ApiException.class, () -> auth.login("USER0001", ""));
        assertThrows(ApiException.class, () -> auth.login(null, null));
    }

    @Test
    void require_returnsSessionForValidToken() {
        String token = (String) auth.login("USER0001", "PASSWORD").get("token");
        AuthService.Session s = auth.require(token);
        assertEquals("USER0001", s.userId());
        assertFalse(s.isAdmin());
    }

    @Test
    void requireAdmin_rejectsRegularUserSession() {
        String token = (String) auth.login("USER0001", "PASSWORD").get("token");
        ApiException ex = assertThrows(ApiException.class, () -> auth.requireAdmin(token));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void requireAdmin_acceptsAdminSession() {
        String token = (String) auth.login("ADMIN001", "PASSWORD").get("token");
        AuthService.Session s = auth.requireAdmin(token);
        assertTrue(s.isAdmin());
    }

    @Test
    void logout_invalidatesToken() {
        String token = (String) auth.login("USER0001", "PASSWORD").get("token");
        auth.logout(token);
        ApiException ex = assertThrows(ApiException.class, () -> auth.require(token));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void require_withUnknownToken_throwsUnauthorized() {
        assertThrows(ApiException.class, () -> auth.require("not-a-real-token"));
    }
}
