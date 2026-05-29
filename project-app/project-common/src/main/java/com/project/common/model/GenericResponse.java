package com.project.common.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GenericResponse<T> {

    private LocalDateTime timestamp;
    private boolean isSuccess;
    private String message;
    private T data;

    // Başarılı işlemler için kullanılacak standart dönüş
    public static <T> GenericResponse<T> success(T data) {
        return GenericResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .isSuccess(true)
                .data(data)
                .build();
    }

    // Hatalı işlemler için kullanılacak standart dönüş
    public static <T> GenericResponse<T> error(String message) {
        return GenericResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .isSuccess(false)
                .message(message)
                .build();
    }
}