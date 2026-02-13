package com.civicpulse.servlet;

import com.civicpulse.service.IssueService;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/issues/track/*")
public class TrackIssueServlet extends HttpServlet {

    private final IssueService issueService = new IssueService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Extract issue ID from path
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(Map.of(
                        "success", false,
                        "message", "Issue ID is required")));
                return;
            }

            String issueId = pathInfo.substring(1); // Remove leading slash

            // Get issue details
            Map<String, Object> issueDetails = issueService.getIssueDetails(issueId);

            if (issueDetails == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(Map.of(
                        "success", false,
                        "message", "Issue not found")));
                return;
            }

            issueDetails.put("success", true);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(issueDetails));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of(
                    "success", false,
                    "message", "Failed to fetch issue: " + e.getMessage())));
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}
