package com.flaminiovilla.security.controller;

import com.flaminiovilla.security.config.AppProperties;
import com.flaminiovilla.security.exception.BadRequestException;
import com.flaminiovilla.security.model.*;
import com.flaminiovilla.security.model.dto.*;
import com.flaminiovilla.security.repository.RoleRepository;
import com.flaminiovilla.security.repository.UserRepository;
import com.flaminiovilla.security.security.RefreshTokenService;
import com.flaminiovilla.security.security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
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




    @PostMapping("/login")
    public HttpServletResponse authenticateUser(@Valid @RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return tokenProvider.createAuthResponse(authentication,response);
    }
    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshtoken(@RequestParam(value = "refreshToken", required = true) String requestRefreshToken) throws Exception {
        System.out.println(requestRefreshToken);
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
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        if(userRepository.existsByEmail(signUpRequestDto.getEmail())) {
            throw new BadRequestException("Email address already in use.");
        }

        // Creating user's account
        User user = new User();
        user.setName(signUpRequestDto.getName());
        user.setEmail(signUpRequestDto.getEmail());
        user.setPassword(signUpRequestDto.getPassword());
        user.setProvider(AuthProvider.local);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User result = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/user/me")
                .buildAndExpand(result.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponseDto(true, "User registered successfully@"));
    }



    @PostMapping("/creaUtenteIniziale")
    public ResponseEntity<?> creaUtenteIniziale(HttpServletRequest req) throws Exception {
        Role ruoloAdmin = new Role();
        ruoloAdmin.setName("ROLE_ADMIN");
        roleRepository.save(ruoloAdmin);

        Role ruoloUser = new Role();
        ruoloUser.setName("ROLE_USER");
        roleRepository.save(ruoloUser);

        ArrayList<Role> roles = new ArrayList<>();
        roles.add(ruoloUser);
        roles.add(ruoloAdmin);
        User user = new User();
        user.setEmail("admin@flaminiovilla.it");
        user.setName("admin");
        user.setPassword(passwordEncoder.encode("flaminio"));
        user.setRoles(roles);
        user.setProvider(AuthProvider.local);
        userRepository.save(user);

        return ResponseEntity.ok(user);

    }
}
