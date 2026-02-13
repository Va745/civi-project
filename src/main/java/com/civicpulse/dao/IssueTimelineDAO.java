package com.civicpulse.dao;

import com.civicpulse.model.IssueTimeline;
import com.civicpulse.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IssueTimelineDAO {

    public void create(IssueTimeline timeline) throws SQLException {
        String sql = "INSERT INTO issue_timeline (issue_id, status, updated_by, remarks, proof_image_url) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, timeline.getIssueId());
            stmt.setString(2, timeline.getStatus());
            stmt.setInt(3, timeline.getUpdatedBy());
            stmt.setString(4, timeline.getRemarks());
            stmt.setString(5, timeline.getProofImageUrl());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        timeline.setTimelineId(rs.getInt(1));
                    }
                }
            }
        }
    }

    public List<IssueTimeline> findByIssueId(String issueId) throws SQLException {
        String sql = "SELECT t.*, u.name as updated_by_name, u.role as updated_by_role " +
                "FROM issue_timeline t " +
                "JOIN users u ON t.updated_by = u.user_id " +
                "WHERE t.issue_id = ? " +
                "ORDER BY t.created_at ASC";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, issueId);

            List<IssueTimeline> timelines = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    timelines.add(extractTimelineFromResultSet(rs));
                }
            }

            return timelines;
        }
    }

    private IssueTimeline extractTimelineFromResultSet(ResultSet rs) throws SQLException {
        IssueTimeline timeline = new IssueTimeline();
        timeline.setTimelineId(rs.getInt("timeline_id"));
        timeline.setIssueId(rs.getString("issue_id"));
        timeline.setStatus(rs.getString("status"));
        timeline.setUpdatedBy(rs.getInt("updated_by"));
        timeline.setRemarks(rs.getString("remarks"));
        timeline.setProofImageUrl(rs.getString("proof_image_url"));
        timeline.setCreatedAt(rs.getTimestamp("created_at"));
        timeline.setUpdatedByName(rs.getString("updated_by_name"));
        timeline.setUpdatedByRole(rs.getString("updated_by_role"));

        return timeline;
    }
}
