package com.carmanual.carmanual.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carmanual.carmanual.config.security.util.JWTUtil;
import com.carmanual.carmanual.controllers.AuthController.AuthResponse;
import com.carmanual.carmanual.controllers.AuthController.UserResponse;
import com.carmanual.carmanual.models.User;
import com.carmanual.carmanual.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class AuthServiceTest {

    private static final String SECRET = "01234567890123456789012345678901";

    private final AuthenticationManager authenticationManager = org.mockito.Mockito.mock(AuthenticationManager.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
    private final JWTUtil jwtUtil = new JWTUtil(SECRET);
    private final AuthService authService =
            new AuthService(authenticationManager, userRepository, passwordEncoder, jwtUtil);

    @Test
    void registerPersistsAnEncodedPasswordAndReturnsSafeUserDetails() {
        when(userRepository.findByUsername("test-user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(12L);
            return user;
        });

        UserResponse response = authService.register("test-user", "password123");

        assertEquals(new UserResponse(12L, "test-user"), response);
        assertFalse(response.toString().contains("encoded-password"));
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user -> "USER".equals(user.getRole())));
    }

    @Test
    void registerRejectsAnExistingUsername() {
        when(userRepository.findByUsername("test-user")).thenReturn(Optional.of(new User()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register("test-user", "password123"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void loginReturnsSignedBearerTokenAfterAuthentication() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(UsernamePasswordAuthenticationToken.authenticated(
                        "test-user", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        AuthResponse response = authService.login("test-user", "password123");

        assertEquals("Bearer", response.tokenType());
        assertTrue(jwtUtil.validateToken(response.token()));
        assertEquals("test-user", jwtUtil.getUsernameFromToken(response.token()));
    }

    @Test
    void loginRejectsInvalidCredentials() {
        doThrow(new BadCredentialsException("invalid"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login("test-user", "wrong-password"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}
