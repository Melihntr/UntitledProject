package com.project.common.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * A standardized wrapper class for all API responses across the enterprise application.
 * This generic structure ensures a consistent contract for external clients, 
 * making it easier for front-end applications or other microservices to parse responses and handle errors.
 *
 * @param <T> The type of the data payload included in the response.
 */
@Getter
@Builder
public class GenericResponse<T> {

    /**
     * The exact date and time when the response was generated.
     */
    private LocalDateTime timestamp;

    /**
     * A boolean flag indicating whether the API request was processed successfully.
     */
    private boolean isSuccess;

    /**
     * An optional message, typically used to provide human-readable error descriptions.
     */
    private String message;

    /**
     * The actual payload or business data returned by the API.
     */
    private T data;

    /**
     * Factory method for creating a standardized success response.
     *
     * @param data The payload to be returned to the client.
     * @param <T>  The type of the data payload.
     * @return A GenericResponse object with isSuccess set to true and the provided data.
     */
    public static <T> GenericResponse<T> success(T data) {
        return GenericResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .isSuccess(true)
                .data(data)
                .build();
    }

    /**
     * Factory method for creating a standardized error response.
     *
     * @param message A descriptive error message explaining what went wrong.
     * @param <T>     The type of the data payload (usually null in case of an error).
     * @return A GenericResponse object with isSuccess set to false and the provided error message.
     */
    public static <T> GenericResponse<T> error(String message) {
        return GenericResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .isSuccess(false)
                .message(message)
                .build();
    }
}