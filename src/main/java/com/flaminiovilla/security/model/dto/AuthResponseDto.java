package com.flaminiovilla.security.model.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

@Data
@Builder
public class AuthResponseDto {
    public long id;
    public String email;
    public String name;
    public Collection<? extends SimpleGrantedAuthority> role;
    public String token;
    public String refreshToken;
    public String duration;

}
