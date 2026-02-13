package com.civicpulse.service;

import com.civicpulse.dao.*;
import com.civicpulse.model.Issue;
import com.civicpulse.model.IssueTimeline;
import com.civicpulse.util.GeoUtil;
import com.civicpulse.util.IssueIdGenerator;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssueService {

    private final IssueDAO issueDAO;
    private final CitizenIssueMapDAO citizenIssueMapDAO;
    private final IssueTimelineDAO timelineDAO;
    private final DepartmentDAO departmentDAO;

    private static final double DUPLICATE_RADIUS_KM = 0.1; // 100 meters

    public IssueService() {
        this.issueDAO = new IssueDAO();
        this.citizenIssueMapDAO = new CitizenIssueMapDAO();
        this.timelineDAO = new IssueTimelineDAO();
        this.departmentDAO = new DepartmentDAO();
    }

    /**
     * Report a new issue with intelligent duplicate detection
     * Returns map with: issueId, merged (boolean), reportCount
     */
    public Map<String, Object> reportIssue(Issue newIssue, int citizenId) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        // Find potential duplicates
        double[] boundingBox = GeoUtil.getBoundingBox(
                newIssue.getLocationLat(),
                newIssue.getLocationLng(),
                DUPLICATE_RADIUS_KM);

        List<Issue> candidates = issueDAO.findNearbyIssues(
                newIssue.getCategory(),
                newIssue.getLocationLat(),
                newIssue.getLocationLng(),
                boundingBox[0], // minLat
                boundingBox[1], // maxLat
                boundingBox[2], // minLng
                boundingBox[3] // maxLng
        );

        // Check for exact duplicate
        Issue duplicate = findDuplicate(newIssue, candidates);

        if (duplicate != null) {
            // Merge with existing issue
            issueDAO.incrementReportCount(duplicate.getIssueId());
            citizenIssueMapDAO.create(citizenId, duplicate.getIssueId());

            // Refresh issue to get updated report count
            Issue updated = issueDAO.findById(duplicate.getIssueId());

            result.put("issueId", duplicate.getIssueId());
            result.put("merged", true);
            result.put("reportCount", updated.getReportCount());
            result.put("message", "Your issue has been merged with an existing report");

        } else {
            // Create new issue
            String issueId = IssueIdGenerator.generateIssueId(newIssue.getCategory());
            newIssue.setIssueId(issueId);
            newIssue.setStatus("REPORTED");
            newIssue.setReportCount(1);

            issueDAO.create(newIssue);
            citizenIssueMapDAO.create(citizenId, issueId);

            // Create timeline entry
            IssueTimeline timeline = new IssueTimeline();
            timeline.setIssueId(issueId);
            timeline.setStatus("REPORTED");
            timeline.setUpdatedBy(citizenId);
            timeline.setRemarks("Issue reported by citizen");
            timelineDAO.create(timeline);

            result.put("issueId", issueId);
            result.put("merged", false);
            result.put("reportCount", 1);
            result.put("message", "Issue reported successfully");
        }

        return result;
    }

    /**
     * Find duplicate issue based on category, location, and status
     */
    private Issue findDuplicate(Issue newIssue, List<Issue> candidates) {
        for (Issue candidate : candidates) {
            // Check if same category (already filtered in query)
            // Check if not resolved
            if ("RESOLVED".equals(candidate.getStatus())) {
                continue;
            }

            // Check geo-proximity
            boolean isNearby = GeoUtil.isWithinRadius(
                    candidate.getLocationLat(),
                    candidate.getLocationLng(),
                    newIssue.getLocationLat(),
                    newIssue.getLocationLng(),
                    DUPLICATE_RADIUS_KM);

            if (isNearby) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * Get issue details with timeline
     */
    public Map<String, Object> getIssueDetails(String issueId) throws SQLException {
        Issue issue = issueDAO.findById(issueId);

        if (issue == null) {
            return null;
        }

        List<IssueTimeline> timeline = timelineDAO.findByIssueId(issueId);

        Map<String, Object> result = new HashMap<>();
        result.put("issue", issue);
        result.put("timeline", timeline);

        // Get department name if assigned
        if (issue.getDeptId() != null) {
            var dept = departmentDAO.findById(issue.getDeptId());
            if (dept != null) {
                result.put("departmentName", dept.getDeptName());
            }
        }

        return result;
    }

    /**
     * Get citizen's reported issues
     */
    public List<Issue> getCitizenIssues(int citizenId) throws SQLException {
        List<String> issueIds = citizenIssueMapDAO.findIssueIdsByCitizen(citizenId);

        List<Issue> issues = new java.util.ArrayList<>();
        for (String issueId : issueIds) {
            Issue issue = issueDAO.findById(issueId);
            if (issue != null) {
                issues.add(issue);
            }
        }

        return issues;
    }

    /**
     * Update issue status (Admin/Department)
     */
    public void updateIssueStatus(String issueId, String newStatus, int updatedBy,
            String remarks, String proofImageUrl) throws SQLException {
        Issue issue = issueDAO.findById(issueId);

        if (issue == null) {
            throw new SQLException("Issue not found");
        }

        // Update issue status
        issue.setStatus(newStatus);

        if ("RESOLVED".equals(newStatus)) {
            issue.setResolvedAt(new Timestamp(System.currentTimeMillis()));
        }

        issueDAO.update(issue);

        // Create timeline entry
        IssueTimeline timeline = new IssueTimeline();
        timeline.setIssueId(issueId);
        timeline.setStatus(newStatus);
        timeline.setUpdatedBy(updatedBy);
        timeline.setRemarks(remarks);
        timeline.setProofImageUrl(proofImageUrl);
        timelineDAO.create(timeline);
    }

    /**
     * Assign issue to department (Admin only)
     */
    public void assignIssueToDepartment(String issueId, int deptId, int adminId) throws SQLException {
        Issue issue = issueDAO.findById(issueId);

        if (issue == null) {
            throw new SQLException("Issue not found");
        }

        issue.setDeptId(deptId);
        issue.setStatus("ASSIGNED");
        issueDAO.update(issue);

        // Create timeline entry
        var dept = departmentDAO.findById(deptId);
        String remarks = "Assigned to " + (dept != null ? dept.getDeptName() : "department");

        IssueTimeline timeline = new IssueTimeline();
        timeline.setIssueId(issueId);
        timeline.setStatus("ASSIGNED");
        timeline.setUpdatedBy(adminId);
        timeline.setRemarks(remarks);
        timelineDAO.create(timeline);
    }

    /**
     * Get all issues (Admin)
     */
    public List<Issue> getAllIssues() throws SQLException {
        return issueDAO.findAll();
    }

    /**
     * Get department issues
     */
    public List<Issue> getDepartmentIssues(int deptId) throws SQLException {
        return issueDAO.findByDepartment(deptId);
    }

    /**
     * Get analytics data (Admin)
     */
    public Map<String, Object> getAnalytics() throws SQLException {
        List<Issue> allIssues = issueDAO.findAll();

        Map<String, Object> analytics = new HashMap<>();

        int total = allIssues.size();
        int reported = 0, assigned = 0, inProgress = 0, resolved = 0;
        Map<String, Integer> byCategory = new HashMap<>();

        for (Issue issue : allIssues) {
            switch (issue.getStatus()) {
                case "REPORTED":
                    reported++;
                    break;
                case "ASSIGNED":
                    assigned++;
                    break;
                case "IN_PROGRESS":
                    inProgress++;
                    break;
                case "RESOLVED":
                    resolved++;
                    break;
            }

            byCategory.put(issue.getCategory(),
                    byCategory.getOrDefault(issue.getCategory(), 0) + 1);
        }

        analytics.put("totalIssues", total);
        analytics.put("reported", reported);
        analytics.put("assigned", assigned);
        analytics.put("inProgress", inProgress);
        analytics.put("resolved", resolved);
        analytics.put("pending", reported + assigned + inProgress);
        analytics.put("byCategory", byCategory);

        return analytics;
    }
}
