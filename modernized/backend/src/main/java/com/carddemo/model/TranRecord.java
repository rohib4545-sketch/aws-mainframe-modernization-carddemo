package com.carddemo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** TRAN-RECORD (CVTRA05Y / CVTRA06Y) */
@Entity
@Table(name = "transaction")
public class TranRecord {
    @Id
    @Column(length = 16)
    private String tranId;
    @Column(length = 2)
    private String typeCd;
    private Integer catCd;
    @Column(length = 10)
    private String source;
    @Column(length = 100, name = "descr")
    private String desc;
    @Column(precision = 11, scale = 2)
    private BigDecimal amt;
    private Long merchantId;
    @Column(length = 50)
    private String merchantName;
    @Column(length = 50)
    private String merchantCity;
    @Column(length = 10)
    private String merchantZip;
    @Column(length = 16)
    private String cardNum;
    @Column(length = 26)
    private String origTs;
    @Column(length = 26)
    private String procTs;

    public String getTranId() { return tranId; }
    public void setTranId(String v) { tranId = v; }
    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String v) { typeCd = v; }
    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer v) { catCd = v; }
    public String getSource() { return source; }
    public void setSource(String v) { source = v; }
    public String getDesc() { return desc; }
    public void setDesc(String v) { desc = v; }
    public BigDecimal getAmt() { return amt; }
    public void setAmt(BigDecimal v) { amt = v; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long v) { merchantId = v; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String v) { merchantName = v; }
    public String getMerchantCity() { return merchantCity; }
    public void setMerchantCity(String v) { merchantCity = v; }
    public String getMerchantZip() { return merchantZip; }
    public void setMerchantZip(String v) { merchantZip = v; }
    public String getCardNum() { return cardNum; }
    public void setCardNum(String v) { cardNum = v; }
    public String getOrigTs() { return origTs; }
    public void setOrigTs(String v) { origTs = v; }
    public String getProcTs() { return procTs; }
    public void setProcTs(String v) { procTs = v; }
}
