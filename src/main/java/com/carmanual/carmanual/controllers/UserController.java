package com.carmanual.carmanual.controllers;

import com.carmanual.carmanual.models.User;
import com.carmanual.carmanual.repositories.UserRepository;
import com.carmanual.carmanual.services.UserService;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummary> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        return ResponseEntity.ok(new UserSummary(user.getId(), user.getUsername(), user.getRole()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserSummary> findById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(new UserSummary(user.getId(), user.getUsername(), user.getRole()));
    }

    public record UserSummary(Long id, String username, String role) {
    }
}
