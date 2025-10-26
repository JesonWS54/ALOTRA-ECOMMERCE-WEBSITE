package com.alotra.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Test Controller để kiểm tra kết nối database và application health
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private DataSource dataSource;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "AloTra Application is running!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Database connection test endpoint
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                response.put("status", "SUCCESS");
                response.put("message", "Database connection established successfully!");
                response.put("database", connection.getMetaData().getDatabaseProductName());
                response.put("version", connection.getMetaData().getDatabaseProductVersion());
                response.put("url", connection.getMetaData().getURL());
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "FAILED");
                response.put("message", "Database connection is closed");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to connect to database");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Application info endpoint
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getApplicationInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "AloTra Milk Tea Website");
        response.put("version", "1.0.0");
        response.put("description", "Spring Boot + Thymeleaf + Bootstrap + JPA + SQL Server");
        response.put("features", new String[]{
            "JWT Authentication",
            "WebSocket Notifications",
            "Cloudinary Image Upload",
            "E-commerce Features",
            "Admin Dashboard"
        });
        return ResponseEntity.ok(response);
    }
}