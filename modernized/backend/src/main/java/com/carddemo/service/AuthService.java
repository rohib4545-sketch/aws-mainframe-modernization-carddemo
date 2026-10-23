package com.carddemo.service;

import com.carddemo.config.ApiException;
import com.carddemo.model.UserSecurity;
import com.carddemo.repo.UserSecurityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** COSGN00C signon logic: validate credentials against USRSEC, issue a session token. */
@Service
public class AuthService {
    public record Session(String userId, String userType, String firstName, String lastName) {
        public boolean isAdmin() { return "A".equalsIgnoreCase(userType); }
    }

    private final UserSecurityRepository users;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public AuthService(UserSecurityRepository users) { this.users = users; }

    public Map<String, Object> login(String userId, String password) {
        if (userId == null || userId.isBlank()) {
            throw ApiException.badRequest("Please enter User ID ...");
        }
        if (password == null || password.isBlank()) {
            throw ApiException.badRequest("Please enter Password ...");
        }
        String uid = userId.toUpperCase();
        String pwd = password.toUpperCase();
        Optional<UserSecurity> found = users.findById(uid);
        if (found.isEmpty()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User not found. Try again ...");
        }
        UserSecurity u = found.get();
        if (!u.getPassword().equals(pwd)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Wrong Password. Try again ...");
        }
        String token = UUID.randomUUID().toString();
        sessions.put(token, new Session(uid, u.getUserType(), u.getFirstName(), u.getLastName()));
        return Map.of(
                "token", token,
                "userId", uid,
                "firstName", u.getFirstName(),
                "lastName", u.getLastName(),
                "userType", u.getUserType());
    }

    public void logout(String token) {
        if (token != null) sessions.remove(token);
    }

    public Session require(String token) {
        if (token == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Please enter User ID ...");
        }
        Session s = sessions.get(token);
        if (s == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Session expired. Please sign on again.");
        }
        return s;
    }

    public Session requireAdmin(String token) {
        Session s = require(token);
        if (!s.isAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No access - Admin Only option...");
        }
        return s;
    }
}
