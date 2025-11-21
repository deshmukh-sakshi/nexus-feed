package com.nexus.feed.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Object errors;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, null);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null);
    }
    
    public static <T> ApiResponse<T> error(String message, Object errors) {
        return new ApiResponse<>(false, message, null, errors);
    }
}
