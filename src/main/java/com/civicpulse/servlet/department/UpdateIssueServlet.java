package com.civicpulse.servlet.department;

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

@WebServlet("/api/department/issues/*/update")
public class UpdateIssueServlet extends HttpServlet {

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
            Integer userId = JWTUtil.getUserIdFromToken(token);

            if (!"DEPARTMENT".equals(role) && !"ADMIN".equals(role)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(gson.toJson(Map.of(
                        "success", false,
                        "message", "Department or Admin access required")));
                return;
            }

            // Extract issue ID from path
            String pathInfo = request.getPathInfo();
            String issueId = pathInfo.substring(1, pathInfo.lastIndexOf("/"));

            // Parse request body
            BufferedReader reader = request.getReader();
            Map<String, String> requestData = gson.fromJson(reader, Map.class);

            String status = requestData.get("status");
            String remarks = requestData.get("remarks");
            String proofImageUrl = requestData.get("proofImageUrl");

            // Update issue
            issueService.updateIssueStatus(issueId, status, userId, remarks, proofImageUrl);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(Map.of(
                    "success", true,
                    "message", "Issue updated successfully")));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of(
                    "success", false,
                    "message", "Failed to update issue: " + e.getMessage())));
        }
    }
}
