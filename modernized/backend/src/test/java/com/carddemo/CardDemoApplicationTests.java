package com.carddemo;

import com.carddemo.repo.UserSecurityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CardDemoApplicationTests {

    @Autowired
    private UserSecurityRepository users;

    @Test
    void contextLoads() {
    }

    @Test
    void dataLoader_seedsUsrsecUsers() {
        assertTrue(users.count() > 0);
        assertTrue(users.findById("USER0001").isPresent());
        assertTrue(users.findById("ADMIN001").isPresent());
    }
}
