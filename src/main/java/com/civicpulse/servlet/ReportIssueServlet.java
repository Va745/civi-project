package com.civicpulse.servlet;

import com.civicpulse.model.Issue;
import com.civicpulse.service.IssueService;
import com.civicpulse.util.JWTUtil;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@WebServlet("/api/issues/report")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class ReportIssueServlet extends HttpServlet {

    private final IssueService issueService = new IssueService();
    private final Gson gson = new Gson();
    private static final String UPLOAD_DIR = "/tmp/civicpulse/uploads";

    @Override
    public void init() throws ServletException {
        // Create upload directory if it doesn't exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Validate JWT token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(Map.of(
                        "success", false,
                        "message", "Unauthorized")));
                return;
            }

            String token = authHeader.substring(7);
            Integer userId = JWTUtil.getUserIdFromToken(token);

            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(Map.of(
                        "success", false,
                        "message", "Invalid token")));
                return;
            }

            // Parse form data
            String category = request.getParameter("category");
            double latitude = Double.parseDouble(request.getParameter("latitude"));
            double longitude = Double.parseDouble(request.getParameter("longitude"));
            String address = request.getParameter("address");
            String description = request.getParameter("description");

            // Handle image upload
            String imageUrl = null;
            Part filePart = request.getPart("image");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = System.currentTimeMillis() + "_" + getFileName(filePart);
                String filePath = UPLOAD_DIR + File.separator + fileName;
                filePart.write(filePath);
                imageUrl = "/uploads/" + fileName;
            }

            // Create issue object
            Issue issue = new Issue(category, latitude, longitude, address, description);
            issue.setImageUrl(imageUrl);

            // Report issue (with duplicate detection)
            Map<String, Object> result = issueService.reportIssue(issue, userId);
            result.put("success", true);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(Map.of(
                    "success", false,
                    "message", "Failed to report issue: " + e.getMessage())));
        }
    }

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String content : contentDisposition.split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "unknown";
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
