package com.environmental.api.dto;

import java.time.LocalDateTime;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String error;
    private boolean stale;
    private String timestamp;

    private ApiResponse() {
        this.timestamp = LocalDateTime.now().toString();
    }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.stale = false;
        return response;
    }

    public static <T> ApiResponse<T> stale(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.stale = true;
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = message;
        return response;
    }

    // Getters for JSON serialization
    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getError() { return error; }
    public boolean isStale() { return stale; }
    public String getTimestamp() { return timestamp; }
}
