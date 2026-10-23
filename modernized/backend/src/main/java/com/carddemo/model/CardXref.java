package com.carddemo.model;

import jakarta.persistence.*;

/** CARD-XREF-RECORD (CVACT03Y) */
@Entity
@Table(name = "card_xref")
public class CardXref {
    @Id
    @Column(length = 16)
    private String cardNum;
    private Long custId;
    private Long acctId;

    public String getCardNum() { return cardNum; }
    public void setCardNum(String v) { cardNum = v; }
    public Long getCustId() { return custId; }
    public void setCustId(Long v) { custId = v; }
    public Long getAcctId() { return acctId; }
    public void setAcctId(Long v) { acctId = v; }
}
