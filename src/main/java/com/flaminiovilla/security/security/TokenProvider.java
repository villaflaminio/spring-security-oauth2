package com.flaminiovilla.security.security;

import com.flaminiovilla.security.config.AppProperties;
import com.flaminiovilla.security.model.RefreshToken;
import com.flaminiovilla.security.model.Role;
import com.flaminiovilla.security.model.User;
import com.flaminiovilla.security.model.dto.AuthResponseDto;
import com.flaminiovilla.security.model.dto.UserPrincipal;
import com.flaminiovilla.security.repository.UserRepository;
import com.google.gson.Gson;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private AppProperties appProperties;
    private RefreshTokenService refreshTokenService;
    @Autowired
    private UserRepository userRepository;
    public TokenProvider(AppProperties appProperties, ApplicationContext context) {
        this.appProperties = appProperties;
        this.refreshTokenService = context.getBean(RefreshTokenService.class);

    }

    public String createToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<String> roles = userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getTokenExpirationMsec());
        String signingKey = appProperties.getAuth().getTokenSecret();

        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, appProperties.getAuth().getTokenSecret())
                .setHeaderParam("typ", "JWT")
                .setIssuedAt(new Date())
                .setAudience("secure-app")
                .setSubject(Long.toString(userPrincipal.getId()))
                .setExpiration(expiryDate)
                .claim("rol", roles)
                .compact();

    }

    public HttpServletResponse createAuthResponse(Authentication authentication, HttpServletResponse response) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new RuntimeException("User not found"));

        String token = createToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        response.addHeader("Authorization", "Bearer " + token);

        try{

            //preparo la risposta da inviare al FE con Username, Authorities e Duration del token di autenticazione
            response.setHeader("Access-Control-Expose-Headers", "*");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            AuthResponseDto authResponseDto =  AuthResponseDto.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getAuthorities())
                    .token(token)
                    .refreshToken(refreshToken.getToken())
                    .duration(Long.toString(appProperties.getAuth().getTokenExpirationMsec()))
                    .build();
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(authResponseDto));

        }catch ( Exception e){
            e.printStackTrace();
        }
        return response;
    }

    public String generateTokenFromUsername(User user) {
        List<String> roles = new ArrayList<>();
        for(Role r : user.getRoles()){
            roles.add(r.getName());
        }
        Date now = new Date();

        Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getTokenExpirationMsec());
        String signingKey = appProperties.getAuth().getTokenSecret();
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, appProperties.getAuth().getTokenSecret())
                .setHeaderParam("typ", "JWT")
                .setIssuedAt(new Date())
                .setAudience("secure-app")
                .setSubject(Long.toString(user.getId()))
                .setExpiration(expiryDate)
                .claim("rol", roles)
                .compact();
    }
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(appProperties.getAuth().getTokenSecret())
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(appProperties.getAuth().getTokenSecret()).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }

}
