package org.ashkelyonok.authservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ashkelyonok.authservice.model.entity.UserCredential;
import org.ashkelyonok.authservice.model.enums.Role;
import org.ashkelyonok.authservice.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemUserInitializer implements CommandLineRunner {

    private final UserCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${system.user.username:system}")
    private String systemUsername;

    @Override
    public void run(String... args) {
        if (!credentialRepository.existsByUsername(systemUsername)) {
            UserCredential systemUser = UserCredential.builder()
                    .username(systemUsername)
                    .email(systemUsername + "@internal.local")
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .accountNonLocked(true)
                    .userId(-1L)
                    .build();
            credentialRepository.save(systemUser);
            log.info("System user created with username: {}", systemUsername);
        } else {
            log.debug("System user already exists: {}", systemUsername);
        }
    }
}