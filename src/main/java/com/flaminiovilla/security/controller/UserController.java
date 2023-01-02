package com.flaminiovilla.security.controller;

import com.flaminiovilla.security.exception.ResourceNotFoundException;
import com.flaminiovilla.security.model.User;
import com.flaminiovilla.security.repository.UserRepository;
import com.flaminiovilla.security.security.CurrentUser;
import com.flaminiovilla.security.model.UserPrincipal;
import com.flaminiovilla.security.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller with the REST endpoints for user managament.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Get the current user.
     * @param userPrincipal the current user
     * @return the current user
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public User getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        // Return the current user found by id.
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }


    @GetMapping("/testGrants")
    public String hello() {
        return "Hello World!";
    }
}
