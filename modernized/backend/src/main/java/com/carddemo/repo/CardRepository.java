package com.carddemo.repo;

import com.carddemo.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {
    List<Card> findByAcctIdOrderByCardNum(Long acctId);
    @Query("SELECT c FROM Card c WHERE (:acctId IS NULL OR c.acctId = :acctId) AND (:cardNum IS NULL OR c.cardNum >= :cardNum) ORDER BY c.cardNum")
    List<Card> browse(@Param("acctId") Long acctId, @Param("cardNum") String cardNum, Pageable pageable);
}
