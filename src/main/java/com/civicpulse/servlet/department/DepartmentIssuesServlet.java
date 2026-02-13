package com.civicpulse.servlet.department;

import com.civicpulse.model.Issue;
import com.civicpulse.model.User;
import com.civicpulse.service.AuthService;
import com.civicpulse.service.IssueService;
import com.civicpulse.util.JWTUtil;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/department/issues")
public class DepartmentIssuesServlet extends HttpServlet {

    private final IssueService issueService = new IssueService();
    private final AuthService authService = new AuthService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Validate JWT token and role
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(Map.of(
                        "success", false,
                        "message", "Unauthorized")));
                return;
            }

            String token = authHeader.substring(7);
            String role = JWTUtil.getRoleFromToken(token);
            Integer userId = JWTUtil.getUserIdFromToken(token);

            if (!"DEPARTMENT".equals(role)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(gson.toJson(Map.of(
                        "success", false,
                        "message", "Department access required")));
                return;
            }

            // Get user's department
            User user = authService.getUserById(userId);
            if (user.getDeptId() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(Map.of(
                        "success", false,
                        "message", "User not assigned to any department")));
                return;
            }

            // Get department issues
            List<Issue> issues = issueService.getDepartmentIssues(user.getDeptId());

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(Map.of(
                    "success", true,
                    "issues", issues)));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of(
                    "success", false,
                    "message", "Failed to fetch issues: " + e.getMessage())));
        }
    }
}
