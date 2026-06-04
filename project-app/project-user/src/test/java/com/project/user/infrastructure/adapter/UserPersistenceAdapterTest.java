package com.project.user.infrastructure.adapter;

import com.project.user.domain.model.UserModel;
import com.project.user.infrastructure.entity.UserEntity;
import com.project.user.infrastructure.mapper.UserInfrastructureMapper;
import com.project.user.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserPersistenceAdapter.
 * Uses Mockito for mocks and constructor injection; no Spring context needed.
 */
class UserPersistenceAdapterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserInfrastructureMapper userInfrastructureMapper;

    // We'll construct the adapter manually to emphasize constructor injection
    private UserPersistenceAdapter adapter;

    @Captor
    private ArgumentCaptor<UserEntity> entityCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new UserPersistenceAdapter(userRepository, userInfrastructureMapper);
        entityCaptor = ArgumentCaptor.forClass(UserEntity.class);
    }

    @Test
    void save_shouldMapModelToEntity_callRepository_andReturnMappedModel() {
        // Arrange
        UserModel inputModel = new UserModel();
        inputModel.setId("input-id");
        inputModel.setUsername("Alice");

        UserEntity mappedEntity = new UserEntity();
        mappedEntity.setId("input-id");
        mappedEntity.setUsername("Alice");

        UserEntity savedEntity = new UserEntity();
        savedEntity.setId("generated-id");
        savedEntity.setUsername("Alice");
        savedEntity.setCreatedAt(System.currentTimeMillis()); // e.g., DB-generated fields

        UserModel returnedModel = new UserModel();
        returnedModel.setId("generated-id");
        returnedModel.setUsername("Alice");

        when(userInfrastructureMapper.toEntity(inputModel)).thenReturn(mappedEntity);
        when(userRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(userInfrastructureMapper.toModel(savedEntity)).thenReturn(returnedModel);

        // Act
        UserModel result = adapter.save(inputModel);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("generated-id");
        assertThat(result.getUsername()).isEqualTo("Alice");

        // verify interactions
        verify(userInfrastructureMapper).toEntity(inputModel);
        verify(userRepository).save(mappedEntity);
        verify(userInfrastructureMapper).toModel(savedEntity);

        verifyNoMoreInteractions(userInfrastructureMapper, userRepository);
    }

    @Test
    void getAllUsers_shouldReturnMappedModels() {
        // Arrange
        UserEntity e1 = new UserEntity();
        e1.setId("u1");
        e1.setUsername("User One");

        UserEntity e2 = new UserEntity();
        e2.setId("u2");
        e2.setUsername("User Two");

        UserModel m1 = new UserModel();
        m1.setId("u1");
        m1.setUsername("User One");

        UserModel m2 = new UserModel();
        m2.setId("u2");
        m2.setUsername("User Two");

        List<UserEntity> entities = Arrays.asList(e1, e2);

        when(userRepository.findAll()).thenReturn(entities);
        when(userInfrastructureMapper.toModel(e1)).thenReturn(m1);
        when(userInfrastructureMapper.toModel(e2)).thenReturn(m2);

        // Act
        List<UserModel> result = adapter.getAllUsers();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(m1, m2);

        verify(userRepository).findAll();
        verify(userInfrastructureMapper).toModel(e1);
        verify(userInfrastructureMapper).toModel(e2);

        verifyNoMoreInteractions(userRepository, userInfrastructureMapper);
    }
}