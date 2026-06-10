package com.project.user.infrastructure.bootstrap;

import com.project.user.domain.model.UserCreateInput;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.usecase.CreateUserHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDataSeederTest {

    @Mock
    private CreateUserHandler createUserHandler;

    @InjectMocks
    private UserDataSeeder seeder;

    @Test
    void runCreatesSenderAndReceiverSeedUsers() {
        when(createUserHandler.handle(org.mockito.ArgumentMatchers.any(UserCreateInput.class)))
                .thenAnswer(invocation -> {
                    UserCreateInput input = invocation.getArgument(0);
                    return UserModel.builder().id(input.getId()).username(input.getUsername()).build();
                });

        seeder.run();

        ArgumentCaptor<UserCreateInput> inputCaptor = ArgumentCaptor.forClass(UserCreateInput.class);
        verify(createUserHandler, times(2)).handle(inputCaptor.capture());
        List<UserCreateInput> inputs = inputCaptor.getAllValues();

        assertThat(inputs).extracting(UserCreateInput::getId)
                .containsExactly(
                        "11111111-1111-1111-1111-111111111111",
                        "22222222-2222-2222-2222-222222222222"
                );
        assertThat(inputs).extracting(UserCreateInput::getUsername)
                .containsExactly("test_sender", "test_receiver");
    }
}
