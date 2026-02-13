package com.civicpulse.servlet;

import com.civicpulse.service.AuthService;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/auth/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Parse JSON request
            BufferedReader reader = request.getReader();
            Map<String, String> requestData = gson.fromJson(reader, Map.class);

            String name = requestData.get("name");
            String email = requestData.get("email");
            String phone = requestData.get("phone");
            String password = requestData.get("password");

            // Register user
            Map<String, Object> result = authService.register(name, email, phone, password);

            if ((Boolean) result.get("success")) {
                response.setStatus(HttpServletResponse.SC_CREATED);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

            response.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> error = Map.of(
                    "success", false,
                    "message", "Registration failed: " + e.getMessage());
            response.getWriter().write(gson.toJson(error));
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
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}
