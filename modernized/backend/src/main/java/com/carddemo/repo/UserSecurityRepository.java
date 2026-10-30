package com.carddemo.repo;

import com.carddemo.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface UserSecurityRepository extends JpaRepository<UserSecurity, String> {
    Optional<UserSecurity> findByUserId(String userId);
}
