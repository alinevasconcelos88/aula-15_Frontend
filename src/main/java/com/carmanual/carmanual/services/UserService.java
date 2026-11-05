package com.carmanual.carmanual.services;

import com.carmanual.carmanual.exceptions.AuthorizationException;
import com.carmanual.carmanual.models.User;
import com.carmanual.carmanual.repositories.UserRepository;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void checkUserAccess(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthorizationException("Acesso negado!");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()) || "ADMIN".equals(authority.getAuthority()));

        if (isAdmin) {
            return;
        }

        String username = authentication.getName();
        Optional<User> currentUser = userRepository.findByUsername(username);

        if (currentUser.isPresent() && userId.equals(currentUser.get().getId())) {
            return;
        }

        throw new AuthorizationException("Acesso negado!");
    }

    public User findById(Long id) {
        checkUserAccess(id);
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario não encontrado! Id: " + id));
    }
}
