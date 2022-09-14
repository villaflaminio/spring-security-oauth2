package com.flaminiovilla.security.model.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MailResponse {
    private String message;
    private Boolean status;
}
