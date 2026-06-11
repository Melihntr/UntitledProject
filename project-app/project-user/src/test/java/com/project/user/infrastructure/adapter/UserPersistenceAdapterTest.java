package com.project.user.infrastructure.adapter;

import com.project.user.domain.model.UserModel;
import com.project.user.infrastructure.entity.UserEntity;
import com.project.user.infrastructure.mapper.UserInfrastructureMapper;
import com.project.user.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserInfrastructureMapper userInfrastructureMapper;

    @InjectMocks
    private UserPersistenceAdapter adapter;

    @Test
    void save_mapsEntitySavesAndReturnsModel() {
        UserModel input = UserModel.builder().id("u1").username("alice").build();
        UserEntity entity = new UserEntity();
        UserEntity savedEntity = new UserEntity();
        UserModel savedModel = UserModel.builder().id("u1").username("alice").build();

        when(userInfrastructureMapper.toEntity(input)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(savedEntity);
        when(userInfrastructureMapper.toModel(savedEntity)).thenReturn(savedModel);

        UserModel result = adapter.save(input);

        assertThat(result).isSameAs(savedModel);
        verify(userInfrastructureMapper).toEntity(input);
        verify(userRepository).save(entity);
        verify(userInfrastructureMapper).toModel(savedEntity);
    }

    @Test
    void getAllUsers_mapsAllEntities() {
        UserEntity entity = new UserEntity();
        UserModel model = UserModel.builder().id("u1").username("alice").build();
        when(userRepository.findAll()).thenReturn(List.of(entity));
        when(userInfrastructureMapper.toModel(entity)).thenReturn(model);

        List<UserModel> result = adapter.getAllUsers();

        assertThat(result).containsExactly(model);
        verify(userRepository).findAll();
        verify(userInfrastructureMapper).toModel(entity);
    }

    @Test
    void deleteUserById_whenUserExists_deletesAndReturnsTrue() {
        UserEntity entity = new UserEntity();
        when(userRepository.findById("u1")).thenReturn(Optional.of(entity));

        boolean result = adapter.deleteUserById("u1");

        assertThat(result).isTrue();
        verify(userRepository).findById("u1");
        verify(userRepository).delete(entity);
        verifyNoInteractions(userInfrastructureMapper);
    }

    @Test
    void deleteUserById_whenUserDoesNotExist_returnsFalse() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        boolean result = adapter.deleteUserById("missing");

        assertThat(result).isFalse();
        verify(userRepository).findById("missing");
        verifyNoInteractions(userInfrastructureMapper);
    }
}
