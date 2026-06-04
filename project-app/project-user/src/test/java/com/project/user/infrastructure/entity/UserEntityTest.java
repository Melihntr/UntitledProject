package com.project.user.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserEntity verifying JPA annotations and basic field behavior.
 *
 * Note: Lombok generates getters/setters at compile time; to avoid depending on exact
 * accessor method names (especially for the boolean field named "isActive"), these tests
 * interact with fields reflectively and assert annotation metadata.
 */
class UserEntityTest {

    @Test
    void class_shouldBeAnnotatedWithEntity_andTableNamedUsers() {
        Class<UserEntity> cls = UserEntity.class;

        // Class-level annotations
        assertThat(cls.isAnnotationPresent(Entity.class)).isTrue();
        assertThat(cls.isAnnotationPresent(Table.class)).isTrue();

        Table table = cls.getAnnotation(Table.class);
        assertThat(table.name()).isEqualTo("users");
    }

    @Test
    void idField_shouldHaveIdAndColumnProperties() throws NoSuchFieldException {
        Field idField = UserEntity.class.getDeclaredField("id");

        // Field-level @Id
        assertThat(idField.isAnnotationPresent(Id.class)).isTrue();

        // Column attributes
        assertThat(idField.isAnnotationPresent(Column.class)).isTrue();
        Column col = idField.getAnnotation(Column.class);
        assertThat(col.name()).isEqualTo("id");
        assertThat(col.updatable()).isFalse();
        assertThat(col.nullable()).isFalse();
    }

    @Test
    void usernameAndEmailFields_shouldHaveColumnProperties() throws NoSuchFieldException {
        Field usernameField = UserEntity.class.getDeclaredField("username");
        Field emailField = UserEntity.class.getDeclaredField("email");

        assertThat(usernameField.isAnnotationPresent(Column.class)).isTrue();
        Column usernameCol = usernameField.getAnnotation(Column.class);
        assertThat(usernameCol.name()).isEqualTo("username");
        assertThat(usernameCol.nullable()).isFalse();
        assertThat(usernameCol.unique()).isTrue();

        assertThat(emailField.isAnnotationPresent(Column.class)).isTrue();
        Column emailCol = emailField.getAnnotation(Column.class);
        assertThat(emailCol.name()).isEqualTo("email");
        assertThat(emailCol.nullable()).isFalse();
        assertThat(emailCol.unique()).isTrue();
    }

    @Test
    void isActiveAndCreatedAt_shouldHaveExpectedColumnAttributes() throws NoSuchFieldException {
        Field activeField = UserEntity.class.getDeclaredField("isActive");
        Field createdAtField = UserEntity.class.getDeclaredField("createdAt");

        assertThat(activeField.isAnnotationPresent(Column.class)).isTrue();
        Column activeCol = activeField.getAnnotation(Column.class);
        assertThat(activeCol.name()).isEqualTo("is_active");
        assertThat(activeCol.nullable()).isFalse();

        assertThat(createdAtField.isAnnotationPresent(Column.class)).isTrue();
        Column createdAtCol = createdAtField.getAnnotation(Column.class);
        assertThat(createdAtCol.name()).isEqualTo("created_at");
        assertThat(createdAtCol.nullable()).isFalse();
        assertThat(createdAtCol.updatable()).isFalse();
    }

    @Test
    void versionField_shouldBeAnnotatedWithVersion() throws NoSuchFieldException {
        Field versionField = UserEntity.class.getDeclaredField("version");
        assertThat(versionField.isAnnotationPresent(Version.class)).isTrue();
    }

    @Test
    void fields_shouldHoldAssignedValues_viaReflection() throws Exception {
        UserEntity entity = new UserEntity();

        // Access and set private fields reflectively
        Field idField = UserEntity.class.getDeclaredField("id");
        Field usernameField = UserEntity.class.getDeclaredField("username");
        Field emailField = UserEntity.class.getDeclaredField("email");
        Field activeField = UserEntity.class.getDeclaredField("isActive");
        Field createdAtField = UserEntity.class.getDeclaredField("createdAt");
        Field versionField = UserEntity.class.getDeclaredField("version");

        idField.setAccessible(true);
        usernameField.setAccessible(true);
        emailField.setAccessible(true);
        activeField.setAccessible(true);
        createdAtField.setAccessible(true);
        versionField.setAccessible(true);

        String id = "abc-123";
        String username = "jdoe";
        String email = "jdoe@example.com";
        boolean active = true;
        LocalDateTime now = LocalDateTime.of(2026, 6, 4, 12, 0);
        Long version = 5L;

        idField.set(entity, id);
        usernameField.set(entity, username);
        emailField.set(entity, email);
        activeField.set(entity, active);
        createdAtField.set(entity, now);
        versionField.set(entity, version);

        // Read back and assert
        assertThat(idField.get(entity)).isEqualTo(id);
        assertThat(usernameField.get(entity)).isEqualTo(username);
        assertThat(emailField.get(entity)).isEqualTo(email);
        assertThat(activeField.get(entity)).isEqualTo(active);
        assertThat(createdAtField.get(entity)).isEqualTo(now);
        assertThat(versionField.get(entity)).isEqualTo(version);
    }
}
