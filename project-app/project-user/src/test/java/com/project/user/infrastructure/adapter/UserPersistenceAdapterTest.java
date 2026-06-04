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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserPersistenceAdapter.
 */
@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserInfrastructureMapper userInfrastructureMapper;

    @InjectMocks
    private UserPersistenceAdapter adapter;

    @Test
    void save_mapsEntity_callsRepositoryAndReturnsSavedModel() {
        // Arrange
        UserModel inputModel = mock(UserModel.class);
        UserEntity mappedEntity = mock(UserEntity.class);
        UserEntity savedEntity = mock(UserEntity.class);
        UserModel savedModel = mock(UserModel.class);

        when(userInfrastructureMapper.toEntity(inputModel)).thenReturn(mappedEntity);
        when(userRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(userInfrastructureMapper.toModel(savedEntity)).thenReturn(savedModel);

        // Act
        UserModel result = adapter.save(inputModel);

        // Assert
        assertThat(result).isSameAs(savedModel);

        // Verify mapping, persistence and mapping back
        verify(userInfrastructureMapper).toEntity(inputModel);
        verify(userRepository).save(mappedEntity);
        verify(userInfrastructureMapper).toModel(savedEntity);
        verifyNoMoreInteractions(userInfrastructureMapper, userRepository);
    }

    @Test
    void getAllUsers_mapsEntitiesToModelsAndReturnsList() {
        // Arrange
        UserEntity entity1 = mock(UserEntity.class);
        UserEntity entity2 = mock(UserEntity.class);
        UserModel model1 = mock(UserModel.class);
        UserModel model2 = mock(UserModel.class);

        when(userRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(userInfrastructureMapper.toModel(entity1)).thenReturn(model1);
        when(userInfrastructureMapper.toModel(entity2)).thenReturn(model2);

        // Act
        List<UserModel> result = adapter.getAllUsers();

        // Assert: returned list preserves order and models are the mapped objects
        assertThat(result).containsExactly(model1, model2);

        // Verify interactions
        verify(userRepository).findAll();
        verify(userInfrastructureMapper).toModel(entity1);
        verify(userInfrastructureMapper).toModel(entity2);
        verifyNoMoreInteractions(userInfrastructureMapper, userRepository);
    }
}