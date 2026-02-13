package com.civicpulse.service;

import com.civicpulse.dao.UserDAO;
import com.civicpulse.model.User;
import com.civicpulse.util.JWTUtil;
import com.civicpulse.util.PasswordUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Register a new citizen
     */
    public Map<String, Object> register(String name, String email, String phone, String password)
            throws SQLException {
        Map<String, Object> result = new HashMap<>();

        // Validate input
        if (name == null || name.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Name is required");
            return result;
        }

        if ((email == null || email.trim().isEmpty()) &&
                (phone == null || phone.trim().isEmpty())) {
            result.put("success", false);
            result.put("message", "Email or phone is required");
            return result;
        }

        if (!PasswordUtil.isValidPassword(password)) {
            result.put("success", false);
            result.put("message", "Password must be at least 6 characters");
            return result;
        }

        // Check if email/phone already exists
        if (email != null && !email.trim().isEmpty() && userDAO.emailExists(email)) {
            result.put("success", false);
            result.put("message", "Email already registered");
            return result;
        }

        if (phone != null && !phone.trim().isEmpty() && userDAO.phoneExists(phone)) {
            result.put("success", false);
            result.put("message", "Phone number already registered");
            return result;
        }

        // Create user
        String passwordHash = PasswordUtil.hashPassword(password);
        User user = new User(name, email, phone, passwordHash, "CITIZEN");

        user = userDAO.create(user);

        result.put("success", true);
        result.put("message", "Registration successful");
        result.put("userId", user.getUserId());

        return result;
    }

    /**
     * Login user (email/phone + password)
     */
    public Map<String, Object> login(String identifier, String password) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        // Find user by email or phone
        User user = userDAO.findByEmailOrPhone(identifier);

        if (user == null) {
            result.put("success", false);
            result.put("message", "Invalid credentials");
            return result;
        }

        // Verify password
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            result.put("success", false);
            result.put("message", "Invalid credentials");
            return result;
        }

        // Generate JWT token
        String token = JWTUtil.generateToken(user.getUserId(), user.getRole(), user.getName());

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getUserId());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getPhone());
        userData.put("role", user.getRole());
        userData.put("deptId", user.getDeptId());

        result.put("success", true);
        result.put("message", "Login successful");
        result.put("token", token);
        result.put("user", userData);

        return result;
    }

    /**
     * Validate JWT token and get user
     */
    public User validateToken(String token) throws SQLException {
        Integer userId = JWTUtil.getUserIdFromToken(token);

        if (userId == null) {
            return null;
        }

        return userDAO.findById(userId);
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) throws SQLException {
        return userDAO.findById(userId);
    }
}
