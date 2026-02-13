package com.civicpulse.servlet.admin;

import com.civicpulse.model.Issue;
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

@WebServlet("/api/admin/issues")
public class AdminIssuesServlet extends HttpServlet {

    private final IssueService issueService = new IssueService();
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

            if (!"ADMIN".equals(role)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(gson.toJson(Map.of(
                        "success", false,
                        "message", "Admin access required")));
                return;
            }

            // Get all issues
            List<Issue> issues = issueService.getAllIssues();

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(Map.of(
                    "success", true,
                    "total", issues.size(),
                    "issues", issues)));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of(
                    "success", false,
                    "message", "Failed to fetch issues: " + e.getMessage())));
        }
    }
}
