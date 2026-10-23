package com.carddemo.repo;

import com.carddemo.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface TranCategoryRepository extends JpaRepository<TranCategory, TranCategoryId> {
    List<TranCategory> findByTypeCdOrderByCatCd(String typeCd);
}
