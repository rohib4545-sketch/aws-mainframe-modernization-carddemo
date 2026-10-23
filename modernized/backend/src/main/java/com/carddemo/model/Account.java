package com.carddemo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** ACCOUNT-RECORD (CVACT01Y) */
@Entity
@Table(name = "account")
public class Account {
    @Id
    private Long acctId;
    @Column(length = 1)
    private String activeStatus;
    @Column(precision = 12, scale = 2)
    private BigDecimal currBal;
    @Column(precision = 12, scale = 2)
    private BigDecimal creditLimit;
    @Column(precision = 12, scale = 2)
    private BigDecimal cashCreditLimit;
    @Column(length = 10)
    private String openDate;
    @Column(length = 10)
    private String expirationDate;
    @Column(length = 10)
    private String reissueDate;
    @Column(precision = 12, scale = 2)
    private BigDecimal currCycCredit;
    @Column(precision = 12, scale = 2)
    private BigDecimal currCycDebit;
    @Column(length = 10)
    private String addrZip;
    @Column(length = 10)
    private String groupId;

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long v) { acctId = v; }
    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String v) { activeStatus = v; }
    public BigDecimal getCurrBal() { return currBal; }
    public void setCurrBal(BigDecimal v) { currBal = v; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal v) { creditLimit = v; }
    public BigDecimal getCashCreditLimit() { return cashCreditLimit; }
    public void setCashCreditLimit(BigDecimal v) { cashCreditLimit = v; }
    public String getOpenDate() { return openDate; }
    public void setOpenDate(String v) { openDate = v; }
    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String v) { expirationDate = v; }
    public String getReissueDate() { return reissueDate; }
    public void setReissueDate(String v) { reissueDate = v; }
    public BigDecimal getCurrCycCredit() { return currCycCredit; }
    public void setCurrCycCredit(BigDecimal v) { currCycCredit = v; }
    public BigDecimal getCurrCycDebit() { return currCycDebit; }
    public void setCurrCycDebit(BigDecimal v) { currCycDebit = v; }
    public String getAddrZip() { return addrZip; }
    public void setAddrZip(String v) { addrZip = v; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String v) { groupId = v; }
}
