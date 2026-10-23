package com.carddemo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** TRAN-CAT-BAL-RECORD (CVTRA01Y) */
@Entity
@Table(name = "tran_cat_bal")
@IdClass(TranCatBalId.class)
public class TranCatBal {
    @Id
    private Long acctId;
    @Id
    @Column(length = 2)
    private String typeCd;
    @Id
    private Integer catCd;
    @Column(precision = 11, scale = 2)
    private BigDecimal bal;

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long v) { acctId = v; }
    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String v) { typeCd = v; }
    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer v) { catCd = v; }
    public BigDecimal getBal() { return bal; }
    public void setBal(BigDecimal v) { bal = v; }
}
