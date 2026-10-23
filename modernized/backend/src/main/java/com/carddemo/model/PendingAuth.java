package com.carddemo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** AUTHFRDS Db2 table + IMS pending auth detail (app-authorization-ims-db2-mq) */
@Entity
@Table(name = "pending_auth")
@IdClass(PendingAuthId.class)
public class PendingAuth {
    @Id
    @Column(length = 16)
    private String cardNum;
    @Id
    @Column(length = 26)
    private String authTs;
    @Column(length = 4)
    private String authType;
    @Column(length = 4)
    private String cardExpiryDate;
    @Column(length = 6)
    private String messageType;
    @Column(length = 6)
    private String messageSource;
    @Column(length = 6)
    private String authIdCode;
    @Column(length = 2)
    private String authRespCode;
    @Column(length = 4)
    private String authRespReason;
    @Column(length = 6)
    private String processingCode;
    @Column(precision = 12, scale = 2)
    private BigDecimal transactionAmt;
    @Column(precision = 12, scale = 2)
    private BigDecimal approvedAmt;
    @Column(length = 4)
    private String merchantCategoryCode;
    @Column(length = 3)
    private String acqrCountryCode;
    private Integer posEntryMode;
    @Column(length = 15)
    private String merchantId;
    @Column(length = 22)
    private String merchantName;
    @Column(length = 13)
    private String merchantCity;
    @Column(length = 2)
    private String merchantState;
    @Column(length = 9)
    private String merchantZip;
    @Column(length = 15)
    private String transactionId;
    @Column(length = 1)
    private String matchStatus; // ' ' unmatched, 'M' matched
    @Column(length = 1)
    private String authFraud;   // 'Y' fraud
    @Column(length = 10)
    private String fraudRptDate;
    private Long acctId;
    private Long custId;

    public String getCardNum() { return cardNum; }
    public void setCardNum(String v) { cardNum = v; }
    public String getAuthTs() { return authTs; }
    public void setAuthTs(String v) { authTs = v; }
    public String getAuthType() { return authType; }
    public void setAuthType(String v) { authType = v; }
    public String getCardExpiryDate() { return cardExpiryDate; }
    public void setCardExpiryDate(String v) { cardExpiryDate = v; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String v) { messageType = v; }
    public String getMessageSource() { return messageSource; }
    public void setMessageSource(String v) { messageSource = v; }
    public String getAuthIdCode() { return authIdCode; }
    public void setAuthIdCode(String v) { authIdCode = v; }
    public String getAuthRespCode() { return authRespCode; }
    public void setAuthRespCode(String v) { authRespCode = v; }
    public String getAuthRespReason() { return authRespReason; }
    public void setAuthRespReason(String v) { authRespReason = v; }
    public String getProcessingCode() { return processingCode; }
    public void setProcessingCode(String v) { processingCode = v; }
    public BigDecimal getTransactionAmt() { return transactionAmt; }
    public void setTransactionAmt(BigDecimal v) { transactionAmt = v; }
    public BigDecimal getApprovedAmt() { return approvedAmt; }
    public void setApprovedAmt(BigDecimal v) { approvedAmt = v; }
    public String getMerchantCategoryCode() { return merchantCategoryCode; }
    public void setMerchantCategoryCode(String v) { merchantCategoryCode = v; }
    public String getAcqrCountryCode() { return acqrCountryCode; }
    public void setAcqrCountryCode(String v) { acqrCountryCode = v; }
    public Integer getPosEntryMode() { return posEntryMode; }
    public void setPosEntryMode(Integer v) { posEntryMode = v; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String v) { merchantId = v; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String v) { merchantName = v; }
    public String getMerchantCity() { return merchantCity; }
    public void setMerchantCity(String v) { merchantCity = v; }
    public String getMerchantState() { return merchantState; }
    public void setMerchantState(String v) { merchantState = v; }
    public String getMerchantZip() { return merchantZip; }
    public void setMerchantZip(String v) { merchantZip = v; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String v) { transactionId = v; }
    public String getMatchStatus() { return matchStatus; }
    public void setMatchStatus(String v) { matchStatus = v; }
    public String getAuthFraud() { return authFraud; }
    public void setAuthFraud(String v) { authFraud = v; }
    public String getFraudRptDate() { return fraudRptDate; }
    public void setFraudRptDate(String v) { fraudRptDate = v; }
    public Long getAcctId() { return acctId; }
    public void setAcctId(Long v) { acctId = v; }
    public Long getCustId() { return custId; }
    public void setCustId(Long v) { custId = v; }
}
