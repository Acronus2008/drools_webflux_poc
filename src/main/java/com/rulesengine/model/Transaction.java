package com.rulesengine.model;

 import java.math.BigDecimal;
import java.time.LocalDateTime;

 
public class Transaction {
    private String id;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String transactionType; // PURCHASE, WITHDRAWAL, TRANSFER, etc.
    private LocalDateTime timestamp;
    private String merchantId;
    private String country;
    private Integer riskScore;
    private String status; // PENDING, APPROVED, REJECTED
    private String rejectionReason;
    
    // Campos para reglas complejas
    private Integer userAge;
    private Integer accountAgeDays;
    private BigDecimal monthlyTransactionVolume;
    private Integer failedTransactionsLastMonth;
    private Boolean isVIP;
    private String accountTier; // BRONZE, SILVER, GOLD, PLATINUM

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Integer getUserAge() {
        return userAge;
    }

    public void setUserAge(Integer userAge) {
        this.userAge = userAge;
    }

    public Integer getAccountAgeDays() {
        return accountAgeDays;
    }

    public void setAccountAgeDays(Integer accountAgeDays) {
        this.accountAgeDays = accountAgeDays;
    }

    public BigDecimal getMonthlyTransactionVolume() {
        return monthlyTransactionVolume;
    }

    public void setMonthlyTransactionVolume(BigDecimal monthlyTransactionVolume) {
        this.monthlyTransactionVolume = monthlyTransactionVolume;
    }

    public Integer getFailedTransactionsLastMonth() {
        return failedTransactionsLastMonth;
    }

    public void setFailedTransactionsLastMonth(Integer failedTransactionsLastMonth) {
        this.failedTransactionsLastMonth = failedTransactionsLastMonth;
    }

    public Boolean getIsVIP() {
        return isVIP;
    }

    public void setIsVIP(Boolean isVIP) {
        this.isVIP = isVIP;
    }

    public String getAccountTier() {
        return accountTier;
    }

    public void setAccountTier(String accountTier) {
        this.accountTier = accountTier;
    }


}

