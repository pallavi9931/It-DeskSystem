package com.itdesk.controller;

import com.itdesk.dto.response.ApiResponse;
import com.itdesk.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("health")
public class HealthController {
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String,Object>>> checkHealth()
    {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "IT Service Desk & ITAM Platform");
        status.put("version", "1.0.0");
        status.put("environment", "development");
        return ResponseEntity.ok(ApiResponse.success(status, "IT Desk Backend Service is healthy and running!"));
    }
    @GetMapping("/test-404")
    public ResponseEntity<ApiResponse<Void>> testNotFound() {
        throw new ResourceNotFoundException("Ticket", "id", 9999);
    }
    public record HealthTestDto(
            @NotBlank(message = "Username cannot be empty") String username,
            @Email(message = "Valid email is required") String email
    ) {}

    @PostMapping("/test-validation")
    public ResponseEntity<ApiResponse<String>> testValidation(@Valid @RequestBody HealthTestDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Validated successfully for user: " + dto.username()));
    }

    }


