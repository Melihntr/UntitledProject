package com.project.user.infrastructure.mapper;

import com.project.user.domain.model.UserModel;
import com.project.user.infrastructure.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Unit tests for MapStruct generated implementation of {@link UserInfrastructureMapper}.
 *
 * Notes:
 * - The MapStruct implementation must be generated at compile time (annotation processor).
 * - Tests use reflection to set/read 'id' and 'username' on domain/entity to avoid relying on specific
 *   accessor names (Lombok-generated accessors can vary for boolean fields).
 */
class UserInfrastructureMapperTest {

    private final UserInfrastructureMapper mapper = Mappers.getMapper(UserInfrastructureMapper.class);

    @Test
    void mapperImplementation_shouldBeAvailable() {
        assertThat(mapper).isNotNull();
    }

    @Test
    void mappingNulls_shouldReturnNulls() {
        // MapStruct typically returns null when source is null
        UserEntity e = mapper.toEntity(null);
        UserModel m = mapper.toModel(null);

        assertThat(e).isNull();
        assertThat(m).isNull();
    }

    @Test
    void basicRoundTripMapping_preservesCommonFields() {
        // Prepare source entity and set common fields by reflection (avoids depending on setters)
        UserEntity sourceEntity = new UserEntity();
        try {
            setFieldValue(sourceEntity, "id", "abc-123");
            setFieldValue(sourceEntity, "username", "jdoe");
            setFieldValue(sourceEntity, "email", "jdoe@example.com");
            setFieldValue(sourceEntity, "isActive", true);
            setFieldValue(sourceEntity, "createdAt", LocalDateTime.of(2026, 6, 4, 12, 0));
            setFieldValue(sourceEntity, "version", 7L);
        } catch (ReflectiveOperationException ex) {
            // If fields are not present as expected, fail with explanatory message
            fail("Failed to set fields on UserEntity via reflection. Adjust test to match actual field names/types.", ex);
            return;
        }

        // Map entity -> domain model
        UserModel mappedModel = mapper.toModel(sourceEntity);
        assertThat(mappedModel).isNotNull();

        // Assert that id and username were mapped (reflectively read from model)
        try {
            Object idOnModel = getFieldValue(mappedModel, "id");
            Object usernameOnModel = getFieldValue(mappedModel, "username");

            assertThat(idOnModel).isEqualTo("abc-123");
            assertThat(usernameOnModel).isEqualTo("jdoe");
        } catch (ReflectiveOperationException ex) {
            // If the domain model uses different field names, fail and show details
            fail("Failed to read expected fields 'id' or 'username' on UserModel via reflection. Adapt the test to the actual domain model.", ex);
        }

        // Now do a round-trip: create a domain model, map to entity, assert preserved fields
        UserModel sourceModel;
        try {
            sourceModel = UserModel.class.getDeclaredConstructor().newInstance();
            setFieldValue(sourceModel, "id", "xyz-789");
            setFieldValue(sourceModel, "username", "alice");
            // other fields optional; only id/username are asserted below
        } catch (ReflectiveOperationException ex) {
            fail("Failed to instantiate or set fields on UserModel via reflection. Adapt test to the actual constructors/fields.", ex);
            return;
        }

        UserEntity mappedEntity = mapper.toEntity(sourceModel);
        assertThat(mappedEntity).isNotNull();

        try {
            Object idOnEntity = getFieldValue(mappedEntity, "id");
            Object usernameOnEntity = getFieldValue(mappedEntity, "username");

            assertThat(idOnEntity).isEqualTo("xyz-789");
            assertThat(usernameOnEntity).isEqualTo("alice");
        } catch (ReflectiveOperationException ex) {
            fail("Failed to read expected fields 'id' or 'username' on UserEntity after mapping. Adapt the test to the actual entity field names.", ex);
        }
    }

    // Utility reflection helpers
    private static void setFieldValue(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Field f = findField(target.getClass(), fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Object getFieldValue(Object target, String fieldName) throws ReflectiveOperationException {
        Field f = findField(target.getClass(), fieldName);
        f.setAccessible(true);
        return f.get(target);
    }

    private static Field findField(Class<?> cls, String fieldName) throws NoSuchFieldException {
        Class<?> current = cls;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field '" + fieldName + "' not found on " + cls.getName() + " or its superclasses.");
    }
}