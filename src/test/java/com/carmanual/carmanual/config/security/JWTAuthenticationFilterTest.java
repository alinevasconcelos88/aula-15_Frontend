package com.carmanual.carmanual.config.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.carmanual.carmanual.config.security.util.JWTUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

class JWTAuthenticationFilterTest {

    private static final String SECRET = "01234567890123456789012345678901";

    private final JWTUtil jwtUtil = new JWTUtil(SECRET);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final JWTAuthenticationFilter filter = new JWTAuthenticationFilter(jwtUtil, userDetailsService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validBearerTokenAuthenticatesTheRequest() throws Exception {
        UserDetails user = User.withUsername("test-user")
                .password("encoded-password")
                .authorities("ROLE_USER")
                .build();
        when(userDetailsService.loadUserByUsername("test-user")).thenReturn(user);
        MockHttpServletRequest request = requestWithToken(jwtUtil.generateToken("test-user"));
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertEquals("test-user", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(userDetailsService).loadUserByUsername("test-user");
    }

    @Test
    void invalidBearerTokenDoesNotAuthenticateTheRequest() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(requestWithToken("not-a-jwt"), new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void validTokenForUnknownUserDoesNotAuthenticateTheRequest() throws Exception {
        org.mockito.Mockito.doThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "unknown"))
                .when(userDetailsService)
                .loadUserByUsername("missing-user");
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(
                requestWithToken(jwtUtil.generateToken("missing-user")),
                new MockHttpServletResponse(),
                chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService).loadUserByUsername("missing-user");
    }

    private MockHttpServletRequest requestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
