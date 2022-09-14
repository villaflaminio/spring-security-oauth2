package com.flaminiovilla.security.controller;

import com.flaminiovilla.security.config.AppProperties;
import com.flaminiovilla.security.exception.BadRequestException;
import com.flaminiovilla.security.model.*;
import com.flaminiovilla.security.model.dto.*;
import com.flaminiovilla.security.repository.PasswordResetTokenRepository;
import com.flaminiovilla.security.repository.RoleRepository;
import com.flaminiovilla.security.repository.UserRepository;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URI;
import java.util.*;

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

    @PostMapping("/login")
    public AuthResponseDto authenticateUser(@Valid @RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow(() -> new BadRequestException("User not found with email : " + loginRequestDto.getEmail()));
        return tokenProvider.generateAuthFromUser(user);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        if(userRepository.existsByEmail(signUpRequestDto.getEmail())) {
            throw new BadRequestException("Email address already in use.");
        }
        Role role = roleRepository.findByName("ROLE_USER").orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruolo non trovato"));
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

        User result = userRepository.save(user);

//        URI location = ServletUriComponentsBuilder
//                .fromCurrentContextPath().path("/user/me")
//                .buildAndExpand(result.getId()).toUri();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshtoken(@RequestParam(value = "refreshToken", required = true) String requestRefreshToken) throws Exception {
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken).orElseThrow(() -> new Exception("Refresh Token non trovato"));
        refreshTokenService.verifyExpiration(refreshToken);
        User user = refreshToken.getUser();
        String token = tokenProvider.generateTokenFromUser(user);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
        TokenRefreshResponse tokenRefreshResponse = new TokenRefreshResponse(token, newRefreshToken.getToken());

        AuthResponseDto authResponseDto =  AuthResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getAuthorities())
                .token(tokenRefreshResponse.getAccessToken())
                .refreshToken(tokenRefreshResponse.getRefreshToken())
                .duration(Long.toString(appProperties.getAuth().getTokenExpirationMsec()))
                .build();

        return ResponseEntity.ok(authResponseDto);
    }

    @PostMapping("/creaUtenteIniziale")
    public ResponseEntity<?> creaUtenteIniziale(HttpServletRequest req) throws Exception {


        Optional<Role> ruoloAdmin = roleRepository.findByName("ROLE_ADMIN");
        Optional<Role> ruoloUser = roleRepository.findByName("ROLE_USER");
        if(!ruoloAdmin.isPresent()) {
            ruoloAdmin.get().setName("ROLE_ADMIN");
            roleRepository.save(ruoloAdmin.get());
        }
        if(!ruoloUser.isPresent()) {
            ruoloUser.get().setName("ROLE_USER");
            roleRepository.save(ruoloUser.get());
        }

        ArrayList<Role> roles = new ArrayList<>();
        roles.add(ruoloUser.get());
        roles.add(ruoloAdmin.get());
        User user = new User();
        user.setEmail("admin@flaminiovilla.it");
        user.setName("admin");
        user.setPassword(passwordEncoder.encode("flaminio"));
        user.setRoles(roles);
        user.setProvider(AuthProvider.local);
        userRepository.save(user);

        return ResponseEntity.ok(user);

    }

    @PostMapping("/recoveryPassword")
    public ResponseEntity<?> recoveryPassword(@RequestParam("email") String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new BadRequestException("User not found with email : " + userEmail));

        if(user.getProvider() != AuthProvider.local) {
            return ResponseEntity.badRequest().body("L'utente non è registrato con un account locale, accedere con OAuth2 di " + user.getProvider());
        }
        return ResponseEntity.ok(customUserDetailsService.reqestResetPassword(user));
    }
    @GetMapping("/tokenResetPassword")
    public ResponseEntity<?> getAuthenticationToChangePassword(@RequestParam("token") String token) {
        Optional<PasswordResetToken> userPasswToken = passwordResetTokenRepository.findByToken(token);
        if(!userPasswToken.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponseDto(false, "Non è stato trovato nessun token"));
        }
        User user = userPasswToken.get().getUser();
        return customUserDetailsService.requestTokenRecoveryPassword(token , user);
    }
}
