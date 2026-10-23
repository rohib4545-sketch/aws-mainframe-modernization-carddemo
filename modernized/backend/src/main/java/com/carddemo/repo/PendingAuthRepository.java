package com.carddemo.repo;

import com.carddemo.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface PendingAuthRepository extends JpaRepository<PendingAuth, PendingAuthId> {
    List<PendingAuth> findByCardNumOrderByAuthTsDesc(String cardNum);
    List<PendingAuth> findByAcctIdOrderByAuthTsDesc(Long acctId);
    List<PendingAuth> findAllByOrderByAuthTsDesc();
}
