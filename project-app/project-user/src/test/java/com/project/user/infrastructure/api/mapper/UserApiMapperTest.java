package com.project.user.infrastructure.api.mapper;

import com.project.user.infrastructure.api.dto.CreateUserRequest;
import com.project.user.domain.model.UserModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserApiMapperTest {

    private final UserApiMapper mapper = new UserApiMapperImpl();

    @Test
    void mapsRequestAndResponses() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("password123");
        UserModel model = UserModel.builder()
                .id("u1")
                .username("alice")
                .email("alice@example.com")
                .build();

        assertThat(mapper.toInput(request).getRawPassword()).isEqualTo("password123");
        assertThat(mapper.toResponse(model).getStatusMessage()).isEqualTo("User account successfully created.");
        assertThat(mapper.toBasicResponse(model).getUsername()).isEqualTo("alice");
        assertThat(mapper.toBasicResponseList(List.of(model))).hasSize(1);
    }

    @Test
    void nullInputsReturnNull() {
        assertThat(mapper.toInput(null)).isNull();
        assertThat(mapper.toResponse(null)).isNull();
        assertThat(mapper.toBasicResponse(null)).isNull();
        assertThat(mapper.toBasicResponseList(null)).isNull();
    }
}
