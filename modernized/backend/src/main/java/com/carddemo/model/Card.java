package com.carddemo.model;

import jakarta.persistence.*;

/** CARD-RECORD (CVACT02Y) */
@Entity
@Table(name = "card")
public class Card {
    @Id
    @Column(length = 16)
    private String cardNum;
    private Long acctId;
    private Integer cvvCode;
    @Column(length = 50)
    private String embossedName;
    @Column(length = 10)
    private String expirationDate;
    @Column(length = 1)
    private String activeStatus;

    public String getCardNum() { return cardNum; }
    public void setCardNum(String v) { cardNum = v; }
    public Long getAcctId() { return acctId; }
    public void setAcctId(Long v) { acctId = v; }
    public Integer getCvvCode() { return cvvCode; }
    public void setCvvCode(Integer v) { cvvCode = v; }
    public String getEmbossedName() { return embossedName; }
    public void setEmbossedName(String v) { embossedName = v; }
    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String v) { expirationDate = v; }
    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String v) { activeStatus = v; }
}
