package com.flaminiovilla.security.security.oauth2;

import com.flaminiovilla.security.exception.OAuth2AuthenticationProcessingException;
import com.flaminiovilla.security.model.dto.AuthProvider;
import com.flaminiovilla.security.model.Role;
import com.flaminiovilla.security.model.User;
import com.flaminiovilla.security.repository.RoleRepository;
import com.flaminiovilla.security.repository.UserRepository;
import com.flaminiovilla.security.model.dto.UserPrincipal;
import com.flaminiovilla.security.security.oauth2.provider.OAuth2UserInfo;
import com.flaminiovilla.security.security.oauth2.provider.OAuth2UserInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Optional;

/**
 * A service to implement common operations for OAuth2 users.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    /**
     * Load a user by OAuth2 user request.
     * @param oAuth2UserRequest the OAuth2 user request
     * @return the OAuth2 user
     * @throws OAuth2AuthenticationException
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        // Get OAuth2 user using superclass method.
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        // Get OAuth2 user info processing it.
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    /**
     * Process OAuth2 user info.
     * @param oAuth2UserRequest the OAuth2 user request
     * @param oAuth2User the OAuth2 user
     * @return the OAuth2 user
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        // Get OAuth2 user info.
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());

        // Check if user is already registered.
        if(StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        // Get user by email.
        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        // If user is not registered, register it.
        if(userOptional.isPresent()) {
            user = userOptional.get();

            if(!user.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use your " + user.getProvider() +
                        " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        // Return user principal.
        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    /**
     * Register a new user.
     * @param oAuth2UserRequest the OAuth2 user request
     * @param oAuth2UserInfo the OAuth2 user info
     * @return the user
     */
    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        // Get user role.
        Role role = roleRepository.findByName("ROLE_USER").orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruolo non trovato"));

        // Create a collection of roles to be added to the user.
        ArrayList<Role> roles = new ArrayList<>();
        roles.add(role);

        // Create a new user.
        User user = new User();

        user.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        user.setProviderId(oAuth2UserInfo.getId());
        user.setName(oAuth2UserInfo.getName());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setImageUrl(oAuth2UserInfo.getImageUrl());
        user.setRoles(roles);

        // Save user.
        return userRepository.save(user);
    }

    /**
     * Update an existing user.
     * @param existingUser the existing user
     * @param oAuth2UserInfo the OAuth2 user info
     * @return the updated user
     */
    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        // Update user information.
        existingUser.setName(oAuth2UserInfo.getName());
        existingUser.setImageUrl(oAuth2UserInfo.getImageUrl());
        return userRepository.save(existingUser);
    }

}
