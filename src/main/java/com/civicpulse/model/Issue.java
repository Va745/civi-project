package com.civicpulse.model;

import java.sql.Timestamp;

public class Issue {
    private String issueId;
    private String category; // ROAD, WATER, SANITATION, ELECTRICITY
    private double locationLat;
    private double locationLng;
    private String address;
    private String description;
    private String status; // REPORTED, ASSIGNED, IN_PROGRESS, RESOLVED
    private int reportCount;
    private Integer deptId;
    private String imageUrl;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp resolvedAt;

    // Constructors
    public Issue() {}

    public Issue(String category, double locationLat, double locationLng, 
                 String address, String description) {
        this.category = category;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
        this.address = address;
        this.description = description;
        this.status = "REPORTED";
        this.reportCount = 1;
    }

    // Getters and Setters
    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(double locationLat) {
        this.locationLat = locationLat;
    }

    public double getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(double locationLng) {
        this.locationLng = locationLng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getReportCount() {
        return reportCount;
    }

    public void setReportCount(int reportCount) {
        this.reportCount = reportCount;
    }

    public void incrementReportCount() {
        this.reportCount++;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Timestamp resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    @Override
    public String toString() {
        return "Issue{" +
                "issueId='" + issueId + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                ", reportCount=" + reportCount +
                '}';
    }
}
