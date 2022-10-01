package com.flaminiovilla.security.controller;

import com.flaminiovilla.security.exception.ResourceNotFoundException;
import com.flaminiovilla.security.model.User;
import com.flaminiovilla.security.repository.UserRepository;
import com.flaminiovilla.security.security.CurrentUser;
import com.flaminiovilla.security.model.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller with the REST endpoints for user managament.
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

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

    /**
     * Update the user password
     * @param userPrincipal the current user
     * @param newPassword the new password
     * @return the updated user
     */
    @PostMapping("/changePassword")
    public User changePassword(@CurrentUser UserPrincipal userPrincipal , @RequestBody String newPassword ){
        // Find the current user by id.
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        // Update the password.
        user.setPassword(newPassword);

        // Encode the new password.
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save the user.
        return userRepository.save(user);
    }
}
