package com.carddemo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** DIS-GROUP-RECORD (CVTRA02Y) */
@Entity
@Table(name = "disclosure_group")
@IdClass(DisclosureGroupId.class)
public class DisclosureGroup {
    @Id
    @Column(length = 10)
    private String acctGroupId;
    @Id
    @Column(length = 2)
    private String tranTypeCd;
    @Id
    private Integer tranCatCd;
    @Column(precision = 6, scale = 2)
    private BigDecimal intRate;

    public String getAcctGroupId() { return acctGroupId; }
    public void setAcctGroupId(String v) { acctGroupId = v; }
    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String v) { tranTypeCd = v; }
    public Integer getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(Integer v) { tranCatCd = v; }
    public BigDecimal getIntRate() { return intRate; }
    public void setIntRate(BigDecimal v) { intRate = v; }
}
