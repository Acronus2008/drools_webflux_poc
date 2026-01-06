package com.rulesengine.model;

import java.util.ArrayList;
import java.util.List;

 
public class RuleResult {
    private String transactionId;
    private String status; // APPROVED, REJECTED, PENDING_REVIEW
    private List<String> appliedRules = new ArrayList<>();
    private List<String> reasons = new ArrayList<>();
    private Integer finalRiskScore;
    private Long processingTimeMs;
    private String complexityLevel; // LOW, MEDIUM, HIGH

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getAppliedRules() {
        return appliedRules;
    }

    public void setAppliedRules(List<String> appliedRules) {
        this.appliedRules = appliedRules;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public Integer getFinalRiskScore() {
        return finalRiskScore;
    }

    public void setFinalRiskScore(Integer finalRiskScore) {
        this.finalRiskScore = finalRiskScore;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public String getComplexityLevel() {
        return complexityLevel;
    }

    public void setComplexityLevel(String complexityLevel) {
        this.complexityLevel = complexityLevel;
    }


}

