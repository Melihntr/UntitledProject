package com.project.user.api.controller;

import com.project.common.model.GenericResponse;
import com.project.user.api.dto.BasicUserResponse;
import com.project.user.api.dto.CreateUserRequest;
import com.project.user.api.dto.CreateUserResponse;
import com.project.user.api.mapper.UserApiMapper;
import com.project.user.domain.model.UserCreateInput;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.usecase.CreateUserHandler;
import com.project.user.domain.usecase.DeleteUserHandler;
import com.project.user.domain.usecase.GetBasicUsersHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private CreateUserHandler createUserHandler;

    @Mock
    private GetBasicUsersHandler getBasicUsersHandler;

    @Mock
    private DeleteUserHandler deleteUserHandler;

    @Mock
    private UserApiMapper userApiMapper;

    @InjectMocks
    private UserController controller;

    @Test
    void createUser_mapsRequestDelegatesAndReturnsResponse() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("password123");

        UserCreateInput input = UserCreateInput.builder().username("alice").build();
        UserModel model = UserModel.builder().id("u1").username("alice").email("alice@example.com").build();
        CreateUserResponse response = CreateUserResponse.builder().id("u1").username("alice").build();

        when(userApiMapper.toInput(request)).thenReturn(input);
        when(createUserHandler.handle(input)).thenReturn(model);
        when(userApiMapper.toResponse(model)).thenReturn(response);

        ResponseEntity<GenericResponse<CreateUserResponse>> result = controller.createUser(request);

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isSameAs(response);
        verify(userApiMapper).toInput(request);
        verify(createUserHandler).handle(input);
        verify(userApiMapper).toResponse(model);
    }

    @Test
    void getBasicUsers_mapsUsersToBasicResponses() {
        List<UserModel> users = List.of(UserModel.builder().id("u1").username("alice").build());
        List<BasicUserResponse> responses = List.of(BasicUserResponse.builder().id("u1").username("alice").build());

        when(getBasicUsersHandler.handle(null)).thenReturn(users);
        when(userApiMapper.toBasicResponseList(users)).thenReturn(responses);

        ResponseEntity<GenericResponse<List<BasicUserResponse>>> result = controller.getBasicUsers();

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isSameAs(responses);
        verify(getBasicUsersHandler).handle(null);
        verify(userApiMapper).toBasicResponseList(users);
    }

    @Test
    void deleteUser_delegatesAndReturnsGenericSuccessResponse() {
        ResponseEntity<GenericResponse<Void>> result = controller.deleteUser("u1");

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(result.getBody().getMessage()).isEqualTo("User deleted successfully.");
        assertThat(result.getBody().getData()).isNull();
        verify(deleteUserHandler).handle("u1");
    }
}
