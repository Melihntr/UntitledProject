package com.project.user.infrastructure.mapper;

import com.project.user.domain.model.UserModel;
import com.project.user.infrastructure.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserInfrastructureMapperTest {

    private final UserInfrastructureMapper mapper = new UserInfrastructureMapperImpl();

    @Test
    void mapsDomainAndEntityIncludingActiveState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        UserModel model = UserModel.builder()
                .id("u1")
                .username("alice")
                .email("alice@example.com")
                .isActive(true)
                .createdAt(createdAt)
                .build();

        UserEntity entity = mapper.toEntity(model);
        UserModel mappedBack = mapper.toModel(entity);

        assertThat(entity.getId()).isEqualTo("u1");
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.getVersion()).isNull();
        assertThat(mappedBack.getId()).isEqualTo("u1");
        assertThat(mappedBack.isActive()).isTrue();
        assertThat(mappedBack.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void nullInputsReturnNull() {
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toModel(null)).isNull();
    }
}
