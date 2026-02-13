package com.civicpulse.model;

import java.sql.Timestamp;

public class Department {
    private int deptId;
    private String deptName;
    private String deptType; // ROAD, WATER, SANITATION, ELECTRICITY
    private String contactEmail;
    private String contactPhone;
    private Timestamp createdAt;
    private boolean isActive;

    // Constructors
    public Department() {}

    public Department(String deptName, String deptType, String contactEmail, String contactPhone) {
        this.deptName = deptName;
        this.deptType = deptType;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.isActive = true;
    }

    // Getters and Setters
    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDeptType() {
        return deptType;
    }

    public void setDeptType(String deptType) {
        this.deptType = deptType;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Department{" +
                "deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", deptType='" + deptType + '\'' +
                '}';
    }
}
