package com.civicpulse.dao;

import com.civicpulse.model.Issue;
import com.civicpulse.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IssueDAO {

    public Issue create(Issue issue) throws SQLException {
        String sql = "INSERT INTO issues (issue_id, category, location_lat, location_lng, address, " +
                "description, status, report_count, dept_id, image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, issue.getIssueId());
            stmt.setString(2, issue.getCategory());
            stmt.setDouble(3, issue.getLocationLat());
            stmt.setDouble(4, issue.getLocationLng());
            stmt.setString(5, issue.getAddress());
            stmt.setString(6, issue.getDescription());
            stmt.setString(7, issue.getStatus());
            stmt.setInt(8, issue.getReportCount());

            if (issue.getDeptId() != null) {
                stmt.setInt(9, issue.getDeptId());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }

            stmt.setString(10, issue.getImageUrl());

            stmt.executeUpdate();

            return issue;
        }
    }

    public Issue findById(String issueId) throws SQLException {
        String sql = "SELECT * FROM issues WHERE issue_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, issueId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractIssueFromResultSet(rs);
                }
            }
        }

        return null;
    }

    public List<Issue> findByCategoryAndStatus(String category, List<String> statuses) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM issues WHERE category = ? AND status IN (");
        for (int i = 0; i < statuses.size(); i++) {
            sql.append("?");
            if (i < statuses.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setString(1, category);
            for (int i = 0; i < statuses.size(); i++) {
                stmt.setString(i + 2, statuses.get(i));
            }

            List<Issue> issues = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    issues.add(extractIssueFromResultSet(rs));
                }
            }

            return issues;
        }
    }

    public List<Issue> findNearbyIssues(String category, double lat, double lng,
            double minLat, double maxLat,
            double minLng, double maxLng) throws SQLException {
        String sql = "SELECT * FROM issues WHERE category = ? " +
                "AND status IN ('REPORTED', 'ASSIGNED', 'IN_PROGRESS') " +
                "AND location_lat BETWEEN ? AND ? " +
                "AND location_lng BETWEEN ? AND ? " +
                "LIMIT 50";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);
            stmt.setDouble(2, minLat);
            stmt.setDouble(3, maxLat);
            stmt.setDouble(4, minLng);
            stmt.setDouble(5, maxLng);

            List<Issue> issues = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    issues.add(extractIssueFromResultSet(rs));
                }
            }

            return issues;
        }
    }

    public void update(Issue issue) throws SQLException {
        String sql = "UPDATE issues SET status = ?, report_count = ?, dept_id = ?, " +
                "image_url = ?, resolved_at = ? WHERE issue_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, issue.getStatus());
            stmt.setInt(2, issue.getReportCount());

            if (issue.getDeptId() != null) {
                stmt.setInt(3, issue.getDeptId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setString(4, issue.getImageUrl());

            if (issue.getResolvedAt() != null) {
                stmt.setTimestamp(5, issue.getResolvedAt());
            } else {
                stmt.setNull(5, Types.TIMESTAMP);
            }

            stmt.setString(6, issue.getIssueId());

            stmt.executeUpdate();
        }
    }

    public void incrementReportCount(String issueId) throws SQLException {
        String sql = "UPDATE issues SET report_count = report_count + 1 WHERE issue_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, issueId);
            stmt.executeUpdate();
        }
    }

    public List<Issue> findAll() throws SQLException {
        String sql = "SELECT * FROM issues ORDER BY created_at DESC";

        try (Connection conn = DatabaseUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            List<Issue> issues = new ArrayList<>();
            while (rs.next()) {
                issues.add(extractIssueFromResultSet(rs));
            }

            return issues;
        }
    }

    public List<Issue> findByDepartment(int deptId) throws SQLException {
        String sql = "SELECT * FROM issues WHERE dept_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deptId);

            List<Issue> issues = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    issues.add(extractIssueFromResultSet(rs));
                }
            }

            return issues;
        }
    }

    private Issue extractIssueFromResultSet(ResultSet rs) throws SQLException {
        Issue issue = new Issue();
        issue.setIssueId(rs.getString("issue_id"));
        issue.setCategory(rs.getString("category"));
        issue.setLocationLat(rs.getDouble("location_lat"));
        issue.setLocationLng(rs.getDouble("location_lng"));
        issue.setAddress(rs.getString("address"));
        issue.setDescription(rs.getString("description"));
        issue.setStatus(rs.getString("status"));
        issue.setReportCount(rs.getInt("report_count"));

        int deptId = rs.getInt("dept_id");
        if (!rs.wasNull()) {
            issue.setDeptId(deptId);
        }

        issue.setImageUrl(rs.getString("image_url"));
        issue.setCreatedAt(rs.getTimestamp("created_at"));
        issue.setUpdatedAt(rs.getTimestamp("updated_at"));
        issue.setResolvedAt(rs.getTimestamp("resolved_at"));

        return issue;
    }
}
