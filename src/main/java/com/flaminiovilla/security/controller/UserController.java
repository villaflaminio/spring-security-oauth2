package com.flaminiovilla.security.controller;

import com.flaminiovilla.security.exception.ResourceNotFoundException;
import com.flaminiovilla.security.model.User;
import com.flaminiovilla.security.repository.UserRepository;
import com.flaminiovilla.security.security.CurrentUser;
import com.flaminiovilla.security.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public User getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }
}
