package com.flaminiovilla.security.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * The type API response DTO to store the response of the API.
 */
@Data
@Builder
public class AlterUserDto {
   public String name;
    public String email;
    public String imageUrl;
    public Boolean enabled;
    public List<String> roles;

}
