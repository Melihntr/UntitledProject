package com.project.user.domain.port;

import com.project.user.domain.model.UserModel;

/**
 * Outbound Port for User operations.
 * The Domain layer uses this interface to interact with external data sources
 * without knowing the underlying technology (e.g., JPA, Mongo, etc.).
 */
public interface UserPort {
    
    UserModel save(UserModel userModel);
    
    // Future methods like findById, existsByEmail, etc. can be added here
    java.util.List<UserModel> getAllUsers();
}