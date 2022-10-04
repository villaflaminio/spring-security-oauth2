package com.flaminiovilla.security.service;


import com.flaminiovilla.security.exception.BadRequestException;
import com.flaminiovilla.security.exception.ResourceNotFoundException;
import com.flaminiovilla.security.model.PasswordResetToken;
import com.flaminiovilla.security.model.User;
import com.flaminiovilla.security.model.dto.ApiResponseDto;
import com.flaminiovilla.security.model.dto.MailResponse;
import com.flaminiovilla.security.model.UserPrincipal;
import com.flaminiovilla.security.model.dto.SessionRequestDto;
import com.flaminiovilla.security.repository.PasswordResetTokenRepository;
import com.flaminiovilla.security.repository.UserRepository;
import com.flaminiovilla.security.security.TokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;


/**
 * Service to handle user details.
 */
@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    private Environment env;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TokenProvider tokenProvider;

    /**
     * Load user by email.
     * @param email The username.
     * @return The user details.
     * @throws UsernameNotFoundException
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email : " + email)
        );

        return UserPrincipal.create(user);
    }

    /**
     * Request a password reset.
     * @param user The user that is requesting the reset.
     * @return The mail response.
     */
    public MailResponse requestResetPassword(User user) {
        // Create a new token to reset the password.
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setUser(user);
        passwordResetToken.setExpiryDate(Instant.now().plusSeconds(Long.parseLong(Objects.requireNonNull(env.getProperty("app.auth.refreshTokenExpiration")))));

        // Save the password reset token
        passwordResetTokenRepository.save(passwordResetToken);

        // Send the email
        Map<String, Object> model = new HashMap<>();
        model.put("name", user.getName());
        model.put("indirizzo", "http://localhost:8080/" + "user/changePassword?token=" + token );
        return emailService.sendEmail(user.getEmail(),"Reset password",model,"recuperoPassword");
    }

    /**
     * Validate the password reset token.
     * @param token The token to validate.
     * @return invalidToken if the token is invalid, expired if the token is expired
     */
    public String validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> userPasswToken = passwordResetTokenRepository.findByToken(token);
        if(!userPasswToken.isPresent()) {
            throw new BadRequestException("Token non valido");
        }
        final PasswordResetToken passToken = userPasswToken.get();

        return !isTokenFound(passToken) ? "invalidToken"
                : isTokenExpired(passToken) ? "expired"
                : null;
    }

    public ResponseEntity<?>  session(User user) {
        return ResponseEntity.ok(tokenProvider.generateAuthFromUser(user));

    }


    /**
     * Check if the token is found.
     * @param passToken The token to check.
     * @return True if the token is found.
     */
    private boolean isTokenFound(PasswordResetToken passToken) {
        return passToken != null;
    }

    /**
     * Check if the token is expired.
     * @param passToken The token to check.
     * @return True if the token is expired.
     */
    private boolean isTokenExpired(PasswordResetToken passToken) {
        return passToken.getExpiryDate().compareTo(Instant.now()) <= 0;
    }

    /**
     * Request token recovery password.
     * @param token
     * @param user
     * @return
     */
    public ResponseEntity<?> requestTokenRecoveryPassword(String token , User user) {
        String result = validatePasswordResetToken(token);
        if(result != null) {
            return ResponseEntity.badRequest().body(new ApiResponseDto(false, result));
        } else {
            return ResponseEntity.ok(tokenProvider.generateAuthFromUser(user));
        }
    }

    /**
     * Load user by id.
     * @param id The id of the user.
     * @return The UserDetails.
     */
    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
            () -> new ResourceNotFoundException("User", "id", id)
        );

        return UserPrincipal.create(user);
    }
}