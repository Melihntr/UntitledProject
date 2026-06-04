package com.project.user.domain.port;

import com.project.user.domain.model.UserModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Small smoke test for the UserPort contract.
 *
 * Note: Interfaces themselves usually don't need direct unit tests. This test only
 * exercises a tiny in-memory anonymous implementation to assert the expected
 * contract (save returns the model and getAllUsers returns stored items).
 * Prefer testing concrete implementations (e.g., UserPersistenceAdapter) instead.
 */
class UserPortTest {

    @Test
    void inMemoryImplementation_savesAndReturnsUsers() {
        // Arrange: create a trivial in-memory implementation of the port
        UserPort inMemory = new UserPort() {
            private final java.util.List<UserModel> store = new java.util.ArrayList<>();

            @Override
            public UserModel save(UserModel userModel) {
                store.add(userModel);
                return userModel;
            }

            @Override
            public List<UserModel> getAllUsers() {
                return List.copyOf(store);
            }
        };

        UserModel user = UserModel.builder()
                .id("u-1")
                .username("alice")
                .build();

        // Act
        UserModel returned = inMemory.save(user);
        List<UserModel> all = inMemory.getAllUsers();

        // Assert
        assertThat(returned).isSameAs(user);
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getId()).isEqualTo("u-1");
        assertThat(all.get(0).getUsername()).isEqualTo("alice");
    }
}
