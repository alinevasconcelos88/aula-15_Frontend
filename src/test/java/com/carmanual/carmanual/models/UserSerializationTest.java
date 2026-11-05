package com.carmanual.carmanual.models;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class UserSerializationTest {

    @Test
    void passwordIsNeverIncludedInSerializedUser() throws JsonProcessingException {
        User user = new User(1L, "test-user", "$2a$12$encoded-password", "USER");

        String json = new ObjectMapper().writeValueAsString(user);

        assertFalse(json.contains("password"));
        assertFalse(json.contains("$2a$12$encoded-password"));
    }
}
