package com.civicpulse.dao;

import com.civicpulse.model.Department;
import com.civicpulse.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    public Department findById(int deptId) throws SQLException {
        String sql = "SELECT * FROM departments WHERE dept_id = ? AND is_active = TRUE";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deptId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractDepartmentFromResultSet(rs);
                }
            }
        }

        return null;
    }

    public Department findByType(String deptType) throws SQLException {
        String sql = "SELECT * FROM departments WHERE dept_type = ? AND is_active = TRUE";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, deptType);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractDepartmentFromResultSet(rs);
                }
            }
        }

        return null;
    }

    public List<Department> findAll() throws SQLException {
        String sql = "SELECT * FROM departments WHERE is_active = TRUE ORDER BY dept_name";

        try (Connection conn = DatabaseUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            List<Department> departments = new ArrayList<>();
            while (rs.next()) {
                departments.add(extractDepartmentFromResultSet(rs));
            }

            return departments;
        }
    }

    private Department extractDepartmentFromResultSet(ResultSet rs) throws SQLException {
        Department dept = new Department();
        dept.setDeptId(rs.getInt("dept_id"));
        dept.setDeptName(rs.getString("dept_name"));
        dept.setDeptType(rs.getString("dept_type"));
        dept.setContactEmail(rs.getString("contact_email"));
        dept.setContactPhone(rs.getString("contact_phone"));
        dept.setCreatedAt(rs.getTimestamp("created_at"));
        dept.setActive(rs.getBoolean("is_active"));

        return dept;
    }
}
