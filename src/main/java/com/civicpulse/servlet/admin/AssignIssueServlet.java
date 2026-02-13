package com.civicpulse.servlet.admin;

import com.civicpulse.service.IssueService;
import com.civicpulse.util.JWTUtil;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/admin/issues/*/assign")
public class AssignIssueServlet extends HttpServlet {

    private final IssueService issueService = new IssueService();
    private final Gson gson = new Gson();

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
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
            Integer adminId = JWTUtil.getUserIdFromToken(token);

            if (!"ADMIN".equals(role)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(gson.toJson(Map.of(
                        "success", false,
                        "message", "Admin access required")));
                return;
            }

            // Extract issue ID from path
            String pathInfo = request.getPathInfo();
            String issueId = pathInfo.substring(1, pathInfo.lastIndexOf("/"));

            // Parse request body
            BufferedReader reader = request.getReader();
            Map<String, Object> requestData = gson.fromJson(reader, Map.class);
            int deptId = ((Double) requestData.get("deptId")).intValue();

            // Assign issue
            issueService.assignIssueToDepartment(issueId, deptId, adminId);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(Map.of(
                    "success", true,
                    "message", "Issue assigned successfully")));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of(
                    "success", false,
                    "message", "Failed to assign issue: " + e.getMessage())));
        }
    }
}
