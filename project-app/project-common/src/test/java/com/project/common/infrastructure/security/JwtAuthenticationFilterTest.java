package com.project.common.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtAuthenticationFilterTest {

    private JwtTokenService jwtTokenService;
    private TokenBlacklistService blacklistService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService(new ObjectMapper());
        ReflectionTestUtils.setField(jwtTokenService, "secret", "filter-secret-that-is-long-enough");
        ReflectionTestUtils.setField(jwtTokenService, "accessTokenTtl", Duration.ofMinutes(15));
        ReflectionTestUtils.setField(jwtTokenService, "refreshTokenTtl", Duration.ofHours(8));
        blacklistService = new TokenBlacklistService();
        filter = new JwtAuthenticationFilter(jwtTokenService, blacklistService, new ObjectMapper());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterWithoutBearerTokenContinuesWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterWithNonBearerAuthorizationContinuesWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic abc");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterWithValidAccessTokenSetsAuthentication() throws Exception {
        String token = jwtTokenService.createAccessToken("user-1", AuthConstants.ROLE_USER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, AuthConstants.BEARER_PREFIX + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(new AuthenticatedUser("user-1", AuthConstants.ROLE_USER));
    }

    @Test
    void doFilterWithInvalidTokenReturnsUnauthorizedJson() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, AuthConstants.BEARER_PREFIX + "bad.token.value");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Token signature is invalid.");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterWithBlacklistedTokenReturnsUnauthorizedJson() throws Exception {
        String token = jwtTokenService.createAccessToken("user-1", AuthConstants.ROLE_USER);
        TokenClaims claims = jwtTokenService.parseAndValidate(token, AuthConstants.TOKEN_TYPE_ACCESS);
        blacklistService.blacklist(claims.tokenId(), claims.expiresAt());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, AuthConstants.BEARER_PREFIX + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Token has been logged out.");
    }
}
