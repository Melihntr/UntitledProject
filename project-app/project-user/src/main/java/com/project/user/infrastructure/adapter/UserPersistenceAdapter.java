package com.project.user.infrastructure.adapter;

import com.project.user.domain.model.UserModel;
import com.project.user.domain.port.UserPort;
import com.project.user.infrastructure.entity.UserEntity;
import com.project.user.infrastructure.mapper.UserInfrastructureMapper;
import com.project.user.infrastructure.repository.UserRepository;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Infrastructure Adapter implementing the outbound Domain Port.
 * This class bridges the gap between the Domain layer and the Database.
 */
@Component
public class UserPersistenceAdapter implements UserPort {
    private final UserRepository userRepository;
    private final UserInfrastructureMapper userInfrastructureMapper;

    public UserPersistenceAdapter(UserRepository userRepository, UserInfrastructureMapper userInfrastructureMapper) {
        this.userRepository = userRepository;
        this.userInfrastructureMapper = userInfrastructureMapper;
    }

    @Override
    public UserModel save(UserModel userModel) {
        
        // 1. Convert Domain Model to Database Entity
        UserEntity entity = userInfrastructureMapper.toEntity(userModel);
        
        // 2. Save to Database using Spring Data JPA
        UserEntity savedEntity = userRepository.save(entity);
        
        // 3. Convert saved Entity back to Domain Model and return
        return userInfrastructureMapper.toModel(savedEntity);
    }
    @Override
    public List<UserModel> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userInfrastructureMapper::toModel)
                .toList();
    }
}