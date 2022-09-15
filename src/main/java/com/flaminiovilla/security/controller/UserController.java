package com.flaminiovilla.security.controller;
import com.flaminiovilla.security.exception.ResourceNotFoundException;
import com.flaminiovilla.security.model.User;
import com.flaminiovilla.security.repository.UserRepository;
import com.flaminiovilla.security.security.CurrentUser;
import com.flaminiovilla.security.model.dto.UserPrincipal;
import com.flaminiovilla.security.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public User getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }

    @PostMapping("/changePassword")
    public User changePassword(@CurrentUser UserPrincipal userPrincipal , @RequestBody String newPassword ){
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        user.setPassword(newPassword);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }





}
