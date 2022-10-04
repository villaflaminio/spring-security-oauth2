package com.flaminiovilla.security.model.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * This class represents the login request.
 */
@Data
public class SessionRequestDto {

    @NotBlank
    private String sessionToken;
}
