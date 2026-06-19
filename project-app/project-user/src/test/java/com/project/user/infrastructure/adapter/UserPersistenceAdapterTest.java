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
        when(userRepository.findAllByIsUserDeletedFalse()).thenReturn(List.of(entity));
        when(userInfrastructureMapper.toModel(entity)).thenReturn(model);

        List<UserModel> result = adapter.getAllUsers();

        assertThat(result).containsExactly(model);
        verify(userRepository).findAllByIsUserDeletedFalse();
        verify(userInfrastructureMapper).toModel(entity);
    }

    @Test
    void deleteUserById_whenUserExists_softDeletesAndReturnsTrue() {
        UserEntity entity = new UserEntity();
        when(userRepository.findByIdAndIsUserDeletedFalse("u1")).thenReturn(Optional.of(entity));
        when(userRepository.save(entity)).thenReturn(entity);

        boolean result = adapter.deleteUserById("u1");

        assertThat(result).isTrue();
        assertThat(entity.isUserDeleted()).isTrue();
        verify(userRepository).findByIdAndIsUserDeletedFalse("u1");
        verify(userRepository).save(entity);
        verifyNoInteractions(userInfrastructureMapper);
    }

    @Test
    void deleteUserById_whenUserDoesNotExist_returnsFalse() {
        when(userRepository.findByIdAndIsUserDeletedFalse("missing")).thenReturn(Optional.empty());

        boolean result = adapter.deleteUserById("missing");

        assertThat(result).isFalse();
        verify(userRepository).findByIdAndIsUserDeletedFalse("missing");
        verifyNoInteractions(userInfrastructureMapper);
    }
}
