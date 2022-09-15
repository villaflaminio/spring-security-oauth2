package com.flaminiovilla.security.model.dto;


import lombok.*;

/**
 * This class represents the response of the email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MailResponse {
    private String message;
    private Boolean status;
}
