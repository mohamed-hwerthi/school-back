package com.schoolSys.schooolSys.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper used across all endpoints.
 *
 * @param <T> the type of the response payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /** Whether the request was successful. */
    private boolean success;

    /** A human-readable message. */
    private String message;

    /** The response payload (nullable on errors). */
    private T data;

    /**
     * Creates a successful response.
     *
     * @param data the payload
     * @param <T>  the payload type
     * @return a success response
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .build();
    }

    /**
     * Creates an error response.
     *
     * @param message the error message
     * @param <T>     the payload type
     * @return an error response
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
