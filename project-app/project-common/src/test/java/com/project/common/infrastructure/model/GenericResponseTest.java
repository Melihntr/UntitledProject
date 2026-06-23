package com.project.common.infrastructure.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GenericResponseTest {

    @Test
    void success_withDefaultMessage_wrapsData() {
        GenericResponse<String> response = GenericResponse.success("payload");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Operation completed successfully");
        assertThat(response.getData()).isEqualTo("payload");
    }

    @Test
    void success_withCustomMessage_wrapsData() {
        GenericResponse<String> response = GenericResponse.success("payload", "done");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("done");
        assertThat(response.getData()).isEqualTo("payload");
    }

    @Test
    void error_setsFailureAndClearsData() {
        GenericResponse<String> response = GenericResponse.error("failed");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("failed");
        assertThat(response.getData()).isNull();
    }

    @Test
    void builderAndConstructorsPopulateFields() {
        GenericResponse<String> built = GenericResponse.<String>builder()
                .success(true)
                .message("ok")
                .data("data")
                .build();
        GenericResponse<String> allArgs = new GenericResponse<>(false, "no", null);
        GenericResponse<String> noArgs = new GenericResponse<>();

        assertThat(built.getData()).isEqualTo("data");
        assertThat(GenericResponse.builder().toString()).contains("GenericResponseBuilder");
        assertThat(allArgs.isSuccess()).isFalse();
        assertThat(allArgs.getMessage()).isEqualTo("no");
        assertThat(noArgs.getData()).isNull();
    }

    @Test
    void successListConvertsNullToEmptyListAndKeepsExistingList() {
        GenericResponse<List<String>> empty = GenericResponse.successList(null);
        List<String> values = List.of("value");
        GenericResponse<List<String>> populated = GenericResponse.successList(values);

        assertThat(empty.getData()).isEmpty();
        assertThat(populated.getData()).isSameAs(values);
    }
}
