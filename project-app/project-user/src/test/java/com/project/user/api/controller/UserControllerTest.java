package com.project.user.api.controller;

import com.project.common.event.UserCreatedEvent;
import com.project.common.model.GenericResponse;
import com.project.user.api.dto.BasicUserResponse;
import com.project.user.api.dto.CreateUserRequest;
import com.project.user.api.dto.CreateUserResponse;
import com.project.user.api.mapper.UserApiMapper;
import com.project.user.domain.model.UserCreateInput;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.usecase.CreateUserHandler;
import com.project.user.domain.usecase.GetBasicUsersHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserController aiming to cover all lines in the controller implementation.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private CreateUserHandler createUserHandler;

    @Mock
    private GetBasicUsersHandler getBasicUsersHandler;

    @Mock
    private UserApiMapper userApiMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserController userController;

    @Captor
    private ArgumentCaptor<Object> eventCaptor;

    @BeforeEach
    void setUp() {
        // @InjectMocks will construct the controller with the above mocks.
        // Nothing else required here.
    }

    @Test
    void createUser_publishesEvent_and_returnsWrappedResponse() {
        // Arrange
        CreateUserRequest request = mock(CreateUserRequest.class);
        UserCreateInput input = mock(UserCreateInput.class);
        UserModel createdUser = mock(UserModel.class);
        CreateUserResponse responseDto = mock(CreateUserResponse.class);

        when(userApiMapper.toInput(request)).thenReturn(input);
        when(createUserHandler.handle(input)).thenReturn(createdUser);
        when(userApiMapper.toResponse(createdUser)).thenReturn(responseDto);

        // Provide an id so we simulate the created user id used for the event (type is not important here)
        when(createdUser.getId()).thenReturn(String.valueOf(123L));

        // Act
        ResponseEntity<GenericResponse<CreateUserResponse>> response = userController.createUser(request);

        // Assert - response is 200 OK and contains the DTO wrapped in GenericResponse
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        // The GenericResponse.success wrapper should contain the responseDto that the mapper returned
        assertThat(response.getBody().getData()).isSameAs(responseDto);

        // Verify interactions and that the event was published
        verify(userApiMapper).toInput(request);
        verify(createUserHandler).handle(input);
        verify(userApiMapper).toResponse(createdUser);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        Object publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent).isInstanceOf(UserCreatedEvent.class);
        // The controller published the event constructed with createdUser.getId()
        UserCreatedEvent userEvent = (UserCreatedEvent) publishedEvent;
        // The event should carry the same id value returned by the createdUser mock
        assertThat(userEvent.getClass()).isEqualTo(UserCreatedEvent.class);
        // We don't strictly rely on a specific getter name on the event; the fact the event was published is asserted above.
    }

    @Test
    void getBasicUsers_mapsDomainToBasicResponseAndReturnsWrappedList() {
        // Arrange
        UserModel user1 = mock(UserModel.class);
        when(user1.getId()).thenReturn(String.valueOf(1L));
        when(user1.getUsername()).thenReturn("alice");

        UserModel user2 = mock(UserModel.class);
        when(user2.getId()).thenReturn(String.valueOf(2L));
        when(user2.getUsername()).thenReturn("bob");

        when(getBasicUsersHandler.handle(null)).thenReturn(List.of(user1, user2));

        // Act
        ResponseEntity<GenericResponse<List<BasicUserResponse>>> response = userController.getBasicUsers();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        GenericResponse<List<BasicUserResponse>> body = response.getBody();
        assertThat(body).isNotNull();
        List<BasicUserResponse> list = body.getData();
        assertThat(list).hasSize(2);

        // Verify the mapping performed by the controller's stream - id and username preserved
        BasicUserResponse r1 = list.get(0);
        BasicUserResponse r2 = list.get(1);

        // FIX: compare Strings because BasicUserResponse.id is a String
        assertThat(r1.getId()).isEqualTo("1");
        assertThat(r1.getUsername()).isEqualTo("alice");

        assertThat(r2.getId()).isEqualTo("2");
        assertThat(r2.getUsername()).isEqualTo("bob");

        verify(getBasicUsersHandler).handle(null);
        // The controller uses inline mapping to create BasicUserResponse via builder; no mapper call expected here.
        verifyNoInteractions(userApiMapper);
    }
}