package com.carddemo.repo;

import com.carddemo.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface TranRecordRepository extends JpaRepository<TranRecord, String> {
    @Query("SELECT MAX(t.tranId) FROM TranRecord t") String maxTranId();
    @Query("SELECT t FROM TranRecord t WHERE t.tranId >= :start ORDER BY t.tranId")
    List<TranRecord> browse(@Param("start") String start, Pageable pageable);
    @Query("SELECT t FROM TranRecord t WHERE t.origTs >= :from AND t.origTs <= :to ORDER BY t.origTs")
    List<TranRecord> findInDateRange(@Param("from") String from, @Param("to") String to);
    List<TranRecord> findByCardNumOrderByOrigTs(String cardNum);
}
