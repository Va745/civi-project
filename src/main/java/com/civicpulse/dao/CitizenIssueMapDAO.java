package com.civicpulse.dao;

import com.civicpulse.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CitizenIssueMapDAO {

    public void create(int citizenId, String issueId) throws SQLException {
        String sql = "INSERT INTO citizen_issue_map (citizen_id, issue_id) VALUES (?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, citizenId);
            stmt.setString(2, issueId);

            stmt.executeUpdate();
        }
    }

    public List<String> findIssueIdsByCitizen(int citizenId) throws SQLException {
        String sql = "SELECT issue_id FROM citizen_issue_map WHERE citizen_id = ? ORDER BY reported_at DESC";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, citizenId);

            List<String> issueIds = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    issueIds.add(rs.getString("issue_id"));
                }
            }

            return issueIds;
        }
    }

    public boolean isCitizenMapped(int citizenId, String issueId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM citizen_issue_map WHERE citizen_id = ? AND issue_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, citizenId);
            stmt.setString(2, issueId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    public int getReporterCount(String issueId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM citizen_issue_map WHERE issue_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, issueId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }
}
