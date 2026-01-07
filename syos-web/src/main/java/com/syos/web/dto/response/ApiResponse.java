package com.syos.web.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Standard API response wrapper for all JSON responses.
 */
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String message;
    private String timestamp;
    private String error;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Creates a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        return response;
    }

    /**
     * Creates a successful response with data and message.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.message = message;
        return response;
    }

    /**
     * Creates a successful response with just a message.
     */
    public static <T> ApiResponse<T> successMessage(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        return response;
    }

    /**
     * Creates an error response.
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = message;
        response.message = message;
        return response;
    }

    /**
     * Creates an error response with additional details.
     */
    public static <T> ApiResponse<T> error(String message, String details) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = message;
        response.message = details;
        return response;
    }

    // Getters and Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
