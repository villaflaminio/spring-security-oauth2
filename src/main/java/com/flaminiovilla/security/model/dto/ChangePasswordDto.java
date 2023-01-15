package com.flaminiovilla.security.model.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * This class represents the sign up request.
 */
@Data
public class ChangePasswordDto {
    @NotBlank
    private String newPassword;

}
