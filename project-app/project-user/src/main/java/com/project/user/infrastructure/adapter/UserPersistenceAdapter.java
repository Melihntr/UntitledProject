package com.project.user.infrastructure.adapter;

import com.project.user.domain.model.UserModel;
import com.project.user.domain.port.UserPort;
import com.project.user.infrastructure.entity.UserEntity;
import com.project.user.infrastructure.mapper.UserInfrastructureMapper;
import com.project.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Infrastructure persistence adapter implementing the outbound {@link UserPort}.
 * This class acts as a secondary adapter in the Hexagonal Architecture pattern.
 * It encapsulates all database-specific logic, shielding the Domain layer from 
 * underlying ORM (Spring Data JPA) concerns.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPort {

    private final UserRepository userRepository;
    private final UserInfrastructureMapper userInfrastructureMapper;

    /**
     * Persists the user domain model to the relational database.
     * Maps the Rich Domain Model to a Database Entity before delegating to the repository.
     *
     * @param userModel The domain model to persist.
     * @return The persisted domain model, reflecting any database-side updates (e.g., generated timestamps).
     */
    @Override
    public UserModel save(UserModel userModel) {
        log.debug("Persisting user domain model to the database: {}", userModel.getId());

        // 1. Convert Domain Model to Database Entity
        UserEntity entity = userInfrastructureMapper.toEntity(userModel);
        
        // 2. Save to Database using Spring Data JPA
        UserEntity savedEntity = userRepository.save(entity);
        
        // 3. Convert saved Entity back to Domain Model and return
        UserModel savedModel = userInfrastructureMapper.toModel(savedEntity);
        
        log.info("Successfully persisted user with ID: {}", savedModel.getId());
        return savedModel;
    }

    /**
     * Retrieves all user records from the database.
     * Maps the retrieved entities back into domain models to keep the domain clean.
     *
     * @return A list of user domain models.
     */
    @Override
    public List<UserModel> getAllUsers() {
        log.debug("Retrieving all user records from the repository.");
        
        List<UserModel> users = userRepository.findAll().stream()
                .map(userInfrastructureMapper::toModel)
                .collect(Collectors.toList());

        log.info("Retrieved {} users from the database.", users.size());
        return users;
    }
}
