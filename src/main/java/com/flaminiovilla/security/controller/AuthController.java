package com.flaminiovilla.security.controller;

import com.flaminiovilla.security.config.AppProperties;
import com.flaminiovilla.security.exception.BadRequestException;
import com.flaminiovilla.security.exception.ResourceNotFoundException;
import com.flaminiovilla.security.model.*;
import com.flaminiovilla.security.model.dto.*;
import com.flaminiovilla.security.repository.PasswordResetTokenRepository;
import com.flaminiovilla.security.repository.RoleRepository;
import com.flaminiovilla.security.repository.UserRepository;
import com.flaminiovilla.security.security.CurrentUser;
import com.flaminiovilla.security.security.RefreshTokenService;
import com.flaminiovilla.security.security.TokenProvider;
import com.flaminiovilla.security.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

/**
 * Controller with the REST endpoints for authentication and user management.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private AppProperties appProperties;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * Authenticate a user and return the JWT token.
     *
     * @param loginRequestDto the login request
     * @return the authenticated user with the JWT token
     */
    @PostMapping("/login")
    public AuthResponseDto authenticateUser(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );

        // Set the authentication in the Security Context.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get the user details from the authentication, if the user is not found an exception will be thrown.
        User user = userRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow(() -> new BadRequestException("User not found with email : " + loginRequestDto.getEmail()));

        // Generate the token and return the wrapped response.
        return tokenProvider.generateAuthFromUser(user);
    }

    /**
     * Register a new user.
     * @param signUpRequestDto the user to register
     * @return the created user
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        // Check if the email is already in use.
        if(userRepository.existsByEmail(signUpRequestDto.getEmail()))
            throw new BadRequestException("Email address already in use.");

        // Trying to find the role with the given name, if not found a bad request exception will be thrown.
        Role role = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new BadRequestException("Role not found"));

        // Create the collection to store all the roles.
        ArrayList<Role> roles = new ArrayList<>();
        roles.add(role);

        // Creating user's account
        User user = new User();
        user.setName(signUpRequestDto.getName());
        user.setEmail(signUpRequestDto.getEmail());
        user.setPassword(signUpRequestDto.getPassword());
        user.setProvider(AuthProvider.local);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(roles);

        // Save the user in the database.
        User result = userRepository.save(user);

        // Return the created user.
        return ResponseEntity.ok(result);
    }

    /**
     * Refresh the JWT token.
     * @param requestRefreshToken the string to request a new token
     * @return the refreshed token
     */
    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshtoken(@RequestParam(value = "refreshToken") String requestRefreshToken) throws Exception {
        // Find the refresh token in the database.
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken).orElseThrow(() -> new Exception("Refresh Token non trovato"));

        // Check if the refresh token is expired.
        refreshTokenService.verifyExpiration(refreshToken);

        // Get the user from the refresh token.
        User user = refreshToken.getUser();
        String token = tokenProvider.generateTokenFromUser(user);

        // Return the refreshed token.
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
        TokenRefreshResponse tokenRefreshResponse = new TokenRefreshResponse(token, newRefreshToken.getToken());

        // Create the authentication response.
        AuthResponseDto authResponseDto =  AuthResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getAuthorities())
                .token(tokenRefreshResponse.getAccessToken())
                .refreshToken(tokenRefreshResponse.getRefreshToken())
                .duration(Long.toString(appProperties.getAuth().getTokenExpirationMsec()))
                .build();

        // Return the authentication response.
        return ResponseEntity.ok(authResponseDto);
    }

    /**
     * Create the first user in the database.
     * @return the created user
     */
    @PostMapping("/creaUtenteIniziale")
    public ResponseEntity<?> creaUtenteIniziale(HttpServletRequest req){
        // Get ADMIN and USER role.
        Optional<Role> ruoloAdmin = roleRepository.findByName("ROLE_ADMIN");
        Optional<Role> ruoloUser = roleRepository.findByName("ROLE_USER");
        ArrayList<Role> roles = new ArrayList<>();

        // If the roles are not present, create them.
        if(!ruoloAdmin.isPresent()) {
            Role ruolo = new Role();
            ruolo.setName("ROLE_ADMIN");
            roles.add(roleRepository.save(ruolo));
        }else {
            roles.add(ruoloAdmin.get());
        }
        if(!ruoloUser.isPresent()) {
            Role ruolo = new Role();
            ruolo.setName("ROLE_USER");
            roles.add(roleRepository.save(ruolo));
        }else {
            roles.add(ruoloUser.get());
        }


        User user = new User();
        user.setEmail("admin@flaminiovilla.it");
        user.setName("admin");
        user.setPassword(passwordEncoder.encode("flaminio"));
        user.setRoles(roles);
        user.setProvider(AuthProvider.local);

        // Save the user in the database.
        userRepository.save(user);

        // Return the created user.
        return ResponseEntity.ok(user);
    }

    /**
     * Reset the password of the user.
     * @param userEmail the email of the user to reset the password
     * @return the response
     */
    @PostMapping("/recoveryPassword")
    public ResponseEntity<?> recoveryPassword(@RequestParam("email") String userEmail) {
        // Find the user with the given email.
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new BadRequestException("User not found with email : " + userEmail));

        // Check if the user is logged in with a social provider.
        if(user.getProvider() != AuthProvider.local) {
            return ResponseEntity.badRequest().body("L'utente non è registrato con un account locale, accedere con OAuth2 di " + user.getProvider());
        }

        // Return the response (200 - OK) and call the method to send the email to recover password.
        return ResponseEntity.ok(customUserDetailsService.requestResetPassword(user));
    }

    /**
     * Retrieve the authentication of the user with the given token to request a change of password.
     * @param token the request to reset the password
     * @return the response
     */
    @GetMapping("/tokenResetPassword")
    public ResponseEntity<?> getAuthenticationToChangePassword(@RequestParam("token") String token) {
        // Find the password reset token using the given token.
        Optional<PasswordResetToken> userPasswToken = passwordResetTokenRepository.findByToken(token);

        // Check if the token is present.
        if(!userPasswToken.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponseDto(false, "Non è stato trovato nessun token"));
        }

        // Retrieve the user from the token.
        User user = userPasswToken.get().getUser();

        // Request the token to change the password.
        return customUserDetailsService.requestTokenRecoveryPassword(token , user);
    }

    @GetMapping("/session")
    public ResponseEntity<?> getCredentialsOAuth2(@CurrentUser UserPrincipal userPrincipal) {
        return customUserDetailsService.session(userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId())));
    }
}
