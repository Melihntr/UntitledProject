package com.project.common.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSecurityHandlersTest {

    @Test
    void authenticationEntryPointWritesUnauthorizedGenericResponse() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint(new ObjectMapper());

        entryPoint.commence(new MockHttpServletRequest(), response, new BadCredentialsException("bad"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Authentication is required.");
    }

    @Test
    void accessDeniedHandlerWritesForbiddenGenericResponse() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonAccessDeniedHandler handler = new JsonAccessDeniedHandler(new ObjectMapper());

        handler.handle(new MockHttpServletRequest(), response, new AccessDeniedException("denied"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("Access denied for this role.");
    }
}
