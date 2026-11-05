package com.carmanual.carmanual.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.carmanual.carmanual.exceptions.AuthorizationException;
import com.carmanual.carmanual.models.User;
import com.carmanual.carmanual.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

class UserServiceAuthorizationTest {

    private final UserRepository repository = mock(UserRepository.class);
    private final UserService userService = new UserService(repository);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ownerCanAccessOwnResource() {
        authenticate("owner", "ROLE_USER");
        when(repository.findByUsername("owner"))
                .thenReturn(Optional.of(new User(7L, "owner", "password", "USER")));

        assertDoesNotThrow(() -> userService.checkUserAccess(7L));
    }

    @Test
    void regularUserCannotAccessAnotherUsersResource() {
        authenticate("owner", "ROLE_USER");
        when(repository.findByUsername("owner"))
                .thenReturn(Optional.of(new User(7L, "owner", "password", "USER")));

        AuthorizationException exception = assertThrows(
                AuthorizationException.class,
                () -> userService.checkUserAccess(8L));

        assertEquals("Acesso negado!", exception.getMessage());
    }

    @Test
    void adminCanAccessAnyUsersResourceWithoutLookingUpOwner() {
        authenticate("admin", "ROLE_ADMIN");

        assertDoesNotThrow(() -> userService.checkUserAccess(8L));
        verifyNoInteractions(repository);
    }

    @Test
    void unauthenticatedRequestIsDenied() {
        SecurityContextHolder.clearContext();

        AuthorizationException exception = assertThrows(
                AuthorizationException.class,
                () -> userService.checkUserAccess(7L));

        assertEquals("Acesso negado!", exception.getMessage());
    }

    private void authenticate(String username, String authority) {
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("password")
                .authorities(authority)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        principal, null, List.of(new SimpleGrantedAuthority(authority))));
    }
}
