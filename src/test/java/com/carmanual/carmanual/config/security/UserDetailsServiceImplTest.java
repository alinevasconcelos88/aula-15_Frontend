package com.carmanual.carmanual.config.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.carmanual.carmanual.models.User;
import com.carmanual.carmanual.repositories.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

class UserDetailsServiceImplTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl(userRepository);

    @Test
    void loadsAdminAuthorityFromUserRole() {
        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(new User(1L, "admin", "encoded", "ADMIN")));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertEquals("ROLE_ADMIN", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void treatsExistingUsersWithoutRoleAsRegularUsers() {
        when(userRepository.findByUsername("legacy"))
                .thenReturn(Optional.of(new User(2L, "legacy", "encoded", null)));

        UserDetails userDetails = userDetailsService.loadUserByUsername("legacy");

        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
    }
}
