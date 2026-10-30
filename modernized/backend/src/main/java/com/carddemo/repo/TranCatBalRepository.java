package com.carddemo.repo;

import com.carddemo.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface TranCatBalRepository extends JpaRepository<TranCatBal, TranCatBalId> {
    List<TranCatBal> findByAcctIdOrderByTypeCdAscCatCdAsc(Long acctId);
}
