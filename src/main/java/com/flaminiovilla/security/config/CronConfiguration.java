package com.flaminiovilla.security.config;

import com.flaminiovilla.security.repository.PasswordResetTokenRepository;
import com.flaminiovilla.security.repository.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.Objects;

@Configuration
@EnableScheduling
@AllArgsConstructor
public class CronConfiguration {

    private RefreshTokenRepository refreshTokenRepository;
    private Environment env;

    private PasswordResetTokenRepository passwordResetTokenRepository;

    // Ogni 27, 28, 1, 2, 3 e 4 del mese (1,2,3,4 successivo) ore 8.30
    @Scheduled(cron = "0 30 8 1,2,3,4,27,28 * ? ")
    public void deleteOldRefreshToken() {

    }
    @Scheduled(fixedRate = 600000)
    public void scheduleFixedRateTask() {
        try {
            refreshTokenRepository.deleteByExpiryDateIsLessThan(Instant.now().plusMillis(Long.parseLong(Objects.requireNonNull(env.getProperty("app.auth.refreshTokenExpiration"))) - 1000));
            passwordResetTokenRepository.deleteByExpiryDateIsLessThan(Instant.now().plusMillis(Long.parseLong(Objects.requireNonNull(env.getProperty("app.auth.refreshTokenExpiration"))) - 1000));
        }catch (Exception e){
        }
    }

}
