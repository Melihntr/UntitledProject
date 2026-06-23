package com.project.common.infrastructure.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {

    @Test
    void getAndSetIdWorkForMappedSuperclass() {
        TestEntity entity = new TestEntity();

        entity.setId("id-1");

        assertThat(entity.getId()).isEqualTo("id-1");
    }

    private static class TestEntity extends BaseEntity {
    }
}
