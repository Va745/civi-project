package com.civicpulse.dao;

import com.civicpulse.model.User;
import com.civicpulse.util.DatabaseUtil;

import java.sql.*;

public class UserDAO {

    public User create(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, phone, password_hash, role, dept_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getPasswordHash());
            stmt.setString(5, user.getRole());
            if (user.getDeptId() != null) {
                stmt.setInt(6, user.getDeptId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getInt(1));
                    }
                }
            }

            return user;
        }
    }

    public User findById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ? AND is_active = TRUE";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }

        return null;
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? AND is_active = TRUE";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }

        return null;
    }

    public User findByPhone(String phone) throws SQLException {
        String sql = "SELECT * FROM users WHERE phone = ? AND is_active = TRUE";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phone);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }

        return null;
    }

    public User findByEmailOrPhone(String identifier) throws SQLException {
        String sql = "SELECT * FROM users WHERE (email = ? OR phone = ?) AND is_active = TRUE";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, identifier);
            stmt.setString(2, identifier);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }

        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    public boolean phoneExists(String phone) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE phone = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phone);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));

        int deptId = rs.getInt("dept_id");
        if (!rs.wasNull()) {
            user.setDeptId(deptId);
        }

        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setActive(rs.getBoolean("is_active"));

        return user;
    }
}
