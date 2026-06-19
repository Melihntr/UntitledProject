package com.project.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Tum API endpoint'lerinin disari dondugu standart JSON sablonu (Util/Wrapper).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // Basarili islemler icin hizli Util metotlari
    public static <T> GenericResponse<T> success(T data) {
        return GenericResponse.<T>builder()
                .success(true)
                .message("Operation completed successfully")
                .data(data)
                .build();
    }

    public static <T> GenericResponse<T> success(T data, String message) {
        return GenericResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> GenericResponse<List<T>> successList(List<T> data) {
        return GenericResponse.<List<T>>builder()
                .success(true)
                .message("Operation completed successfully")
                .data(data == null ? List.of() : data)
                .build();
    }

    // Hatali islemler icin hizli Util metodu
    public static <T> GenericResponse<T> error(String message) {
        return GenericResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}
