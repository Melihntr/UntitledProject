package com.project.user.api.mapper;

import com.project.user.api.dto.BasicUserResponse;
import com.project.user.api.dto.CreateUserRequest;
import com.project.user.api.dto.CreateUserResponse;
import com.project.user.domain.model.UserCreateInput;
import com.project.user.domain.model.UserModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserApiMapperTest {

    private final UserApiMapper mapper = Mappers.getMapper(UserApiMapper.class);

    @Test
    void toInput_mapsPasswordToRawPassword() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("alice");
        req.setEmail("alice@example.com");
        req.setPassword("secret123");

        UserCreateInput input = mapper.toInput(req);

        assertThat(input).isNotNull();
        assertThat(input.getUsername()).isEqualTo("alice");
        assertThat(input.getEmail()).isEqualTo("alice@example.com");
        assertThat(input.getRawPassword()).isEqualTo("secret123");
    }

    @Test
    void toResponse_mapsFieldsAndSetsStatusMessageConstant() {
        UserModel model = UserModel.builder()
                .id("1")
                .username("alice")
                .email("alice@example.com")
                .build();

        CreateUserResponse dto = mapper.toResponse(model);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("1");
        assertThat(dto.getUsername()).isEqualTo("alice");
        assertThat(dto.getEmail()).isEqualTo("alice@example.com");
        assertThat(dto.getStatusMessage()).isEqualTo("User account successfully created.");
    }

    @Test
    void toBasicResponseList_mapsListCorrectly() {
        UserModel m1 = UserModel.builder().id("1").username("alice").build();
        UserModel m2 = UserModel.builder().id("2").username("bob").build();

        List<BasicUserResponse> list = mapper.toBasicResponseList(List.of(m1, m2));

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getId()).isEqualTo("1");
        assertThat(list.get(0).getUsername()).isEqualTo("alice");
        assertThat(list.get(1).getId()).isEqualTo("2");
        assertThat(list.get(1).getUsername()).isEqualTo("bob");
    }
}
