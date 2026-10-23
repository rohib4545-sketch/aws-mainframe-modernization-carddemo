package com.carddemo.service;

import com.carddemo.config.ApiException;
import com.carddemo.model.UserSecurity;
import com.carddemo.repo.UserSecurityRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** COUSR00C (list), COUSR01C (add), COUSR02C (update), COUSR03C (delete). Admin only. */
@Service
public class UserService {
    private final UserSecurityRepository users;

    public UserService(UserSecurityRepository users) { this.users = users; }

    public Map<String, Object> list(String startId, int page, int size) {
        var sort = Sort.by("userId");
        List<UserSecurity> all;
        if (!ValidationUtil.isBlank(startId)) {
            all = users.findAll(sort).stream()
                    .filter(u -> u.getUserId().compareTo(startId.trim().toUpperCase()) >= 0).toList();
        } else {
            all = users.findAll(sort);
        }
        int from = Math.max(page, 0) * size;
        List<UserSecurity> slice = all.stream().skip(from).limit(size).toList();
        Map<String, Object> out = new HashMap<>();
        out.put("users", slice.stream().map(UserService::publicView).toList());
        out.put("page", Math.max(page, 0));
        out.put("pageSize", size);
        out.put("hasNextPage", all.size() > from + size);
        out.put("hasPrevPage", page > 0);
        out.put("total", all.size());
        return out;
    }

    public Map<String, Object> get(String userId) {
        return publicView(find(userId));
    }

    public Map<String, Object> add(Map<String, String> in) {
        String userId = req(in, "userId", "User ID can NOT be empty...").toUpperCase();
        String first = req(in, "firstName", "First Name can NOT be empty...");
        String last = req(in, "lastName", "Last Name can NOT be empty...");
        String pwd = req(in, "password", "Password can NOT be empty...");
        String type = req(in, "userType", "User Type can NOT be empty...").toUpperCase();
        if (!"A".equals(type) && !"U".equals(type)) {
            throw ApiException.badRequest("User Type must be A or U");
        }
        if (userId.length() > 8) throw ApiException.badRequest("User ID must be 8 characters or less");
        if (users.existsById(userId)) throw ApiException.badRequest("User ID already exist...");
        UserSecurity u = new UserSecurity();
        u.setUserId(userId); u.setFirstName(first); u.setLastName(last);
        u.setPassword(pwd); u.setUserType(type);
        users.save(u);
        return Map.of("message", "User " + userId + " has been added ...", "user", publicView(u));
    }

    public Map<String, Object> update(String userId, Map<String, String> in) {
        UserSecurity u = find(userId);
        if (!ValidationUtil.isBlank(in.get("firstName"))) u.setFirstName(in.get("firstName").trim());
        if (!ValidationUtil.isBlank(in.get("lastName"))) u.setLastName(in.get("lastName").trim());
        if (!ValidationUtil.isBlank(in.get("password"))) u.setPassword(in.get("password").trim());
        if (!ValidationUtil.isBlank(in.get("userType"))) {
            String t = in.get("userType").trim().toUpperCase();
            if (!"A".equals(t) && !"U".equals(t)) throw ApiException.badRequest("User Type must be A or U");
            u.setUserType(t);
        }
        users.save(u);
        return Map.of("message", "User " + u.getUserId() + " has been updated ...", "user", publicView(u));
    }

    public Map<String, Object> delete(String userId, String currentUserId) {
        UserSecurity u = find(userId);
        if (u.getUserId().equalsIgnoreCase(currentUserId)) {
            throw ApiException.badRequest("Cannot delete the currently signed on user");
        }
        users.delete(u);
        return Map.of("message", "User " + u.getUserId() + " has been deleted ...");
    }

    private UserSecurity find(String userId) {
        if (ValidationUtil.isBlank(userId)) throw ApiException.badRequest("User ID can NOT be empty...");
        return users.findById(userId.trim().toUpperCase())
                .orElseThrow(() -> ApiException.notFound("User ID NOT found..."));
    }

    private static String req(Map<String, String> in, String k, String msg) {
        String v = in.get(k);
        if (ValidationUtil.isBlank(v)) throw ApiException.badRequest(msg);
        return v.trim();
    }

    private static Map<String, Object> publicView(UserSecurity u) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId", u.getUserId());
        m.put("firstName", u.getFirstName());
        m.put("lastName", u.getLastName());
        m.put("userType", u.getUserType());
        return m;
    }
}
