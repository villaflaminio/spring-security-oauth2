package com.flaminiovilla.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date;

/**
 * The type Password reset token.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    // The constant EXPIRATION.
    private static final long EXPIRATION = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private Instant expiryDate;

    /**
     * Instantiates a new Password reset token.
     * @param token the token to be set
     * @param user  the user
     */
    public PasswordResetToken(String token, User user, Instant date) {
        this.token = token;
        this.user = user;
        expiryDate = date;
    }


}