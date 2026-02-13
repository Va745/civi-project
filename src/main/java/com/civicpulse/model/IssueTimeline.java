package com.civicpulse.model;

import java.sql.Timestamp;

public class IssueTimeline {
    private int timelineId;
    private String issueId;
    private String status;
    private int updatedBy;
    private String remarks;
    private String proofImageUrl;
    private Timestamp createdAt;
    
    // Additional fields for display
    private String updatedByName;
    private String updatedByRole;

    // Constructors
    public IssueTimeline() {}

    public IssueTimeline(String issueId, String status, int updatedBy, String remarks) {
        this.issueId = issueId;
        this.status = status;
        this.updatedBy = updatedBy;
        this.remarks = remarks;
    }

    // Getters and Setters
    public int getTimelineId() {
        return timelineId;
    }

    public void setTimelineId(int timelineId) {
        this.timelineId = timelineId;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(int updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getProofImageUrl() {
        return proofImageUrl;
    }

    public void setProofImageUrl(String proofImageUrl) {
        this.proofImageUrl = proofImageUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }

    public String getUpdatedByRole() {
        return updatedByRole;
    }

    public void setUpdatedByRole(String updatedByRole) {
        this.updatedByRole = updatedByRole;
    }

    @Override
    public String toString() {
        return "IssueTimeline{" +
                "issueId='" + issueId + '\'' +
                ", status='" + status + '\'' +
                ", updatedBy=" + updatedBy +
                ", createdAt=" + createdAt +
                '}';
    }
}
